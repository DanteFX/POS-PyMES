package com.dantefx.pospymes

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.nio.ByteBuffer

class ModelLoader(context: Context) {

    fun recognizeProduct(image: ByteBuffer): Int {
        // Simular una predicción devolviendo siempre el índice 0
        return 0
    }

    companion object {
        private const val NUM_CLASSES = 10 // Número de clases en el modelo
    }
}
/*class ModelLoader(context: Context) {
    private val interpreter: Interpreter

    init {
        val model = loadModelFile(context, "product_model.tflite")
        interpreter = Interpreter(model)
    }

    private fun loadModelFile(context: Context, modelPath: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelPath)
        val inputStream = fileDescriptor.createInputStream()
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun recognizeProduct(image: ByteBuffer): Int {
        val output = Array(1) { FloatArray(NUM_CLASSES) }
        interpreter.run(image, output)

        // Encontrar el índice del máximo valor en la salida
        val probabilities = output[0]
        var maxIndex = 0
        var maxProb = probabilities[0]
        for (i in probabilities.indices) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIndex = i
            }
        }

        return maxIndex
    }

    companion object {
        private const val NUM_CLASSES = 10 // Número de clases en el modelo
    }
}*/
