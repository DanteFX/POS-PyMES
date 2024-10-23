package com.dantefx.pospymes.fragments
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.dantefx.pospymes.R
import com.dantefx.pospymes.model.Product
import com.google.android.material.button.MaterialButton

class EditProductDialogFragment(
    private val product: Product,
    private val onSave: (Product) -> Unit
) : DialogFragment() {

    private lateinit var nameEditText: EditText
    private lateinit var priceEditText: EditText
    private lateinit var quantityEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_product_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nameEditText = view.findViewById(R.id.editProductName)
        priceEditText = view.findViewById(R.id.editProductPrice)
        quantityEditText = view.findViewById(R.id.editProductQuantity)

        nameEditText.setText(product.name)
        priceEditText.setText(product.price.toString())
        quantityEditText.setText(product.quantity.toString())

        view.findViewById<MaterialButton>(R.id.saveButton).setOnClickListener {
            val updatedProduct = product.copy(
                name = nameEditText.text.toString(),
                price = priceEditText.text.toString().toDoubleOrNull() ?: product.price,
                quantity = quantityEditText.text.toString().toIntOrNull() ?: product.quantity
            )
            onSave(updatedProduct)
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.cancelButton).setOnClickListener {
            dismiss()
        }
    }
}
