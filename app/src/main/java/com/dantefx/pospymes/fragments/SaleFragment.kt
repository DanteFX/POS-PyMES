package com.dantefx.pospymes.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dantefx.pospymes.R
import com.dantefx.pospymes.adapter.SaleAdapter
import com.dantefx.pospymes.viewmodel.ProductViewModel
import com.dantefx.pospymes.viewmodel.SharedViewModel
import com.dantefx.pospymes.viewmodel.SharedViewModelFactory
import com.google.android.material.button.MaterialButton

class SaleFragment : Fragment() {

   // private val sharedViewModel: SharedViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by viewModels()
 /*   private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this, SharedViewModelFactory(productViewModel))[SharedViewModel::class.java]
    }*/
 private val sharedViewModel: SharedViewModel by activityViewModels {
     SharedViewModelFactory(productViewModel)
 }

    private lateinit var totalPriceTextView: TextView
    private lateinit var adapter: SaleAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        sharedViewModel.setMode(SharedViewModel.Mode.SALE)
        val view = inflater.inflate(R.layout.fragment_sale, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewSales)
        val completeSaleButton = view.findViewById<MaterialButton>(R.id.completeSaleButton)
        val scanProductButton = view.findViewById<MaterialButton>(R.id.scanProductButton)
        totalPriceTextView = view.findViewById(R.id.totalPriceTextView)

        adapter = SaleAdapter { product ->
            sharedViewModel.removeProductFromSale(product)
        }

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        sharedViewModel.scannedProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products.toList())
            adapter.notifyDataSetChanged() // Actualiza el RecyclerView
        }

        sharedViewModel.totalPrice.observe(viewLifecycleOwner) { totalPrice ->
            totalPriceTextView.text = "Total: $${String.format("%.2f", totalPrice)}"
        }

        completeSaleButton.setOnClickListener {
            confirmSale()
        }

        scanProductButton.setOnClickListener {

            findNavController().navigate(R.id.productRecognitionFragment)
        }
        sharedViewModel.currentMode.observe(viewLifecycleOwner) { mode ->
            println("Current mode sale: $mode")
        }



        return view
    }
    private fun confirmSale() {
        sharedViewModel.scannedProducts.observe(viewLifecycleOwner) { products ->
            for (product in products) {
                product.quantity = product.quantity - 1
                sharedViewModel.updateProductInInventory(product)
                sharedViewModel.removeProductFromSale(product)
            }

            adapter.notifyDataSetChanged()
            Toast.makeText(requireContext(), "Venta completada", Toast.LENGTH_SHORT).show()
        }
    }
}
