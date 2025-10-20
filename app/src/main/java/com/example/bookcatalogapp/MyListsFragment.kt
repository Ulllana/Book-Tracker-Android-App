package com.example.bookcatalogapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookcatalogapp.adapters.ListsAdapter
import com.example.bookcatalogapp.databinding.FragmentMyListsBinding

class MyListsFragment : Fragment() {

    private var _binding: FragmentMyListsBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var listsAdapter: ListsAdapter
    private lateinit var username: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        val sharedPref = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        username = sharedPref.getString("logged_username", "") ?: ""

        listsAdapter = ListsAdapter { listItem ->
            val bundle = Bundle().apply { putLong("list_id", listItem.id) }
            val fragment = BookListFragment()
            fragment.arguments = bundle
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.listsRecyclerView.adapter = listsAdapter
        binding.listsRecyclerView.layoutManager = LinearLayoutManager(context)

        loadLists()

        binding.createButton.setOnClickListener {
            val name = binding.editListName.text.toString()
            if (name.isNotEmpty()) {
                val rowId = databaseHelper.insertList(name, username)
                if (rowId > 0) {
                    Toast.makeText(requireContext(), "List created", Toast.LENGTH_SHORT).show()
                    loadLists()
                    binding.editListName.text.clear()
                } else {
                    Toast.makeText(requireContext(), "Failed to create list", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadLists() {
        val lists = databaseHelper.getUserLists(username)
        listsAdapter.submitList(lists)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}