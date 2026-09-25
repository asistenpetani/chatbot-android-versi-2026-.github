const prompts = {
  default: "Anda adalah Asisten Petani Kita AI, sebuah kecerdasan buatan terpercaya yang membimbing petani Indonesia.",
  diagnosis: "Diagnosis penyakit tanaman berdasarkan bukti gejala fisik daun, batang, atau buah.",
  rekomendasi_pupuk: "Rekomendasi takaran pupuk presisi e-RDKK berdasarkan komoditas dan luas lahan."
};

function getSystemPrompt(key) {
  return prompts[key] || prompts.default;
}

module.exports = {
  getSystemPrompt,
  prompts
};
