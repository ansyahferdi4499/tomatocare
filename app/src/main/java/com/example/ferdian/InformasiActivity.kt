package com.example.ferdian

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ferdian.databinding.ActivityInformasiBinding

class InformasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInformasiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi binding menggunakan ViewBinding
        binding = ActivityInformasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.findViewById<android.widget.TextView>(R.id.tvDiseaseListTitle)
            .text = getString(R.string.disease_list_title)
        binding.root.findViewById<android.widget.ImageView>(R.id.ivBack)
            .setOnClickListener { finish() }

        val penyakitList = listOf(
            JenisTanaman(
                "Busuk Kering - Leaf Mold",
                getString(R.string.leaf_mold_description),
                R.drawable.icon_tomato_leaf,
                getString(R.string.leaf_mold_detail)
            ),
            JenisTanaman(
                "Bercak Coklat - Septoria",
                getString(R.string.septoria_description),
                R.drawable.icon_tomato_leaf,
                getString(R.string.septoria_detail)
            ),
            JenisTanaman(
                "Bercak Coklat - Early Blight",
                getString(R.string.early_blight_description),
                R.drawable.icon_tomato_leaf,
                getString(R.string.early_blight_detail)
            ),
            JenisTanaman(
                "Daun Tomat Sehat",
                getString(R.string.healthy_leaf_description),
                R.drawable.icon_tomato_leaf,
                getString(R.string.healthy_leaf_detail)
            ),
            JenisTanaman(
                "Busuk Basah - Late Blight",
                getString(R.string.late_blight_description),
                R.drawable.icon_tomato_leaf,
                getString(R.string.late_blight_detail)
            )
        )

        // Mengatur adapter RecyclerView
        val adapter = JenisTanamanAdapter(penyakitList) { penyakit ->
            // Menggunakan Intent untuk mengirim data ke DetailActivity
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("judul", penyakit.title)
                putExtra("deskripsi", penyakit.description)
                putExtra("gambar", penyakit.imageResId)
                putExtra("penjelasan", penyakit.penjelasanLengkap)
            }
            startActivity(intent)
        }

        // Set up RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }
}
