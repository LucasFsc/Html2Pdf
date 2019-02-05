package io.github.lucasfsc.html2pdf

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import java.io.File
import java.net.URI

class MainActivity : AppCompatActivity(), Html2Pdf.OnCompleteConversion {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Impl example
        val converter = Html2Pdf.Companion.Builder()
            .context(this)
            .html("<p> Your awesome HTML string here! </p>")
            .file(File(URI("Your file URI")))
            .build()

        //can be called with a callback to warn the user
        converter.convertToPdf(this)

        //or without a callback
        converter.convertToPdf()

    }

    override fun onSuccess() {
        //do your thing
    }

    override fun onFailed() {
        //do your thing
    }

}
