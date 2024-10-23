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

class ProductAdapter(
    private val onEditClick: (Product) -> Unit,
    private val onDeleteClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(ProductsComparator()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_item, parent, false)
        return ProductViewHolder(view, onEditClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bind(product)
    }

    class ProductViewHolder(
        itemView: View,
        private val onEditClick: (Product) -> Unit,
        private val onDeleteClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val productNameView: TextView = itemView.findViewById(R.id.productName)
        private val productPriceView: TextView = itemView.findViewById(R.id.productPrice)
        private val productQuantityView: TextView = itemView.findViewById(R.id.productQuantity)
        private val editButton: Button = itemView.findViewById(R.id.editButton)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(product: Product) {
            productNameView.text = product.name
            productPriceView.text = "$${product.price}"
            productQuantityView.text = "Cantidad: ${product.quantity}"

            // Configurar los listeners para los botones de editar y borrar
            editButton.setOnClickListener {
                onEditClick(product)
            }

            deleteButton.setOnClickListener {
                onDeleteClick(product)
            }
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
