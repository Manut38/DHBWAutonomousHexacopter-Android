package net.gyroinc.dhbwhexacopter.utils

import android.view.View
import androidx.databinding.BindingAdapter

class BindingAdapters {
    companion object {
        @BindingAdapter("goneUnless")
        @JvmStatic
        fun goneUnless(view: View, visible: Boolean) {
            view.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
}