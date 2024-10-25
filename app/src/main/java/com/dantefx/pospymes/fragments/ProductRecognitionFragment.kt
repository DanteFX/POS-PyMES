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
   // private val sharedViewModel: SharedViewModel by activityViewModels()
    private val productViewModel: ProductViewModel by viewModels()

    /*private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(this, SharedViewModelFactory(productViewModel)).get(SharedViewModel::class.java)
    }*/
    private val sharedViewModel: SharedViewModel by activityViewModels {
        SharedViewModelFactory(productViewModel)
    }


    private var isProcessing = false

    // Inicializar TextRecognizer
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    var currentMode: SharedViewModel.Mode? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedViewModel.currentMode.observe(viewLifecycleOwner) { mode ->
            println("Current mode in recognition: $mode")
        }
        sharedViewModel.currentMode.observe(viewLifecycleOwner) { mode ->
            currentMode = mode
            println("Current mode camera: $mode") // Log para verificar si el modo cambia
        }
        _binding = FragmentProductRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()

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

        // Solo observamos el currentMode una vez y guardamos su valor


        if (mediaImage != null && currentMode != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val recognizedText = visionText.textBlocks.joinToString(" ") { it.text }

                    // Lista de palabras clave (marcas y productos)
                    val keywords = listOf(
                        // Bebidas No Alcohólicas
                        "Coca-Cola",
                        "Pepsi",
                        "Fanta",
                        "Sprite",
                        "7UP",
                        "Dr Pepper",
                        "Mountain Dew",
                        "Lipton",
                        "Arizona",
                        "Jumex",
                        "Boing",
                        "Gatorade",
                        "Powerade",
                        "Manzanita Sol",
                        "Del Valle",
                        "Nescafé",
                        "Café Legal",
                        "V8",
                        "Tonicol",

                        // Bebidas Energizantes
                        "Monster",
                        "Red Bull",
                        "Vive100",
                        "Boost",
                        "Volt",
                        "Burn",
                        "Rockstar",
                        "BANG",
                        "Full Throttle",
                        "AMP Energy",
                        "NOS Energy",
                        "Adrenaline",
                        "Celsius",
                        "XS Energy",

                        // Cervezas y Bebidas Alcohólicas
                        "Corona",
                        "Modelo",
                        "Victoria",
                        "Heineken",
                        "Budweiser",
                        "Tecate",
                        "Sol",
                        "Pacífico",
                        "Amstel",
                        "Stella Artois",
                        "XX Lager",
                        "Negra Modelo",
                        "Coors",
                        "Miller",
                        "Guinness",
                        "Carlsberg",
                        "Blue Moon",
                        "Michelob",

                        // Vinos y Licores
                        "Casillero del Diablo",
                        "Concha y Toro",
                        "Tequila Herradura",
                        "Don Julio",
                        "Jack Daniel's",
                        "José Cuervo",
                        "Bacardí",
                        "Freixenet",
                        "Absolut",
                        "Smirnoff",
                        "Jameson",
                        "Jägermeister",

                        // Cigarros
                        "Marlboro",
                        "Camel",
                        "Winston",
                        "Lucky Strike",
                        "Chesterfield",
                        "Pall Mall",
                        "Faros",
                        "Newport",
                        "Benson & Hedges",
                        "Davidoff",
                        "Salem",
                        "Kool",
                        "Barclay",

                        "Coca-Cola", "Pepsi", "Fanta", "Sprite", "7UP", "Dr Pepper", "Mountain Dew",
                        "Lipton", "Arizona", "Jumex", "Boing", "Gatorade", "Powerade", "Monster",
                        "Red Bull", "V8", "Tonicol", "Manzanita Sol", "Del Valle", "Nescafé", "Café Legal",

                        // Botanas
                        "Lays", "Doritos", "Ruffles", "Cheetos", "Sabritas", "Pringles", "Barcel",
                        "Takis", "Totis", "Churrumais", "Fritos", "Rancheritos", "Chips", "Fritos",
                        "Chicharrones", "Quesabritas", "Tostitos", "Kacang", "Krispy Kreme",

                        // Galletas y Pan
                        "Gamesa", "Marinela", "Principe", "Oreo", "Chokis", "Emperador", "Mamut",
                        "Suavicremas", "Canelitas", "Hershey's", "Gansito", "Choco Roles", "Submarinos",
                        "Pingüinos", "Tía Rosa", "Wonder", "Bimbo", "Nito", "Milano", "Triki Trakes",

                        // Cereales
                        "Kellogg's", "Zucaritas", "Froot Loops", "Choco Krispis", "Corn Flakes",
                        "Special K", "All-Bran", "Quaker", "Granola", "Nesquik", "Cap'n Crunch",
                        "Lucky Charms", "Honey Bunches of Oats", "Fitness", "Trix",

                        // Lácteos
                        "Lala", "Alpura", "Sello Rojo", "Danone", "Philadelphia", "Nido", "Santa Clara",
                        "Yakult", "Chobani", "Nutri Leche", "Cremosa", "Hollandia", "Nestlé",
                        "Clavel", "Cielito Querido", "Lyncott",

                        // Conservas
                        "La Costeña", "Herdez", "Del Monte", "Jumex", "San Marcos", "La Morena",
                        "Doña María", "Valle Verde", "Isadora", "Rosarita", "McCormick", "Mazola",
                        "Heinz", "Hunts", "Kikkoman", "Goya",

                        // Pastas y Arroz
                        "La Moderna", "Barilla", "Roma", "Great Value", "Knorr", "Gallo", "San Antonio",
                        "Riso Scotti", "Vervena", "Tres Estrellas", "Faraon", "Luigi Vitelli", "De Cecco",

                        // Azúcar, Aceites y Harinas
                        "Maseca", "Gamesa", "C&H", "La Fama", "Crisco", "Capullo", "Nutrioli",
                        "Mazola", "Oléico", "Kirkland", "Aladino", "Piloncillo", "Zulka", "Estrella Blanca",

                        // Salsas y Condimentos
                        "Valentina", "Buffalo", "Tabasco", "McCormick", "Doña María", "La Morena",
                        "Herdez", "Búfalo", "Maggi", "Salsas Huichol", "Frank's RedHot", "Ragú",
                        "Prego", "Knorr", "Kraft", "Hellmann's", "Sriracha", "Lala Cremas", "Lea & Perrins",

                        // Carnes y Embutidos
                        "Fud", "San Rafael", "KIR", "Bafar", "Zwan", "La Villita", "Pek", "Lala",
                        "Bistec", "Tofurky", "Oscar Mayer", "Sukarne", "El Corral", "Sabormex", "Tangamanga",

                        // Congelados
                        "La Huerta", "Birds Eye", "McCain", "Granja", "Empanadas", "Freski",
                        "La Villita", "Green Giant", "Kirkland", "Maruchan",

                        // Dulces y Chocolates
                        "Hershey's", "Snickers", "M&M's", "Milky Way", "Crunch", "Ferrero Rocher",
                        "Raffaello", "Kinder", "Carlos V", "Marlboro", "Reese's", "KitKat", "Rockaleta",
                        "Pulparindo", "Lucas", "Mazapán", "De la Rosa", "Ricolino", "Paleta Payaso",

                        // Cuidado personal
                        "Colgate", "Sensodyne", "Oral-B", "Listerine", "Gillette", "Dove",
                        "Palmolive", "Nivea", "Vaseline", "Pantene", "Head & Shoulders", "Always",
                        "Kotex", "Suave", "Neutrogena", "Garnier", "L'Oreal", "Veet",

                        // Higiene del hogar
                        "Fabuloso", "Pinol", "Cloralex", "Mr. Músculo", "Ariel", "Ace", "Suavitel",
                        "Vanish", "Persil", "Zote", "Blancatel", "Bold", "Downy", "Pato Purific",
                        "Sarricida", "Scotch-Brite", "Vim",

                        // Productos para mascotas
                        "Pedigree", "Whiskas", "Dog Chow", "Purina", "Cat Chow", "Eukanuba", "Royal Canin",
                        "Pro Plan", "Beneful", "Friskies", "Hill's Science", "Pet Pride", "Fancy Feast",
                        "Kirkland Pet Food", "CanCat",

                        // Otros
                        "Cloralex", "Pinol", "Rexona", "Palmolive", "Suavitel", "Lysol", "Raid", "Off",
                        "Galletas Maria", "Tampico", "Frutsi", "Te Lipton", "Redoxon", "Alka Seltzer",
                        "Aspirina", "Pepto-Bismol",

                        // Bebidas No Alcohólicas
                        "Coca-Cola", "Pepsi", "Fanta", "Sprite", "7UP", "Dr Pepper", "Mountain Dew",
                        "Lipton", "Arizona", "Jumex", "Boing", "Gatorade", "Powerade", "Manzanita Sol",
                        "Del Valle", "Nescafé", "Café Legal", "V8", "Tonicol",

                        // Bebidas Energizantes
                        "Monster", "Red Bull", "Vive100", "Boost", "Volt", "Burn", "Rockstar", "BANG",
                        "Full Throttle", "AMP Energy", "NOS Energy", "Adrenaline", "Celsius", "XS Energy",

                        // Cervezas y Bebidas Alcohólicas
                        "Corona", "Modelo", "Victoria", "Heineken", "Budweiser", "Tecate", "Sol",
                        "Pacífico", "Amstel", "Stella Artois", "XX Lager", "Negra Modelo",
                        "Coors", "Miller", "Guinness", "Carlsberg", "Blue Moon", "Michelob",

                        // Vinos
                        "Casillero del Diablo", "Concha y Toro", "Vinos La Cetto", "Monte Xanic", "Freixenet",
                        "Santa Carolina", "Viña Tarapacá", "Don Simón", "Muga", "Sangre de Toro",
                        "Marqués de Riscal", "Campo Viejo", "Ribera del Duero", "Protos", "Ramón Bilbao",

                        // Licores
                        "José Cuervo", "Bacardí", "Tequila Herradura", "Don Julio", "Mezcal 400 Conejos",
                        "Captain Morgan", "Absolut", "Smirnoff", "Patrón", "Jack Daniel's", "Johnnie Walker",
                        "Bulleit", "Jameson", "Tanqueray", "Bombay Sapphire", "Grey Goose", "Baileys",
                        "Jägermeister", "Licor 43", "Cazadores", "Sauza", "Disaronno",

                        // Cigarros
                        "Marlboro", "Camel", "Winston", "Lucky Strike", "Chesterfield", "Pall Mall",
                        "Delicados", "Montana", "Faros", "Newport", "Benson & Hedges", "Davidoff",
                        "Salem", "Kool", "Barclay", "Rothmans",

                        // Botanas
                        "Lays", "Doritos", "Ruffles", "Cheetos", "Sabritas", "Pringles", "Barcel",
                        "Takis", "Totis", "Churrumais", "Fritos", "Rancheritos", "Chips", "Quesabritas",
                        "Tostitos", "Chicharrones", "Krispy Kreme",

                        // Galletas y Pan
                        "Gamesa", "Marinela", "Principe", "Oreo", "Chokis", "Emperador", "Mamut",
                        "Suavicremas", "Canelitas", "Gansito", "Choco Roles", "Submarinos",
                        "Pingüinos", "Tía Rosa", "Wonder", "Bimbo", "Milano", "Triki Trakes",

                        // Cereales
                        "Kellogg's", "Zucaritas", "Froot Loops", "Choco Krispis", "Corn Flakes",
                        "Special K", "All-Bran", "Quaker", "Granola", "Nesquik", "Cap'n Crunch",
                        "Lucky Charms", "Honey Bunches of Oats", "Fitness", "Trix",

                        // Lácteos
                        "Lala", "Alpura", "Sello Rojo", "Danone", "Philadelphia", "Nido", "Santa Clara",
                        "Yakult", "Chobani", "Nutri Leche", "Cremosa", "Hollandia", "Nestlé",
                        "Clavel", "Lyncott",

                        // Conservas
                        "La Costeña", "Herdez", "Del Monte", "Jumex", "San Marcos", "La Morena",
                        "Doña María", "Valle Verde", "Isadora", "Rosarita", "McCormick", "Mazola",
                        "Heinz", "Hunts", "Kikkoman", "Goya",

                        // Pastas y Arroz
                        "La Moderna", "Barilla", "Roma", "Knorr", "Gallo", "San Antonio",
                        "Riso Scotti", "Vervena", "Tres Estrellas", "Faraon", "De Cecco",

                        // Azúcar, Aceites y Harinas
                        "Maseca", "Gamesa", "C&H", "La Fama", "Crisco", "Capullo", "Nutrioli",
                        "Mazola", "Oléico", "Kirkland", "Piloncillo", "Zulka", "Estrella Blanca",

                        // Salsas y Condimentos
                        "Valentina", "Buffalo", "Tabasco", "McCormick", "Doña María", "La Morena",
                        "Herdez", "Búfalo", "Maggi", "Huichol", "Frank's RedHot", "Ragú", "Prego",
                        "Knorr", "Kraft", "Hellmann's", "Sriracha", "Lea & Perrins",

                        // Carnes y Embutidos
                        "Fud", "San Rafael", "KIR", "Bafar", "Zwan", "La Villita", "Pek",
                        "Lala", "Oscar Mayer", "Sukarne", "El Corral", "Sabormex", "Tangamanga",

                        // Congelados
                        "La Huerta", "Birds Eye", "McCain", "Granja", "Empanadas",
                        "Freski", "Green Giant", "Maruchan",

                        // Dulces y Chocolates
                        "Hershey's", "Snickers", "M&M's", "Milky Way", "Crunch",
                        "Ferrero Rocher", "Kinder", "Carlos V", "Reese's", "KitKat",
                        "Rockaleta", "Pulparindo", "Lucas", "Mazapán", "Ricolino",
                        "Paleta Payaso",

                        // Cuidado Personal
                        "Colgate", "Sensodyne", "Oral-B", "Listerine", "Gillette", "Dove",
                        "Palmolive", "Nivea", "Vaseline", "Pantene", "Head & Shoulders", "Always",
                        "Kotex", "Suave", "Neutrogena", "Garnier", "L'Oreal",

                        // Higiene del Hogar
                        "Fabuloso", "Pinol", "Cloralex", "Mr. Músculo", "Ariel", "Ace",
                        "Suavitel", "Vanish", "Persil", "Zote", "Blancatel", "Bold",
                        "Downy", "Pato Purific", "Scotch-Brite",

                        // Productos para Mascotas
                        "Pedigree", "Whiskas", "Dog Chow", "Purina", "Cat Chow", "Eukanuba",
                        "Royal Canin", "Pro Plan", "Beneful", "Friskies", "Hill's Science",
                        "Fancy Feast",

                        // Otros
                        "Cloralex", "Pinol", "Rexona", "Palmolive", "Suavitel", "Lysol",
                        "Raid", "Off", "Te Lipton", "Alka Seltzer", "Pepto-Bismol"
                    )

                    // Lista de palabras a ignorar (stopwords)
                    val stopWords = listOf(
                        "de", "con", "el", "la", "ingredientes", "Para", "los", "las", "por"
                    )

                    // Aplicar filtros al texto reconocido (quitamos stopWords y palabras con longitud menor a 3)
                    val filteredText = recognizedText.split(" ").filter {
                        it !in stopWords && it.length > 3
                    }.joinToString(" ")

                    // Aplicar filtro de productos reconocidos (solo tomar los productos que estén en la lista de keywords)
                    val validProduct = filteredText.split(" ").filter { word ->
                        keywords.any { it.contains(word, ignoreCase = true) }
                    }.joinToString(" ")

                    if (validProduct.isNotEmpty()) {
                        // Aquí se identificó un producto válido y se ejecuta la acción según el modo actual
                        when (currentMode) {
                            SharedViewModel.Mode.SALE -> {
                                val productFromInventory =
                                    sharedViewModel.findProductInInventory(validProduct)
                                if (productFromInventory != null) {
                                    sharedViewModel.addProductToSale(productFromInventory)
                                    Toast.makeText(
                                        requireContext(),
                                        "Producto añadido a la venta: ${productFromInventory.name}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Producto no encontrado en el inventario",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            SharedViewModel.Mode.INVENTORY -> {
                                val newProduct =
                                    Product(name = validProduct, price = 0.0, quantity = 1)
                                sharedViewModel.addProductToInventory(newProduct)
                                Toast.makeText(
                                    requireContext(),
                                    "Producto añadido al inventario: $validProduct",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            null -> TODO()
                        }
                    } else {
                        // Si no se encuentra un producto válido, se muestra un mensaje
                        Toast.makeText(
                            requireContext(),
                            "No se encontró un producto válido",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    isProcessing = false
                    imageProxy.close()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        requireContext(),
                        "Error en el reconocimiento de texto",
                        Toast.LENGTH_SHORT
                    ).show()
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
