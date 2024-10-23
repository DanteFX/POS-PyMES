package com.dantefx.pospymes.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dantefx.pospymes.R
import com.dantefx.pospymes.model.Product

class SaleAdapter(
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, SaleAdapter.SaleViewHolder>(ProductsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sale, parent, false)
        return SaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: SaleViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
        holder.deleteButton.setOnClickListener {
            onDeleteClick(product)
        }
    }

    class SaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productNameView: TextView = itemView.findViewById(R.id.productName)
        private val productPriceView: TextView = itemView.findViewById(R.id.productPrice)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(product: Product) {
            productNameView.text = product.name
            productPriceView.text = "$${product.price}"
        }
    }

    class ProductsComparator : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
}
