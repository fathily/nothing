# Gmail OTP API

Backend kecil untuk mengirim OTP lewat Gmail menggunakan Nodemailer.

## Setup

1. Salin `.env.example` menjadi `.env`.
2. Isi:
   - `GMAIL_USER` = alamat Gmail pengirim.
   - `GMAIL_APP_PASSWORD` = App Password Gmail 16 karakter.
3. Jalankan:
   `npm install`
4. Jalankan:
   `npm start`

## Endpoint

- `GET /health`
- `POST /api/auth/send-otp`
- `POST /api/auth/verify-otp`

Jangan pernah memasukkan password Gmail biasa atau App Password ke APK atau repository Git.
