package com.example.bookcatalogapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.bookcatalogapp.R
import com.example.bookcatalogapp.databinding.ItemBookBinding
import com.example.bookcatalogapp.models.VolumeItem

class BooksAdapter(private val onAddClick: (VolumeItem) -> Unit) :
    ListAdapter<VolumeItem, BooksAdapter.BookViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<VolumeItem>() {
        override fun areItemsTheSame(oldItem: VolumeItem, newItem: VolumeItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: VolumeItem, newItem: VolumeItem): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookViewHolder(private val binding: ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(book: VolumeItem) {
            binding.titleTextView.text = book.volumeInfo.title ?: "Unknown Title"
            binding.authorsTextView.text = book.volumeInfo.authors?.joinToString(", ") ?: "Unknown Authors"
            binding.thumbnailImageView.load(book.volumeInfo.imageLinks?.thumbnail) {
                placeholder(R.drawable.ic_placeholder) // Add a placeholder drawable
                error(R.drawable.ic_error)
            }
            binding.addButton.setOnClickListener {
                onAddClick(book)
            }
        }
    }
}