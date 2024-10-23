package com.dantefx.pospymes.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.dantefx.pospymes.databinding.FragmentProductRecognitionBinding
import com.dantefx.pospymes.model.Product
import com.dantefx.pospymes.viewmodel.ProductViewModel
import com.dantefx.pospymes.viewmodel.SharedViewModel
import com.dantefx.pospymes.viewmodel.SharedViewModelFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ProductRecognitionFragment : Fragment() {

    private var _binding: FragmentProductRecognitionBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageCapture: ImageCapture
    //private val sharedViewModel: SharedViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by viewModels()

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this, SharedViewModelFactory(productViewModel)).get(SharedViewModel::class.java)
    }

    private var isProcessing = false

    // Inicializar TextRecognizer
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        val mode = arguments?.getString("MODE")

        startCamera()

        binding.captureButton.setOnClickListener {
            if (!isProcessing) {
                isProcessing = true
                takePhoto()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Error al iniciar la cámara", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                processImageProxyForTextRecognition(imageProxy)
            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(requireContext(), "Error al capturar la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
                isProcessing = false
            }
        })
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxyForTextRecognition(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.textBlocks.joinToString(" ") { it.text }
                    if (recognizedText.isNotEmpty()) {
                        val currentMode = sharedViewModel.currentMode.value

                        if (currentMode == SharedViewModel.Mode.SALE) {
                            val productFromInventory = sharedViewModel.findProductInInventory(recognizedText)

                            if (productFromInventory != null) {
                                sharedViewModel.addProductToSale(productFromInventory)
                                Toast.makeText(requireContext(), "Producto añadido a la venta: ${productFromInventory.name}", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(requireContext(), "Producto no encontrado en el inventario", Toast.LENGTH_SHORT).show()
                            }
                        } else if (currentMode == SharedViewModel.Mode.INVENTORY) {
                            // Si estamos en modo inventario, agregar el producto al inventario
                            val newProduct = Product(name = recognizedText, price = 0.0, quantity = 1)
                            sharedViewModel.addProductToInventory(newProduct)
                            Toast.makeText(requireContext(), "Producto añadido al inventario: $recognizedText", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "No se reconoció ningún texto", Toast.LENGTH_SHORT).show()
                    }
                    isProcessing = false
                    imageProxy.close()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error en el reconocimiento de texto", Toast.LENGTH_SHORT).show()
                    isProcessing = false
                    imageProxy.close()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }
}
