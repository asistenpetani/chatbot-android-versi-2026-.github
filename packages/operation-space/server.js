const express = require('express');
const cors = require('cors');
const { GoogleGenerativeAI } = require('@google/generative-ai');
const admin = require('firebase-admin');
const { getSystemPrompt } = require('ai-knowledge-space');

// Inisialisasi Firebase Admin (Auth & Firestore)
// Catatan: Di Cloud Run / Google Cloud, ini akan otomatis menggunakan Default Service Account
if (!admin.apps.length) {
    admin.initializeApp();
}

const app = express();
const port = process.env.PORT || 8080;

// Middleware
app.use(express.json({ limit: '50mb' })); // Limit ditingkatkan untuk gambar resolusi tinggi
app.use(cors());

// Inisialisasi Gemini SDK
const genAI = new GoogleGenerativeAI(process.env.GEMINI_API_KEY || "DUMMY_KEY");

// Middleware Autentikasi untuk memverifikasi request dari Aplikasi Android
async function verifyAuth(req, res, next) {
    const authHeader = req.headers.authorization || '';
    if (!authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ success: false, reply: 'Autentikasi ditolak. Header tidak valid.' });
    }
    const idToken = authHeader.split('Bearer ')[1];
    
    // Bypass untuk mode Development (Dummy Token dari Android)
    if (idToken === 'DUMMY_TOKEN') {
        req.user = { uid: 'dummy_user_123', isVip: true }; // Anggap VIP untuk testing
        return next();
    }

    try {
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        req.user = decodedToken;
        
        // Cek status VIP di Firestore Database
        // const userDoc = await admin.firestore().collection('users').doc(decodedToken.uid).get();
        // req.user.isVip = userDoc.data()?.isVip || false;
        req.user.isVip = false; // Default sementara

        next();
    } catch (error) {
        console.error('Verifikasi Token Gagal:', error);
        return res.status(401).json({ success: false, reply: 'Sesi tidak valid atau kadaluarsa.' });
    }
}

// Endpoint 1: Health & Quota Check
app.get('/api/check-quota', verifyAuth, (req, res) => {
    res.json({ success: true, reply: 'Quota OK. Sistem siap melayani.', isVip: req.user?.isVip });
});

// Endpoint 2: Chat Assistant (Daya Ingat Berbasis History dari Android)
app.post('/api/chat', verifyAuth, async (req, res) => {
    try {
        const { message, history } = req.body;
        
        // Logika Migrasi (isVip)
        const isVip = req.user?.isVip || false;
        const modelName = isVip ? "gemini-2.0-flash-exp" : "gemini-1.5-flash"; // Model bervariasi tergantung plan
        
        const model = genAI.getGenerativeModel({ 
            model: modelName,
            systemInstruction: getSystemPrompt('default')
        });

        // Mapping history Android ke format Gemini SDK Server
        const formattedHistory = (history || []).map(msg => {
            return {
                role: msg.role === 'user' ? 'user' : 'model',
                parts: [{ text: msg.parts?.[0]?.text || '' }]
            };
        });

        const chat = model.startChat({
            history: formattedHistory
        });

        const result = await chat.sendMessage(message);
        res.json({ success: true, reply: result.response.text() });
    } catch (err) {
        console.error("Kesalahan Chat Engine:", err);
        res.status(500).json({ success: false, reply: 'Maaf, otak AI sedang sibuk. ' + err.message });
    }
});

// Endpoint 3: Multimodal Image Analysis (Penyakit Tanaman / Rekomendasi)
app.post('/api/analyze', verifyAuth, async (req, res) => {
    try {
        const { image, mimeType, prompt } = req.body;
        
        const isVip = req.user?.isVip || false;
        const modelName = isVip ? "gemini-2.0-flash-exp" : "gemini-1.5-flash";
        
        const model = genAI.getGenerativeModel({ 
            model: modelName,
            systemInstruction: getSystemPrompt('diagnosis')
        });

        const imageParts = [
            {
                inlineData: {
                    data: image,
                    mimeType: mimeType || 'image/jpeg'
                }
            }
        ];

        const result = await model.generateContent([prompt, ...imageParts]);
        res.json({ success: true, reply: result.response.text() });
    } catch (err) {
        console.error("Kesalahan Image Analysis:", err);
        res.status(500).json({ success: false, reply: 'Gagal menganalisis gambar. ' + err.message });
    }
});

// Endpoint Fallback / Status
app.get('/api/status', (req, res) => {
  res.json({
    status: 'online',
    message: 'Ruang Operasi (Backend Express) is running smoothly.',
    timestamp: new Date()
  });
});

app.listen(port, () => {
  console.log(`Backend server listening on port ${port}`);
});
