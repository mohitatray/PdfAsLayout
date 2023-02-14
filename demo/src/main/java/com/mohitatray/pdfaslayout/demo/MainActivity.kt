package com.mohitatray.pdfaslayout.demo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextPaint
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.mohitatray.pdfaslayout.MyPdfDocument
import com.mohitatray.pdfaslayout.demo.databinding.ActivityMainBinding
import com.mohitatray.pdfaslayout.layouts.Text
import com.mohitatray.pdfaslayout.pageitem.Content
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @SuppressLint("IntentReset")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        binding.buttonGeneratePdf.setOnClickListener {

            // Define PDF content
            val pdfContent = listOf(Content(Text("Hello PdfAsLayout!", TextPaint())))

            // Build PDF
            val pdfDocument = MyPdfDocument(
                pageWidth = 500, // PDF width
                pageHeight = 800, // PDF height
                headerContent = null, // Header content
                footerContent = null, // Footer content
                pageItems = pdfContent // Main PDF content
            )

            // Write PDF to a file in cache
            val pdfsDir = File(cacheDir, "pdfs").apply { mkdirs() }
            val file = File(pdfsDir, "myPDF.pdf")
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeToStream(fileOutputStream)
            fileOutputStream.close()
            pdfDocument.close()

            // Share file using file provider
            val uri = FileProvider.getUriForFile(
                this,
                "com.mohitatray.pdfaslayout.fileprovider",
                file
            )
            val intent = Intent().apply {
                type = "application/pdf"
                action = Intent.ACTION_VIEW
                data = uri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooserIntent = Intent.createChooser(intent, "Open with")
            startActivity(chooserIntent)

        }
    }
}