package com.mohitatray.pdfaslayout.demo

import android.content.Intent
import android.os.Bundle
import android.text.TextPaint
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.mohitatray.pdfaslayout.MyPdfDocument
import com.mohitatray.pdfaslayout.layouts.Text
import com.mohitatray.pdfaslayout.pageitem.Content
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button_generate_pdf.setOnClickListener {

            // Define PDF content
            val pdfContent = listOf(Content(Text("Hello PdfAsLayout!", TextPaint())))

            // Build PDF
            val pdfDocument = MyPdfDocument(
                500, // PDF width
                800, // PDF height
                null, // Header content
                null, // Footer content
                pdfContent // Main PDF content
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
