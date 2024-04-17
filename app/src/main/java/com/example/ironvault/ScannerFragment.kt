package com.example.ironvault

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ScannerFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val submitButton: Button = view.findViewById(R.id.submitButton)
        val passwordCheckTextView: TextView = view.findViewById(R.id.passwordCheck)
        val returningText: TextView = view.findViewById(R.id.returningText)
        val switchOffColor = ContextCompat.getColor(requireContext(), R.color.SwitchOff)
        val switchOnColor = ContextCompat.getColor(requireContext(), R.color.SwitchOn)
        submitButton.setOnClickListener {
            lifecycleScope.launch {
                submitButton.isEnabled = false
                submitButton.setBackgroundColor(switchOffColor)
                if (UtilityFunctions.checkEmptyString(passwordCheckTextView.text.toString())) {
                    UtilityFunctions.showToastMessage(
                        requireContext(),
                        "You can't leave the field empty!"
                    )
                    submitButton.isEnabled = true
                    submitButton.setBackgroundColor(switchOnColor)
                    return@launch
                }
                val occurrences = passwordBreachCheckAsync(passwordCheckTextView.text.toString(), requireContext())
                if (occurrences > 0) {
                    submitButton.isEnabled = true
                    submitButton.setBackgroundColor(switchOnColor)
                    returningText.setText("This password was found $occurrences times in previous databreaches. You are advised to not use it.")
                    setRedGradientBackground()
                } else {
                    submitButton.isEnabled = true
                    submitButton.setBackgroundColor(switchOnColor)
                    returningText.setText("This password was not found in any previous databreaches recorded.")
                    setGreenGradientBackground()
                }

            }
        }
    }

    private fun setRedGradientBackground(){
        val drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.red_gradient_background)
        requireView().background = drawable
    }

    private fun setGreenGradientBackground(){
        val drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.green_gradient_background)
        requireView().background = drawable
    }

    private suspend fun passwordBreachCheckAsync(password: String, context: Context): Int {
        return suspendCoroutine { continuation ->
            val shaPassword = UtilityFunctions.sha1(password)
            val firstFiveHexCharacters = shaPassword.substring(0, 5).uppercase()
            val apiURL = "https://api.pwnedpasswords.com/range/$firstFiveHexCharacters"
            val queue = Volley.newRequestQueue(context)

            val stringRequest = StringRequest(
                Request.Method.GET, apiURL,
                { response ->
                    val map = parseResponseString(response)
                    val suffix = shaPassword.substring(5).uppercase()
                    val occurrences = map[suffix] ?: 0
                    continuation.resume(occurrences)
                },
                { error ->
                    Log.d("---Error---:", error.toString())
                    continuation.resume(0) // or handle the error accordingly
                })
            queue.add(stringRequest)
        }
    }

    private fun parseResponseString(response: String): Map<String, Int> {
        val map = mutableMapOf<String, Int>()

        val lines = response.lines()
        for (line in lines) {
            val parts = line.trim().split(":")
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim().toIntOrNull()

                if (key.isNotEmpty() && value != null) {
                    map[key] = value
                }
            }
        }

        return map
    }
}