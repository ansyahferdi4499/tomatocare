package com.example.pepey

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pepey.databinding.FragmentInformasiBinding

class InformasiFragment : Fragment() {

    private var _binding: FragmentInformasiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInformasiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Data daftar penyakit kulit dengan gambar, judul, dan deskripsi
        val penyakitList = listOf(
            PenyakitKulit(
                "Ringworm",
                "Infeksi jamur yang menyebabkan gatal dan rambut rontok.",
                R.drawable.images,
                getString(R.string.detail_ringworm) // Penjelasan panjang diambil dari resources string
            ),
            PenyakitKulit(
                "Dermatitis Alergi",
                "Alergi yang menyebabkan gatal parah dan peradangan kulit.",
                R.drawable.images,
                "Penjelasan lengkap tentang dermatitis alergi."
            )
        )

        // Setup RecyclerView menggunakan binding
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = PenyakitKulitAdapter(penyakitList) { penyakit ->
            // Ketika item diklik, buka halaman detail
            val intent = Intent(requireContext(), DetailPenyakitActivity::class.java).apply {
                putExtra("judul", penyakit.title)
                putExtra("deskripsi", penyakit.description)
                putExtra("gambar", penyakit.imageResId)
                putExtra("penjelasan", penyakit.penjelasanLengkap) // Kirim penjelasan lengkap
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
