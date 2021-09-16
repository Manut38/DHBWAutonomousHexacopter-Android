package net.gyroinc.dhbwhexacopter.utils

import android.text.InputFilter
import android.text.Spanned

class InputFilterMinMax(min: Float, max: Float) : InputFilter {
    private var min: Float = 0.0F
    private var max: Float = 0.0F

    init {
        this.min = min
        this.max = max
    }

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        try {
            val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(
                dend,
                dest.length
            )).toFloat()
            if (isInRange(min, max, input))
                return null
        } catch (nfe: NumberFormatException) {
        }
        return ""
    }

    private fun isInRange(a: Float, b: Float, c: Float): Boolean {
        return if (b > a) c in a..b else c in b..a
    }
}