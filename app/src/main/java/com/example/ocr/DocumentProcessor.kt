package com.example.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object DocumentProcessor {

    data class ProcessedDocument(
        val fileName: String,
        val pageCount: Int,
        val previewBitmap: Bitmap?,
        val pageBitmaps: List<Bitmap>
    )

    suspend fun processPdfUri(
        context: Context,
        uri: Uri,
        maxPages: Int = 4
    ): ProcessedDocument = withContext(Dispatchers.IO) {
        val fileName = getFileName(context, uri) ?: "academy_paper.pdf"
        val tempFile = File(context.cacheDir, "temp_render_${System.currentTimeMillis()}.pdf")

        // Copy uri to temp file to obtain seekable FileDescriptor
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        val pfd = ParcelFileDescriptor.open(tempFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val totalPages = renderer.pageCount
        val pagesToRender = minOf(totalPages, maxPages)

        val bitmaps = mutableListOf<Bitmap>()
        for (i in 0 until pagesToRender) {
            val page = renderer.openPage(i)
            // Scale up for high OCR clarity (e.g. 1.5x - 2.0x target ~ 1200 - 1600px width)
            val scale = (1400f / page.width.toFloat()).coerceIn(1.0f, 2.5f)
            val targetWidth = (page.width * scale).toInt()
            val targetHeight = (page.height * scale).toInt()

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Ensure white background for PDF

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bitmap)
            page.close()
        }

        renderer.close()
        pfd.close()
        tempFile.delete()

        ProcessedDocument(
            fileName = fileName,
            pageCount = totalPages,
            previewBitmap = bitmaps.firstOrNull(),
            pageBitmaps = bitmaps
        )
    }

    suspend fun processImageUri(
        context: Context,
        uri: Uri
    ): ProcessedDocument = withContext(Dispatchers.IO) {
        val fileName = getFileName(context, uri) ?: "scanned_paper.jpg"
        var bitmap: Bitmap? = null

        context.contentResolver.openInputStream(uri)?.use { stream ->
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(stream, null, options)

            // Calculate sample size
            var sampleSize = 1
            val maxDimension = 2048
            while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
            }

            context.contentResolver.openInputStream(uri)?.use { realStream ->
                bitmap = BitmapFactory.decodeStream(realStream, null, decodeOptions)
            }
        }

        val list = if (bitmap != null) listOf(bitmap!!) else emptyList()
        ProcessedDocument(
            fileName = fileName,
            pageCount = list.size,
            previewBitmap = bitmap,
            pageBitmaps = list
        )
    }

    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // High quality JPEG for clear Urdu Nastaliq and English font OCR
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        name = it.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path?.let { File(it).name }
        }
        return name
    }
}
