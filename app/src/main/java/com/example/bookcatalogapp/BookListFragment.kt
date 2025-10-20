package com.example.bookcatalogapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookcatalogapp.adapters.BooksAdapter
import com.example.bookcatalogapp.databinding.FragmentBookListBinding
import com.example.bookcatalogapp.models.VolumeItem

class BookListFragment : Fragment() {

    private var _binding: FragmentBookListBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var username: String
    private var listId: Long = -1
    private lateinit var adapter: BooksAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBookListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username = sharedPref.getString("logged_username", "") ?: ""
        listId = arguments?.getLong("list_id") ?: return

        loadBooks()
    }

    private fun loadBooks() {
        val books = databaseHelper.getBooksInList(listId, username)
        val favListId = databaseHelper.getListId(username, "Favorites") ?: return
        val favoriteMap = mutableMapOf<String, Boolean>()
        books.forEach { book ->
            favoriteMap[book.id] = databaseHelper.isBookInList(favListId, book.id)
        }

        val adapter = BooksAdapter(
            favoriteStatus = favoriteMap,
            onFavoriteToggle = { book, newFavorite ->
                if (newFavorite) {
                    databaseHelper.insertBookToList(book, "Favorites", username)
                } else {
                    databaseHelper.removeBookFromList(favListId, book.id)
                }
                favoriteMap[book.id] = newFavorite
                val position = books.indexOf(book)
                if (position != -1) {
                    adapter.notifyItemChanged(position) // Line 60, potential issue
                }
            }
        )
        binding.booksRecyclerView.adapter = adapter
        binding.booksRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter.submitList(books)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}