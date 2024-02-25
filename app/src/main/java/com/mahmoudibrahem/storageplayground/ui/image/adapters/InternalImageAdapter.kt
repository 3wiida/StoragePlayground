package com.mahmoudibrahem.storageplayground.ui.image.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mahmoudibrahem.storageplayground.databinding.ImageItemLayoutBinding
import com.mahmoudibrahem.storageplayground.model.InternalImage

class InternalImageAdapter :
    ListAdapter<InternalImage, InternalImageAdapter.ItemViewHolder>(Comparator) {

     var onImageLongClick: ((InternalImage) -> Unit)? = null

    inner class ItemViewHolder(private val binding: ImageItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InternalImage) {
            binding.imageIv.setImageBitmap(item.bitmap)
            binding.imageIv.setOnLongClickListener {
                onImageLongClick?.invoke(item)
                false
            }
        }
    }

    private object Comparator : DiffUtil.ItemCallback<InternalImage>() {
        override fun areItemsTheSame(oldItem: InternalImage, newItem: InternalImage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: InternalImage, newItem: InternalImage): Boolean {
            return oldItem.name == newItem.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = ImageItemLayoutBinding.inflate(LayoutInflater.from(parent.context))
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }
}