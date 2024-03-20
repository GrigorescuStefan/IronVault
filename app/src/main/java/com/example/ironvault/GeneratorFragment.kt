package com.example.ironvault

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//private const val ARG_PARAM1 = "param1"
//private const val ARG_PARAM2 = "param2"

///**
// * A simple [Fragment] subclass.
// * Use the [GeneratorFragment.newInstance] factory method to
// * create an instance of this fragment.
// */
class GeneratorFragment : Fragment() {
//    private var param1: String? = null
//    private var param2: String? = null

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_generator, container, false)
        val disabledColor = ContextCompat.getColor(requireContext(), R.color.SwitchOff)
        val enabledColor = ContextCompat.getColor(requireContext(), R.color.SwitchOn)
        val refreshButton: ImageButton = viewInflate.findViewById(R.id.refreshButton)
        val copyButton: ImageButton = viewInflate.findViewById(R.id.copyButton)
        val passwordLengthSeekBar: SeekBar = viewInflate.findViewById(R.id.seekBar)
        val passwordLength: TextView = viewInflate.findViewById(R.id.passwordLength)
        val lowerCaseSwitch: SwitchCompat = viewInflate.findViewById(R.id.switchLowerCase)
        val upperCaseSwitch: SwitchCompat = viewInflate.findViewById(R.id.switchUpperCase)
        val numbersSwitch: SwitchCompat = viewInflate.findViewById(R.id.switchNumbers)
        val symbolsSwitch: SwitchCompat = viewInflate.findViewById(R.id.switchSymbols)
        val minusNumberButton: Button = viewInflate.findViewById(R.id.minusNumbers)
        val plusNumberButton: Button = viewInflate.findViewById(R.id.plusNumbers)
        val numberText: TextView = viewInflate.findViewById(R.id.numberText)
        val symbolText: TextView = viewInflate.findViewById(R.id.symbolText)
        val minusSymbolButton: Button = viewInflate.findViewById(R.id.minusSymbols)
        val plusSymbolButton: Button = viewInflate.findViewById(R.id.plusSymbols)

        lowerCaseSwitch.setChecked(true)
        updateText(passwordLengthSeekBar.progress, passwordLength)

        refreshButton.setOnClickListener {
            UtilityFunctions.showToastMessage(requireContext(), "Refresh Button Works")
        }

        copyButton.setOnClickListener {
            UtilityFunctions.showToastMessage(requireContext(), "Copy Button Works")
        }

        minusNumberButton.setOnClickListener {
            if (numberText.text.toString() == "1") {
                numberText.text = "0"
                minusNumberButton.isEnabled = false
                minusNumberButton.setBackgroundColor(disabledColor)
                return@setOnClickListener
            }
            if (numberText.text.toString() == "5") {
                numberText.text = "4"
                plusNumberButton.isEnabled = true
                plusNumberButton.setBackgroundColor(enabledColor)
                return@setOnClickListener
            }
            numberText.text = (numberText.text.toString().toInt() - 1).toString()
        }

        plusNumberButton.setOnClickListener {
            if (numberText.text.toString() == "4") {
                numberText.text = "5"
                plusNumberButton.isEnabled = false
                plusNumberButton.setBackgroundColor(disabledColor)
                return@setOnClickListener
            }
            if (numberText.text.toString() == "0") {
                numberText.text = "1"
                minusNumberButton.isEnabled = true
                minusNumberButton.setBackgroundColor(enabledColor)
                return@setOnClickListener
            }
            numberText.text = (numberText.text.toString().toInt() + 1).toString()
        }




        minusSymbolButton.setOnClickListener {
            if (symbolText.text.toString() == "1") {
                symbolText.text = "0"
                minusSymbolButton.isEnabled = false
                minusSymbolButton.setBackgroundColor(disabledColor)
                return@setOnClickListener
            }
            if (symbolText.text.toString() == "5") {
                symbolText.text = "4"
                plusSymbolButton.isEnabled = true
                plusSymbolButton.setBackgroundColor(enabledColor)
                return@setOnClickListener
            }
            symbolText.text = (symbolText.text.toString().toInt() - 1).toString()
        }

        plusSymbolButton.setOnClickListener {
            if (symbolText.text.toString() == "4") {
                symbolText.text = "5"
                plusSymbolButton.isEnabled = false
                plusSymbolButton.setBackgroundColor(disabledColor)
                return@setOnClickListener
            }
            if (symbolText.text.toString() == "0") {
                symbolText.text = "1"
                minusSymbolButton.isEnabled = true
                minusSymbolButton.setBackgroundColor(enabledColor)
                return@setOnClickListener
            }
            symbolText.text = (symbolText.text.toString().toInt() + 1).toString()
        }

        passwordLengthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateText(progress, passwordLength)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        return viewInflate
    }

    @SuppressLint("SetTextI18n")
    private fun updateText(progress: Int, passwordLength: TextView) {
        // Update text of the TextView with the progress value
        passwordLength.text = " Length: $progress "
    }

//    companion object {
//        /**
//         * Use this factory method to create a new instance of
//         * this fragment using the provided parameters.
//         *
//         * @param param1 Parameter 1.
//         * @param param2 Parameter 2.
//         * @return A new instance of fragment GeneratorFragment.
//         */
//        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            GeneratorFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
//    }
}