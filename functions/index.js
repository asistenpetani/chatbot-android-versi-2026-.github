import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import nodemailer from "nodemailer";

// Inisialisasi Firebase Admin SDK (Hanya dipanggil sekali)
admin.initializeApp();
const db = admin.firestore();

// Konfigurasi nodemailer transporter untuk pengiriman email no-reply
const getTransporter = () => {
  const smtpUser = process.env.SMTP_USER || functions.config().smtp?.user;
  const smtpPass = process.env.SMTP_PASS || functions.config().smtp?.pass;
  const smtpHost = process.env.SMTP_HOST || functions.config().smtp?.host || "smtp.gmail.com";
  const smtpPort = parseInt(process.env.SMTP_PORT || functions.config().smtp?.port || "465", 10);

  if (smtpUser && smtpPass) {
    return nodemailer.createTransport({
      host: smtpHost,
      port: smtpPort,
      secure: smtpPort === 465, // true untuk port 465, false untuk port lainnya
      auth: {
        user: smtpUser,
        pass: smtpPass
      }
    });
  }

  // Fallback: Menggunakan log jika kredensial tidak disediakan
  functions.logger.warn("Transporter SMTP tidak memiliki kredensial lengkap. Menjalankan fallback pengiriman email virtual/log.");
  return null;
};

/**
 * Trigger otomatis berjalan setiap kali pengguna BARU mendaftar
 * melalui Google Auth (Frictionless Login).
 */
export const createNewUserProfile = functions
  .region("asia-southeast2") // Server Jakarta (asia-southeast2)
  .auth.user().onCreate(async (user) => {
    
    functions.logger.log(`Mendaftarkan pengguna baru via Google Auth: ${user.uid}`);

    // 1. Tentukan referensi dokumen di koleksi 'users'
    const userRef = db.collection("users").doc(user.uid);

    // 2. Susun data default yang SINKRON dengan model FarmerProfile Kotlin
    // Data yang belum diketahui (seperti luas lahan) diisi dengan nilai bawaan (default)
    const newFarmerProfile = {
      id: user.uid,
      name: user.displayName || "Petani ASPEK", // Otomatis ditarik dari akun Google
      email: user.email || null,
      cropType: "Belum Diatur", // Akan diisi petani nanti di dalam aplikasi
      landArea: 0.0,            // Default luas lahan (Double di Kotlin)
      isErdkkRegistered: false, // Default status e-RDKK (Boolean di Kotlin)
      
      // Metadata tambahan untuk sistem & UI
      photoURL: user.photoURL || null,
      subscription: "free",
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    };

    // 3. Simpan data profil ke Firestore
    try {
      await userRef.set(newFarmerProfile);
      functions.logger.log(`FarmerProfile berhasil diukir di database untuk: ${user.uid}`);
      
      // 4. Kirim email pemberitahuan no-reply otomatis ketika berhasil
      if (newFarmerProfile.email) {
        const mailOptions = {
          from: '"ASPEK AI No-Reply" <no-reply@asistenpetani-ai.id>',
          to: newFarmerProfile.email,
          subject: "🌾 Selamat Bergabung di Asisten Petani Kita AI! (ASPEK)",
          html: `
            <div style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
              <div style="background-color: #2e7d32; padding: 20px; text-align: center; color: white;">
                <h1 style="margin: 0; font-size: 24px;">ASPEK - Asisten Petani Kita AI</h1>
              </div>
              <div style="padding: 20px;">
                <p>Halo, <strong>${newFarmerProfile.name}</strong>!</p>
                <p>Selamat bergabung di platform kecerdasan buatan nasional tepercaya untuk membimbing para petani Indonesia!</p>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;" />
                
                <h3 style="color: #2e7d32; margin-top: 0;">📋 Ringkasan Profil Pengguna Anda</h3>
                <table style="width: 100%; border-collapse: collapse;">
                  <tr>
                    <td style="padding: 6px 0; color: #666; width: 140px;">ID Pengguna</td>
                    <td style="padding: 6px 0; font-weight: bold;">: ${newFarmerProfile.id}</td>
                  </tr>
                  <tr>
                    <td style="padding: 6px 0; color: #666;">Nama Lengkap</td>
                    <td style="padding: 6px 0; font-weight: bold;">: ${newFarmerProfile.name}</td>
                  </tr>
                  <tr>
                    <td style="padding: 6px 0; color: #666;">Surel (Email)</td>
                    <td style="padding: 6px 0; font-weight: bold;">: ${newFarmerProfile.email}</td>
                  </tr>
                  <tr>
                    <td style="padding: 6px 0; color: #666;">Status e-RDKK</td>
                    <td style="padding: 6px 0; font-weight: bold; color: #e65100;">: Belum Terverifikasi / Evaluasi</td>
                  </tr>
                </table>
                
                <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;" />
                
                <p>Asisten AI Anda kini siap melayani pencarian subsidi pupuk presisi e-RDKK, konsultasi hama tanaman, rekomendasi cuaca, dan rute pembeli hasil bumi terdekat langsung melalui telepon genggam Anda.</p>
                <p style="background-color: #f9f9f9; padding: 12px; border-left: 4px solid #2e7d32; font-style: italic; font-size: 14px; margin: 15px 0;">
                  "Membantu meningkatkan produktivitas komoditas kelapa, padi, nilam, jagung, tebu, dan kakao menuju kedaulatan pangan berkelanjutan."
                </p>
                <p style="font-size: 13px; color: #888; margin-top: 25px;">
                  <em>*Email ini dikirim secara otomatis oleh sistem kami. Mohon tidak membalas email ini karena alamat ini tidak dipantau (no-reply).</em>
                </p>
              </div>
              <div style="background-color: #f1f1f1; padding: 15px; text-align: center; font-size: 12px; color: #777; border-top: 1px solid #eee;">
                &copy; 2026 ASPEK AI - Kabupaten Kolaka, Sulawesi Tenggara. Semua Hak Cipta Dilindungi.
              </div>
            </div>
          `
        };

        const transporter = getTransporter();
        if (transporter) {
          await transporter.sendMail(mailOptions);
          functions.logger.log(`Surel sambutan bermotif no-reply sukses melompat ke: ${newFarmerProfile.email}`);
        } else {
          functions.logger.log(`[SIMULATION LOG] Email No-Reply terkirim sukses tanpa SMTP transporter ke: ${newFarmerProfile.email}`);
          functions.logger.log(`Isi Pesan: ${JSON.stringify(mailOptions.subject)}`);
        }
      }

      return;
    } catch (error) {
      functions.logger.error(`Gagal membuat profil atau mengirim no-reply untuk ${user.uid}`, error);
      return;
    }
  });
