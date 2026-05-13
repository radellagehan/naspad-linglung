# ♦ MinangLuxe Tender System

**Platform Tender Premium — Dari Minang, Untuk Dunia**

Sistem tender modern berbasis Java Swing dengan tema dark gold premium.

---

## Fitur Lengkap

| Fitur | Deskripsi |
|---|---|
| **Buyer Panel** | Chat bubble interface untuk posting permintaan tender |
| **Smart Parser** | Parsing otomatis: `"rendang 2 gulai 1"` → `{rendang:2, gulai:1}` |
| **Seller Panel** | 10 lapau penjual dengan form input penawaran |
| **AI Ranking** | Penawaran diurutkan berdasarkan score `(rating / harga) × 10.000` |
| **Rekomendasi** | Panel tengah menampilkan penawaran terbaik per penjual |
| **Keranjang** | Add/remove item, tampil total harga real-time |
| **Checkout** | Bayar dan simpan ke riwayat pesanan |
| **Riwayat Pesanan** | Kartu per order lengkap dengan tombol "Hubungi Penjual" |
| **Info Penjual** | Dialog kontak + live map preview (OpenStreetMap) |
| **WhatsApp** | Tombol langsung buka chat WA penjual |

---

## Cara Menjalankan

### Prasyarat
- **Java JDK 17+** — [Download](https://adoptium.net)
- **Maven 3.x** — [Download](https://maven.apache.org/download.cgi)

### Linux / Mac
```bash
chmod +x run.sh
./run.sh
```

### Windows
```
run.bat
```

### Manual
```bash
mvn package -DskipTests
java -jar target/minangluxe.jar
```

---

## Cara Pakai

1. **Buyer** (kolom kiri): Ketik pesanan seperti `"rendang 2 gulai 1 es teh 3"` → klik **Kirim Tender**
2. **Seller** (kolom kanan): Pilih permintaan dari daftar, isi produk/harga/rating → **Kirim Penawaran**
3. **Rekomendasi** (kolom tengah): Pilih paket atau item individual → masuk **Keranjang**
4. Isi **alamat pengiriman** → klik **Bayar Sekarang**
5. Cek **Riwayat Pesanan** di tab kedua

---

## Arsitektur OOP

- **Singleton** — `TenderController` satu instance untuk seluruh sesi
- **Observer** — `TenderListener` interface untuk update UI real-time
- **MVC** — Model (`Buyer`, `Seller`, `Offer`, `Payment`), View (Swing panels), Controller (`TenderController`)
- **Mediator** — `TenderController` sebagai perantara Buyer ↔ Seller

---

*MinangLuxe © 2024 — Dark Gold Edition*
