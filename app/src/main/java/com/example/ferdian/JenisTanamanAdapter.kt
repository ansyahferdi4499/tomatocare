package com.example.ferdian

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.ferdian.databinding.ItemCardBinding

class JenisTanamanAdapter(
    private val penyakitList: List<JenisTanaman>,
    private val onClick: (JenisTanaman) -> Unit // Tambahkan parameter onClick
) : RecyclerView.Adapter<JenisTanamanAdapter.PenyakitViewHolder>() {

    inner class PenyakitViewHolder(val binding: ItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PenyakitViewHolder {
        val binding = ItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PenyakitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PenyakitViewHolder, position: Int) {
        val penyakit = penyakitList[position]
        holder.binding.tvTitle.text = penyakit.title
        holder.binding.tvDescription.text = penyakit.description

        // Set onClickListener untuk memanggil fungsi onClick yang diberikan dari fragment
        holder.binding.root.setOnClickListener {
            onClick(penyakit) // Panggil fungsi onClick dengan item penyakit
        }
    }

    override fun getItemCount(): Int = penyakitList.size
}
