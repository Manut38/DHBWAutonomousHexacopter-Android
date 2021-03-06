package net.gyroinc.dhbwhexacopter.fragments

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import net.gyroinc.dhbwhexacopter.R
import net.gyroinc.dhbwhexacopter.interfaces.IColorChangeListener

class LedControlFragment(private val colorChangeListener: IColorChangeListener) : BottomSheetDialogFragment() {
    private lateinit var prefs: SharedPreferences
    private lateinit var ledPowerButton: ImageView
    private lateinit var colorPreviewView: ImageView
    private lateinit var colorPicker: HSLColorPicker
    private lateinit var brightnessSeekBar: SeekBar
    private var pickedColor: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.led_control_fragment, container, false)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        bindViews(view)
        setupColorpicker()
        setupButtonActions(view)
        setupBrightnessSeekBar()
        setPickedColor(prefs.getInt("ledControlColor", requireContext().getColor(R.color.red)))
        return view
    }

    private fun setupBrightnessSeekBar() {
        brightnessSeekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                prefs.edit().putInt("ledControlBrightness", seekBar.progress).apply()
                colorsChanged()
            }
        })
        brightnessSeekBar.progress = prefs.getInt("ledControlBrightness", 0)
    }

    private fun setupButtonActions(view: View) {
        ledPowerButton.setOnClickListener {
            if (brightnessSeekBar.progress == 0) {
                brightnessSeekBar.progress = 200
            } else {
                brightnessSeekBar.progress = 0
            }
            prefs.edit().putInt("ledControlBrightness", brightnessSeekBar.progress).apply()
            colorsChanged()
        }

        view.findViewById<ImageView>(R.id.close_button).setOnClickListener { dismiss() }

        val onPresetClick =
            View.OnClickListener {
                setPickedColor(it.backgroundTintList!!.defaultColor)
                colorsChanged()
            }

        view.findViewById<ImageView>(R.id.color_preset_red).setOnClickListener(onPresetClick)
        view.findViewById<ImageView>(R.id.color_preset_green).setOnClickListener(onPresetClick)
        view.findViewById<ImageView>(R.id.color_preset_blue).setOnClickListener(onPresetClick)
    }

    private fun setupColorpicker() {
        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                colorPreviewView.background.setTint(color)
            }

            override fun onColorSelectionEnd(color: Int) {
                setPickedColor(color)
                colorsChanged()
            }
        })
    }

    private fun bindViews(view: View) {
        colorPreviewView = view.findViewById(R.id.color_preview_view)
        brightnessSeekBar = view.findViewById(R.id.brightness_seek_bar)
        colorPicker = view.findViewById(R.id.led_control_color_picker)
        ledPowerButton = view.findViewById(R.id.button_led_power)
    }

    private fun setPickedColor(color: Int) {
        prefs.edit().putInt("ledControlColor", color).apply()
        pickedColor = color
        colorPicker.setColor(color)
        colorPreviewView.background.setTint(color)
    }

    private fun colorsChanged() {
        val r: Int = Color.red(pickedColor)
        val g: Int = Color.green(pickedColor)
        val b: Int = Color.blue(pickedColor)
        colorChangeListener.onColorChanged(r,g,b, brightnessSeekBar.progress)
    }

    companion object {
        const val TAG = "LedControlFragment"
    }
}