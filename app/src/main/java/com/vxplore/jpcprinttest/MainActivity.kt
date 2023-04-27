package com.vxplore.jpcprinttest


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.caysn.autoreplyprint.AutoReplyPrint
import com.sun.jna.Pointer
import com.sun.jna.ptr.IntByReference
import com.vxplore.jpcprinttest.ui.theme.JpcPrintTestTheme
import java.io.File


class MainActivity : ComponentActivity(), AutoReplyPrint.CP_OnPortOpenedEvent_Callback {

    private var h: Pointer? = null


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
                                print()
                            }) {
                                Text(text = "Print Page")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun print() {
        val paperWidth = 384
        val bitmap = getBitmapFromImage(this, R.drawable.test_pdf_img)
        if (bitmap == null || bitmap.width == 0 || bitmap.height == 0) return

        var printwidth = 384
        val width_mm = IntByReference()
        val height_mm = IntByReference()
        val dots_per_mm = IntByReference()
        if (AutoReplyPrint.INSTANCE.CP_Printer_GetPrinterResolutionInfo(
                h,
                width_mm,
                height_mm,
                dots_per_mm
            )
        ) {
            printwidth = width_mm.value * dots_per_mm.value
        }
        //val converted = TestUtils.scaleImageToWidth(bitmap, printwidth)

        //val bitmapImage = BitmapFactory.decodeFile("Your path")
        val nh = (bitmap.height * (420.0 / bitmap.width)).toInt()
        val scaled = Bitmap.createScaledBitmap(bitmap, 420, nh, true)
      //  your_imageview.setImageBitmap(scaled)


        val result =
            AutoReplyPrint.CP_Pos_PrintRasterImageFromData_Helper.PrintRasterImageFromBitmap(
                h,
                scaled.width,
                scaled.height,
                scaled,
                AutoReplyPrint.CP_ImageBinarizationMethod_ErrorDiffusion,
                AutoReplyPrint.CP_ImageCompressionMethod_None
            )
        if (!result) TestUtils.showMessageOnUiThread(this, "Write failed")

        AutoReplyPrint.INSTANCE.CP_Pos_Beep(h, 1, 500)

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

    private fun convertPdfToBitmap(): Bitmap {

        val fileDescriptor =
            ParcelFileDescriptor.open(
                File("res/raw/test_pdf.pdf"),
                ParcelFileDescriptor.MODE_READ_ONLY
            )
        val pdfRenderer = PdfRenderer(fileDescriptor)

// Open the page to be rendered.
        val page = pdfRenderer.openPage(1)

// Render the page to the bitmap.
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

// Close the `PdfRenderer` when you are done with it.
        page.close()
        pdfRenderer.close()

        return bitmap
    }

    private fun getBitmapFromImage(context: Context, drawable: Int): Bitmap {

        // on below line we are getting drawable
        val db = ContextCompat.getDrawable(context, drawable)

        // in below line we are creating our bitmap and initializing it.
        val bit = Bitmap.createBitmap(
            db!!.intrinsicWidth, db.intrinsicHeight, Bitmap.Config.ARGB_8888
        )

        // on below line we are
        // creating a variable for canvas.
        val canvas = Canvas(bit)

        // on below line we are setting bounds for our bitmap.
        db.setBounds(0, 0, canvas.width, canvas.height)

        // on below line we are simply
        // calling draw to draw our canvas.
        db.draw(canvas)

        // on below line we are
        // returning our bitmap.
        return bit
    }
}
