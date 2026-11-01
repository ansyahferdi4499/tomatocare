package com.example.ferdian

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ferdian.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            // Inflate layout dengan ViewBinding
            binding = ActivityDetailBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Ambil data dari Intent
            val judul = intent.getStringExtra("judul") ?: "Judul tidak tersedia"
            val deskripsi = intent.getStringExtra("deskripsi") ?: "Deskripsi tidak tersedia"
            val gambar = intent.getIntExtra("gambar", R.drawable.ic_launcher_foreground)
            val penjelasan = intent.getStringExtra("penjelasan") ?: "Penjelasan tidak tersedia" // Ambil penjelasan yang dikirim

            // Set data ke layout
            binding.textViewJudul.text = judul
            binding.textViewDeskripsi.text = deskripsi
            binding.imageViewPenyakit.setImageResource(gambar)
            binding.root.findViewById<android.widget.ImageView>(R.id.iv_back)
                .setOnClickListener { finish() }

            // Tampilkan data penjelasan di WebView
            binding.webViewPenjelasan.settings.javaScriptEnabled = true
            binding.webViewPenjelasan.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            val styledExplanation = """
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <style>
                        body { font-family: sans-serif; color: #748078; font-size: 16px; line-height: 1.6; margin: 0; }
                        p { margin: 0 0 16px; }
                        b, strong { color: #263D33; font-weight: 600; }
                    </style>
                </head>
                <body>$penjelasan</body>
                </html>
            """.trimIndent()
            binding.webViewPenjelasan.loadDataWithBaseURL(
                null,
                styledExplanation,
                "text/html",
                "UTF-8",
                null
            )


        } catch (e: Exception) {
            Log.e("DetailActivity", "Error in onCreate", e)
            Toast.makeText(this, "Terjadi kesalahan saat memuat detail", Toast.LENGTH_SHORT).show()
        }
    }
}
