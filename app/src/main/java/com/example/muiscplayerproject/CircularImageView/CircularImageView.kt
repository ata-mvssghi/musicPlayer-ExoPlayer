import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CircularImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val paint = Paint()
    private val borderPaint = Paint()

    private var borderWidth = 0f

    init {
        setup()
    }

    private fun setup() {
        paint.isAntiAlias = true
        borderPaint.style = Paint.Style.STROKE
        borderPaint.isAntiAlias = true

        val density = resources.displayMetrics.density
        borderWidth = density * 2 // Set your desired border width

        // Set an optional border color
        // borderPaint.color = ContextCompat.getColor(context, R.color.your_border_color)
    }

    override fun onDraw(canvas: Canvas) {
        val drawable = drawable ?: return

        val bitmap = getBitmapFromDrawable(drawable)
        val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

        val halfWidth = width / 2f
        val halfHeight = height / 2f
        val radius = Math.min(halfWidth, halfHeight) - borderWidth / 2

        paint.shader = shader
        canvas.drawCircle(halfWidth, halfHeight, radius, paint)

        if (borderWidth > 0) {
            canvas.drawCircle(halfWidth, halfHeight, radius, borderPaint)
        }
    }

    private fun getBitmapFromDrawable(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
