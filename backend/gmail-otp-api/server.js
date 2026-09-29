import "dotenv/config";
import express from "express";
import nodemailer from "nodemailer";
import crypto from "crypto";

const app = express();
app.use(express.json());

const port = Number(process.env.PORT || 3000);
const otpStore = new Map();

if (!process.env.GMAIL_USER || !process.env.GMAIL_APP_PASSWORD) {
  console.warn("GMAIL_USER / GMAIL_APP_PASSWORD belum diisi.");
}

const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.GMAIL_USER,
    pass: process.env.GMAIL_APP_PASSWORD
  }
});

function createOtp() {
  return crypto.randomInt(100000, 1000000).toString();
}

app.get("/health", (_req, res) => {
  res.json({ success: true, service: "gmail-otp-api" });
});

app.post("/api/auth/send-otp", async (req, res) => {
  try {
    const email = String(req.body.email || "").trim().toLowerCase();
    const username = String(req.body.username || "").trim();
    const type = String(req.body.type || "register").trim();

    if (!email || !email.includes("@")) {
      return res.status(400).json({
        success: false,
        error: "Email tidak valid"
      });
    }

    const otp = createOtp();

    otpStore.set(`${type}:${email}`, {
      otp,
      username,
      expiresAt: Date.now() + 5 * 60 * 1000
    });

    await transporter.sendMail({
      from: `"ALFAA XITER" <${process.env.GMAIL_USER}>`,
      to: email,
      subject: "Kode Verifikasi ALFAA XITER",
      text:
        `Halo${username ? ` ${username}` : ""},\\n\\n` +
        `Kode verifikasi kamu adalah: ${otp}\\n\\n` +
        "Kode berlaku selama 5 menit.\\n" +
        "Jika kamu tidak meminta kode ini, abaikan email ini."
    });

    return res.json({
      success: true,
      message: "Kode verifikasi telah dikirim ke email."
    });
  } catch (error) {
    console.error("send-otp:", error);
    return res.status(500).json({
      success: false,
      error: "Gagal mengirim email"
    });
  }
});

app.post("/api/auth/verify-otp", (req, res) => {
  const email = String(req.body.email || "").trim().toLowerCase();
  const type = String(req.body.type || "register").trim();
  const code = String(req.body.code || "").trim();

  const key = `${type}:${email}`;
  const record = otpStore.get(key);

  if (!record || Date.now() > record.expiresAt) {
    otpStore.delete(key);
    return res.status(400).json({
      success: false,
      error: "Kode OTP sudah kedaluwarsa atau tidak ditemukan"
    });
  }

  if (record.otp !== code) {
    return res.status(400).json({
      success: false,
      error: "Kode OTP salah"
    });
  }

  otpStore.delete(key);

  return res.json({
    success: true,
    message: "OTP valid"
  });
});

app.listen(port, () => {
  console.log(`Gmail OTP API berjalan di port ${port}`);
});
