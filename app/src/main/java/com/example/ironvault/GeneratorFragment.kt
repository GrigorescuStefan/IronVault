package com.example.ironvault

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeneratorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewInflate = inflater.inflate(R.layout.fragment_generator, container, false)
        val disabledColor = ContextCompat.getColor(requireContext(), R.color.SwitchOff)
        val enabledColor = ContextCompat.getColor(requireContext(), R.color.SwitchOn)
        val passwordText: TextView = viewInflate.findViewById(R.id.passwordText)
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

        passwordRegenerate(
            passwordText,
            upperCaseSwitch,
            lowerCaseSwitch,
            numbersSwitch,
            symbolsSwitch,
            numberText.text.toString().toInt(),
            symbolText.text.toString().toInt(),
            passwordLengthSeekBar.progress
        )

        lowerCaseSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && !upperCaseSwitch.isChecked && !numbersSwitch.isChecked && !symbolsSwitch.isChecked) {
                lowerCaseSwitch.setChecked(true)
            }
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        upperCaseSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && !upperCaseSwitch.isChecked && !numbersSwitch.isChecked && !symbolsSwitch.isChecked) {
                lowerCaseSwitch.setChecked(true)
            }
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        numbersSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && !upperCaseSwitch.isChecked && !numbersSwitch.isChecked && !symbolsSwitch.isChecked) {
                lowerCaseSwitch.setChecked(true)
            }
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        symbolsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && !upperCaseSwitch.isChecked && !numbersSwitch.isChecked && !symbolsSwitch.isChecked) {
                lowerCaseSwitch.setChecked(true)
            }
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        lowerCaseSwitch.setChecked(true)
        updateText(passwordLengthSeekBar.progress, passwordLength)

        refreshButton.setOnClickListener {
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        copyButton.setOnClickListener {
            val clipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("password", passwordText.text)
            clipboardManager.setPrimaryClip(clip)
            UtilityFunctions.showToastMessage(requireContext(),"Password copied")
        }

        minusNumberButton.setOnClickListener {
            if (numberText.text.toString() == "1") {
                numberText.text = "0"
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
                minusNumberButton.isEnabled = false
                minusNumberButton.setBackgroundColor(disabledColor)
                return@setOnClickListener
            }
            if (numberText.text.toString() == "5") {
                numberText.text = "4"
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
                plusNumberButton.isEnabled = true
                plusNumberButton.setBackgroundColor(enabledColor)
                return@setOnClickListener
            }
            (numberText.text.toString().toInt() - 1).toString().also { numberText.text = it }
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        plusNumberButton.setOnClickListener {
            if (numberText.text.toString() == "4") {
                numberText.text = "5"
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
                plusNumberButton.isEnabled = false
                plusNumberButton.setBackgroundColor(disabledColor)
                return@setOnClickListener
            }
            if (numberText.text.toString() == "0") {
                numberText.text = "1"
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
                minusNumberButton.isEnabled = true
                minusNumberButton.setBackgroundColor(enabledColor)
                return@setOnClickListener
            }
            (numberText.text.toString().toInt() + 1).toString().also { numberText.text = it }
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        minusSymbolButton.setOnClickListener {
            if (symbolText.text.toString() == "1") {
                symbolText.text = "0"
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
                minusSymbolButton.isEnabled = false
                minusSymbolButton.setBackgroundColor(disabledColor)
                return@setOnClickListener
            }
            if (symbolText.text.toString() == "5") {
                symbolText.text = "4"
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
                plusSymbolButton.isEnabled = true
                plusSymbolButton.setBackgroundColor(enabledColor)
                return@setOnClickListener
            }
            (symbolText.text.toString().toInt() - 1).toString().also { symbolText.text = it }
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        plusSymbolButton.setOnClickListener {
            if (symbolText.text.toString() == "4") {
                symbolText.text = "5"
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
                plusSymbolButton.isEnabled = false
                plusSymbolButton.setBackgroundColor(disabledColor)
                return@setOnClickListener
            }
            if (symbolText.text.toString() == "0") {
                symbolText.text = "1"
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
                minusSymbolButton.isEnabled = true
                minusSymbolButton.setBackgroundColor(enabledColor)
                return@setOnClickListener
            }
            (symbolText.text.toString().toInt() + 1).toString().also { symbolText.text = it }
            passwordRegenerate(
                passwordText,
                upperCaseSwitch,
                lowerCaseSwitch,
                numbersSwitch,
                symbolsSwitch,
                numberText.text.toString().toInt(),
                symbolText.text.toString().toInt(),
                passwordLengthSeekBar.progress
            )
        }

        passwordLengthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateText(progress, passwordLength)
                passwordRegenerate(
                    passwordText,
                    upperCaseSwitch,
                    lowerCaseSwitch,
                    numbersSwitch,
                    symbolsSwitch,
                    numberText.text.toString().toInt(),
                    symbolText.text.toString().toInt(),
                    passwordLengthSeekBar.progress
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        return viewInflate
    }

    private fun passwordRegenerate(
        passwordField: TextView,
        upperSwitch: SwitchCompat,
        lowerSwitch: SwitchCompat,
        numberSwitch: SwitchCompat,
        symbolSwitch: SwitchCompat,
        countNumbers: Int,
        countSymbols: Int,
        seekbarProgress: Int
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            val newPassword = generatePassword(
                upperSwitch,
                lowerSwitch,
                numberSwitch,
                symbolSwitch,
                countNumbers,
                countSymbols,
                seekbarProgress
            )
            passwordField.text = newPassword
        }
    }

    private suspend fun generatePassword(
        upperSwitch: SwitchCompat,
        lowerSwitch: SwitchCompat,
        numberSwitch: SwitchCompat,
        symbolSwitch: SwitchCompat,
        countNumbers: Int,
        countSymbols: Int,
        seekbarProgress: Int
    ): String {
        return withContext(Dispatchers.Default) {
            var newPassword = ""
            var numberOfCharacters = seekbarProgress
            val lowercaseChars = "abcdefghijklmnopqrstuvwxyz".toList()
            val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toList()
            val numberChars = "0123456789".toList()
            val symbolChars = "!@#$%^&*()_-+=<>?/{}[]|".toList()

            if (numberSwitch.isChecked) { // adaug numarul de numere setat din UI
                numberOfCharacters -= countNumbers
                repeat(countNumbers) {
                    newPassword += numberChars.random()
                }
            }
            if (symbolSwitch.isChecked) { // adaug numarul de simboluri setat din UI
                numberOfCharacters -= countSymbols
                repeat(countSymbols) {
                    newPassword += symbolChars.random()
                }
            }
            if (upperSwitch.isChecked && !lowerSwitch.isChecked) { // adaug numarul de litere mari setat din UI daca am acces doar la litere mari
                repeat(numberOfCharacters) {
                    newPassword += uppercaseChars.random()
                }
            }
            if (!upperSwitch.isChecked && lowerSwitch.isChecked) {
                repeat(numberOfCharacters) {
                    newPassword += lowercaseChars.random()
                }
            }

            if (upperSwitch.isChecked && lowerSwitch.isChecked) { // adaug numarul de litere mari si mici setat din UI, si e nevoie sa le impart in mod egal sau cat de egal posibil ca sa le adaug pe amandoua
                repeat(numberOfCharacters / 2) {
                    newPassword += uppercaseChars.random()
                }
                numberOfCharacters -= numberOfCharacters / 2
                repeat(numberOfCharacters) {
                    newPassword += lowercaseChars.random()
                }
            }
            if (!upperSwitch.isChecked && !lowerSwitch.isChecked && numberOfCharacters != 0) { // adaug numarul de litere mici ramase in cazul in care nu am literele mici activate, dar mai am numar de caractere ramase
                lowerSwitch.setChecked(true)
                repeat(numberOfCharacters) {
                    newPassword += lowercaseChars.random()
                }
            }
            newPassword = newPassword.toList().shuffled().joinToString("")

            newPassword
        }
    }

    @SuppressLint("SetTextI18n")
    private fun updateText(progress: Int, passwordLength: TextView) {
        // Update text of the TextView with the progress value
        passwordLength.text = " Length: $progress "
    }
}