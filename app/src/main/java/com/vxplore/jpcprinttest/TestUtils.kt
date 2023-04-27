package com.vxplore.jpcprinttest

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.widget.Toast
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream


internal object TestUtils {
    fun ReadFromFile(fileName: String?): ByteArray? {
        var data: ByteArray? = null
        try {
            val `in`: InputStream = FileInputStream(fileName)
            data = ByteArray(`in`.available())
            `in`.read(data)
            `in`.close()
        } catch (tr: Throwable) {
            tr.printStackTrace()
        }
        return data
    }

    fun getImageFromAssetsFile(ctx: Context, fileName: String?): Bitmap? {
        var image: Bitmap? = null
        val am = ctx.resources.assets
        try {
            val `is` = am.open(fileName!!)
            image = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return image
    }

    fun showMessageOnUiThread(activity: Activity, msg: String?) {
        activity.runOnUiThread { Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show() }
    }

    fun getPrintBitmap(context: Context, width: Int, height: Int): Bitmap {
        val SMALL_TEXT = 18f
        val LARGE_TEXT = 30f
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint()
        paint.color = Color.BLACK // Always Black

        //SET FONT
        val tfContent =
            Typeface.createFromAsset(context.assets, "fonts" + File.separator + "Zfull-GB.ttf")
        paint.typeface = tfContent
        var xPoint: Float
        var yPoint: Float
        val fontMetrics = paint.fontMetrics
        val smallHeight =
            Math.abs(fontMetrics.leading).toInt() + Math.abs(fontMetrics.ascent).toInt() + Math.abs(
                fontMetrics.descent
            ).toInt()
        yPoint =
            (Math.abs(fontMetrics.leading).toInt() + Math.abs(fontMetrics.ascent).toInt()).toFloat()
        paint.textSize = LARGE_TEXT
        val name1 = "花糕玉米(500g)"
        xPoint = (width - paint.measureText(name1)) / 2.0f
        yPoint = yPoint + 15
        canvas.drawText(name1, xPoint, yPoint, paint)
        yPoint = yPoint + smallHeight * 2 - 5
        val saleTip = "如重量不足，将自动退还差额"
        paint.textSize = SMALL_TEXT
        xPoint = (width - paint.measureText(saleTip)) / 2.0f
        canvas.drawText(saleTip, xPoint, yPoint, paint)
        yPoint = yPoint + smallHeight
        xPoint = 0f
        val lineWidth = 2.0f
        paint.strokeWidth = 3.0f
        canvas.drawLine(xPoint, yPoint - 5, width.toFloat(), yPoint - 5, paint)
        yPoint = yPoint + 15
        canvas.drawText("上市日期", xPoint, yPoint, paint)
        xPoint = (width / 3 + 30).toFloat()
        canvas.drawText("存储条件", xPoint, yPoint, paint)
        xPoint = (width * 2 / 3 + 20).toFloat()
        canvas.drawText("123456", xPoint, yPoint, paint)
        paint.strokeWidth = lineWidth
        canvas.drawLine(
            (width / 3 + 20).toFloat(),
            yPoint - 15,
            (width / 3 + 20).toFloat(),
            yPoint + 2 * smallHeight,
            paint
        )
        paint.strokeWidth = lineWidth
        canvas.drawLine(
            (width * 2 / 3).toFloat(),
            yPoint - 15,
            (width * 2 / 3).toFloat(),
            yPoint + 2 * smallHeight,
            paint
        )
        yPoint = yPoint + smallHeight + 5
        xPoint = 0f
        canvas.drawText("2019/12/31 24:00", xPoint, yPoint, paint)
        xPoint = (width / 3 + 30).toFloat()
        canvas.drawText("冷藏", xPoint, yPoint, paint)
        xPoint = (width * 2 / 3 + 20).toFloat()
        canvas.drawText("0312", xPoint, yPoint, paint)
        yPoint = yPoint + 70
        paint.strokeWidth = 3.0f
        xPoint = 0f
        canvas.drawLine(xPoint, yPoint - 0, width.toFloat(), yPoint - 0, paint)
        canvas.drawText("客服电话:4099503665", xPoint, yPoint + 20, paint)
        yPoint = yPoint + 45
        canvas.drawText("服务时间:07:00-21:00", xPoint, yPoint, paint)
        canvas.save()
        return bitmap
    }

    fun scaleImageToWidth(bitmap: Bitmap, w: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        // 缩放图片的尺寸
        val scaleWidth = w.toFloat() / bitmapWidth
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleWidth)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false)
    }

    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap? {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        // CREATE A MATRIX FOR THE MANIPULATION
        val matrix = Matrix()
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight)

        // "RECREATE" THE NEW BITMAP
        val resizedBitmap = Bitmap.createBitmap(
            bm, 0, 0, width, height, matrix, false
        )
        bm.recycle()
        return resizedBitmap
    }

    fun scaleImageToDestWidthHeight(bitmap: Bitmap, w: Int, h: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        // 缩放图片的尺寸
        val scaleWidth = w.toFloat() / bitmapWidth
        val scaleHeight = h.toFloat() / bitmapHeight
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, false)
    }
}