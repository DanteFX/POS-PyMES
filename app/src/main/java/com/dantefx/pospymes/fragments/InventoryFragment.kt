package com.dantefx.pospymes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantefx.pospymes.R
import com.dantefx.pospymes.adapter.ProductAdapter
import com.dantefx.pospymes.model.Product
import com.dantefx.pospymes.viewmodel.ProductViewModel
import com.dantefx.pospymes.viewmodel.SharedViewModel
import com.dantefx.pospymes.viewmodel.SharedViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class InventoryFragment : Fragment() {

    private val productViewModel: ProductViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this, SharedViewModelFactory(productViewModel)).get(SharedViewModel::class.java)
    }
    //private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        val searchView = view.findViewById<SearchView>(R.id.searchView)

        adapter = ProductAdapter(
            onEditClick = { product -> editProduct(product) },
            onDeleteClick = { product -> deleteProduct(product) }
        )

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        productViewModel.allProducts.observe(viewLifecycleOwner) { products ->
            products?.let { adapter.submitList(it) }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterProducts(newText)
                return true
            }
        })

        fab.setOnClickListener {

            findNavController().navigate(R.id.productRecognitionFragment)
            sharedViewModel.setMode(SharedViewModel.Mode.INVENTORY) // Cambiar a modo "Inventario"
        }
        sharedViewModel.currentMode.observe(viewLifecycleOwner) { mode ->
            println("Current mode: $mode") // Log para verificar si el modo cambia
        }


        return view
    }

    private fun filterProducts(query: String?) {
        val filteredList = productViewModel.allProducts.value?.filter {
            it.name.contains(query ?: "", ignoreCase = true)
        }
        adapter.submitList(filteredList)
    }

    private fun editProduct(product: Product) {
        val dialog = EditProductDialogFragment(product) { updatedProduct ->
            productViewModel.updateProduct(updatedProduct)
            Toast.makeText(requireContext(), "${updatedProduct.name} actualizado", Toast.LENGTH_SHORT).show()
        }
        dialog.show(parentFragmentManager, "EditProductDialog")
    }

    private fun deleteProduct(product: Product) {
        productViewModel.deleteProduct(product)
        Toast.makeText(requireContext(), "${product.name} eliminado", Toast.LENGTH_SHORT).show()
    }
}
