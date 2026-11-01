# TomatoCare

TomatoCare adalah aplikasi Android untuk membantu pengguna mengamati kondisi daun tomat. Aplikasi menggunakan model TensorFlow Lite yang disimpan di perangkat untuk menganalisis gambar dari kamera, lalu menyediakan informasi gejala dan penanganan awal.

> Hasil pemeriksaan hanya merupakan panduan awal, bukan diagnosis pasti. Amati kondisi tanaman secara menyeluruh dan konsultasikan dengan penyuluh pertanian jika gejala berlanjut.

## Fitur

- Memeriksa daun tomat melalui kamera secara langsung.
- Menampilkan kelas kondisi yang dikenali dan tingkat keyakinan model.
- Menyediakan panduan gejala dan penanganan awal untuk:
  - Busuk Kering - Leaf Mold
  - Bercak Coklat - Septoria
  - Bercak Coklat - Early Blight
  - Daun Tomat Sehat
  - Busuk Basah - Late Blight
- Menyediakan halaman informasi aplikasi dan profil pengembang.

## Teknologi

- Kotlin dan Android SDK
- Jetpack Compose untuk beranda
- AndroidX dan View Binding untuk layar lainnya
- CameraX untuk akses kamera
- TensorFlow Lite untuk inferensi model di perangkat

## Persyaratan

- Android Studio dengan Android SDK 34
- JDK 17
- Perangkat Android atau emulator dengan kamera
- Koneksi internet saat Gradle pertama kali mengunduh plugin dan dependensi

Aplikasi mendukung Android API 23 (Android 6.0) ke atas. Izin kamera diperlukan untuk menjalankan pemeriksaan langsung.

## Menjalankan aplikasi

1. Clone repositori dan buka folder proyek di Android Studio.
2. Tunggu proses Gradle Sync selesai.
3. Hubungkan perangkat Android atau jalankan emulator yang memiliki kamera.
4. Pilih konfigurasi `app`, lalu tekan **Run**.
5. Izinkan akses kamera ketika diminta.

Untuk membangun APK debug dari terminal:

```bash
# Windows
.\gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

APK debug tersedia di `app/build/outputs/apk/debug/app-debug.apk`.

## Pengujian

Jalankan unit test dengan:

```bash
# Windows
.\gradlew.bat test

# macOS / Linux
./gradlew test
```

## Struktur proyek

```text
app/
├── src/main/
│   ├── assets/       # Model TensorFlow Lite
│   ├── java/         # Activity, logika kamera, dan klasifikasi
│   └── res/          # Layout, gambar, font, dan teks aplikasi
└── build.gradle.kts  # Konfigurasi modul Android dan dependensi
```

Model yang digunakan untuk pemeriksaan kamera berada di `app/src/main/assets/tomat_effnet_raw.tflite`.
