package com.dantefx.pospymes

import android.graphics.Bitmap
import java.nio.ByteBuffer

fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
    // Asegúrate de que el tamaño del ByteBuffer sea correcto
    val byteBuffer = ByteBuffer.allocateDirect(4 * bitmap.width * bitmap.height * 3)
    bitmap.copyPixelsToBuffer(byteBuffer)
    return byteBuffer
}
