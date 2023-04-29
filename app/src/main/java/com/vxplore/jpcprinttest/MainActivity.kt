package com.vxplore.jpcprinttest


import android.app.DownloadManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.caysn.autoreplyprint.AutoReplyPrint
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.vxplore.jpcprinttest.ui.theme.JpcPrintTestTheme
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import kotlin.io.path.absolutePathString
import kotlin.io.path.outputStream

val client = HttpClient(Android) {
    engine {
        // this: AndroidEngineConfig
        connectTimeout = 100_000
        socketTimeout = 100_000
    }
}


class MainActivity : ComponentActivity(), AutoReplyPrint.CP_OnPortOpenedEvent_Callback {

    private var h: Pointer? = null
    var manager: DownloadManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JpcPrintTestTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(onClick = {
                                checkDeviceStatus()
                            }) {
                                Text(text = "Check Device")
                            }
                            Button(onClick = {
                                convertPdfToBitmap(coroutineScope = lifecycleScope) {
                                    tryToPrint(it)
                                }
                            }) {
                                Text(text = "Print Page")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun tryToPrint(bitmaps: List<Bitmap>) {
        bitmaps.forEach { bitmap ->
            bitmap.apply {
                var printwidth = 384
                val width_mm = IntByReference()
                val height_mm = IntByReference()
                val dots_per_mm = IntByReference()

                Thread {
                    if (!AutoReplyPrint.INSTANCE.CP_Printer_GetPrinterResolutionInfo(
                            h,
                            width_mm,
                            height_mm,
                            dots_per_mm
                        )
                    ) {
                        printwidth = width_mm.value * dots_per_mm.value
                    }

                    val nh = (height * (384 / width))
                    val scaled = TestUtils.scaleImageToWidth(
                        this@apply,
                        printwidth
                    ) //Bitmap.createScaledBitmap(this@apply, 500, height_mm.value, true)


                    val result =
                        AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
                            h,
                            scaled.width,
                            scaled.height,
                            scaled,
                            AutoReplyPrint.CP_ImageBinarizationMethod_ErrorDiffusion,
                            AutoReplyPrint.CP_ImageCompressionMethod_None
                        )
                    if (!result) TestUtils.showMessageOnUiThread(
                        this@MainActivity,
                        "Write failed"
                    )

                    AutoReplyPrint.INSTANCE.CP_Pos_Beep(h, 1, 500)
                }.start()

            }
        }
    }


    private fun Context.convertPdfToBitmap(
        target: String = "https://www.v-xplore.com/dev/rohan/toi-ci3/assets/uploads/pdf_bills/KgIgRnDl10.pdf",
        coroutineScope: CoroutineScope,
        onBitmapCreated: (List<Bitmap>) -> Unit
    ) {
        val bitmaps = mutableListOf<Bitmap>()
        coroutineScope.launch {
            try {
                val inputStream = client.get(urlString = target).body<InputStream>()
                val path = kotlin.io.path.createTempFile(prefix = "sample", suffix = ".pdf")
                inputStream.use { input ->
                    path.outputStream().use {
                        input.copyTo(it)
                    }
                }
                val file = File(path.absolutePathString())
                Log.d(
                    "TESTING",
                    "File ${path.absolutePathString()} ${file.isFile} ${file.name} ${file.path}"
                )
                val pdfRenderer = PdfRenderer(
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                )
                repeat(pdfRenderer.pageCount) { idx ->
                    val page = pdfRenderer.openPage(idx)
                    val w = resources.displayMetrics.densityDpi / 72 * page.width
                    val h = resources.displayMetrics.densityDpi / 72 * page.height
                    val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                    val newBitmap = Bitmap.createBitmap(
                        bitmap.width,
                        bitmap.height, bitmap.config
                    )
                    val canvas = Canvas(newBitmap)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(bitmap, 0f, 0f, Paint())
                    newBitmap?.let {
                        bitmaps.add(it)
                    }
                    page.close()
                }
                onBitmapCreated(bitmaps)
                pdfRenderer.close()
            } catch (ex: Exception) {
                Toast.makeText(
                    this@convertPdfToBitmap,
                    "${ex.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    private fun checkDeviceStatus() {
        AutoReplyPrint.INSTANCE.CP_Port_AddOnPortOpenedEvent(this, Pointer.NULL)
        h = AutoReplyPrint.INSTANCE.CP_Port_OpenBtSpp("86:67:7A:11:E4:93", 0)
        h?.let {
            Log.d("TESTING", "$it")
        }

    }

    override fun CP_OnPortOpenedEvent(p0: Pointer?, p1: String?, p2: Pointer?) {
        lifecycleScope.launchWhenCreated {
            Toast.makeText(
                this@MainActivity,
                "Status Pointer1 ${p0.toString()} Name: $p1 Pointer2 ${p2.toString()}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }




}
