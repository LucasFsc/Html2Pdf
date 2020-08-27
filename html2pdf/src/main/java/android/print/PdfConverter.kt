package android.print

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.ParcelFileDescriptor
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import java.io.File

internal class PdfConverter private constructor() : Runnable {

    private var mContext: Context? = null
    private var mHtmlString: String? = null
    private var mPdfFile: File? = null
    private var pdfPrintAttrs: PrintAttributes? = null
        get() = if (field != null) field else defaultPrintAttrs
    private var mIsCurrentlyConverting: Boolean = false
    private var mWebView: WebView? = null
    private var mOnComplete: OnComplete? = null

    private val outputFileDescriptor: ParcelFileDescriptor?
        get() {
            try {
                mPdfFile!!.createNewFile()
                return ParcelFileDescriptor.open(mPdfFile, ParcelFileDescriptor.MODE_TRUNCATE or ParcelFileDescriptor.MODE_READ_WRITE)
            } catch (e: Exception) {
                Log.d(TAG, "Failed to open ParcelFileDescriptor", e)
            }

            return null
        }

    private val defaultPrintAttrs: PrintAttributes?
        get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) null else PrintAttributes.Builder()
            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
            .setResolution(PrintAttributes.Resolution("RESOLUTION_ID", "RESOLUTION_ID", 600, 600))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

    override fun run() {
        mWebView = WebView(mContext)
        mWebView!!.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
                    throw RuntimeException("call requires API level 19")
                else {
                    val documentAdapter = mWebView!!.createPrintDocumentAdapter("PDF Export")
                    documentAdapter.onLayout(null, pdfPrintAttrs, null, object : PrintDocumentAdapter.LayoutResultCallback() {
                        override fun onLayoutFinished(info: PrintDocumentInfo?, changed: Boolean) {
                            super.onLayoutFinished(info, changed)

                            documentAdapter.onWrite(arrayOf(PageRange.ALL_PAGES), outputFileDescriptor, null, object : PrintDocumentAdapter.WriteResultCallback() {
                                override fun onWriteFinished(pages: Array<PageRange>) {
                                    mOnComplete?.onWriteComplete()
                                    destroy()
                                }

                                override fun onWriteFailed(error: CharSequence?) {
                                    super.onWriteFailed(error)
                                    mOnComplete?.onWriteFailed()
                                }
                            })

                        }
                    }, null)
                }
            }
        }

        mWebView!!.loadDataWithBaseURL("", mHtmlString, "text/html", "UTF-8", null)
    }

    @Throws(Exception::class)
    fun convert(context: Context?, htmlString: String?, file: File?, onComplete: OnComplete?) {
        if (context == null)
            throw Exception("context can't be null")
        if (htmlString == null)
            throw Exception("htmlString can't be null")
        if (file == null)
            throw Exception("file can't be null")

        if (mIsCurrentlyConverting)
            return

        mContext = context
        mHtmlString = htmlString
        mPdfFile = file
        mIsCurrentlyConverting = true
        mOnComplete = onComplete
        runOnUiThread(this)
    }

    private fun runOnUiThread(runnable: Runnable) {
        val handler = Handler(mContext!!.mainLooper)
        handler.post(runnable)
    }

    private fun destroy() {
        mContext = null
        mHtmlString = null
        mPdfFile = null
        pdfPrintAttrs = null
        mIsCurrentlyConverting = false
        mWebView = null
        mOnComplete = null
    }

    companion object {

        private const val TAG = "PdfConverter"
        private var sInstance: PdfConverter? = null

        val instance: PdfConverter
            @Synchronized get() {
                if (sInstance == null)
                    sInstance = PdfConverter()

                return sInstance!!
            }

        interface OnComplete {
            fun onWriteComplete()
            fun onWriteFailed()
        }
    }
}