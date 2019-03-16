package com.greenkey.nehaj.organaizercalculator2

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.view.Gravity
import android.view.View
import kotlinx.android.synthetic.main.activity_calculator.*
import android.support.v7.widget.SwitchCompat
import android.widget.Toast
import android.os.Vibrator
import android.widget.Button
import android.widget.TextView
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.BigDecimal
import java.math.MathContext


class CalculatorActivity : AppCompatActivity() {
    // TextView used to display the input and output
    lateinit var txtInput: TextView
    lateinit var resultTextView: TextView
    lateinit var clearButton: Button

    // Represent whether the lastly pressed key is numeric or not
    var lastNumeric: Boolean = false

    // Represent that current state is in error or not
    var stateError: Boolean = false

    // If true, do not allow to add another DOT
    var lastDot: Boolean = false

    private lateinit var navigationView: NavigationView
    private lateinit var drawerThemeSwitch: SwitchCompat
    private lateinit var drawerVibrationSwitch: SwitchCompat

    lateinit var vibe: Vibrator
    private var isVibrate = false

    private val CALCULATOR_PREFERENCE = "calculator_preference"
    private val LIGHT_THEME_PREF = "light_theme"
    private val VIBRATION_PREF = "vibration_theme"

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE)
        val useLightTheme = preferences.getBoolean(LIGHT_THEME_PREF, false)
        isVibrate = preferences.getBoolean(VIBRATION_PREF, false)

        if(useLightTheme) {
            setTheme(R.style.LightTheme)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        txtInput = findViewById(R.id.formulaTextView)
        resultTextView = findViewById(R.id.resultTextView)

        clearButton = findViewById(R.id.clear_button)

        clearButton.setOnLongClickListener{
            txtInput.text = ""
            resultTextView.text = ""

            isOperation = false
            lastDot = false
            lastNumeric = false
            stateError = false

            return@setOnLongClickListener true }

        navigationView = findViewById(R.id.navigation_view)

        drawerThemeSwitch = navigationView.menu.findItem(R.id.menu_theme_switch).actionView as SwitchCompat
        drawerThemeSwitch.isChecked = useLightTheme

        drawerVibrationSwitch = navigationView.menu.findItem(R.id.menu_vibration_switch).actionView as SwitchCompat
        drawerVibrationSwitch.isChecked = isVibrate

        drawerThemeSwitch.setOnCheckedChangeListener { _, isChecked -> toggleTheme(isChecked) }
        drawerVibrationSwitch.setOnCheckedChangeListener { _, isChecked -> toggleVibration(isChecked) }
    }

    private fun toggleVibration(vibration: Boolean) {
        val editor = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE).edit()
        editor.putBoolean(VIBRATION_PREF, vibration)
        editor.apply()
        isVibrate = vibration
    }

    private fun toggleTheme(lightTheme: Boolean) {
        val editor = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE).edit()
        editor.putBoolean(LIGHT_THEME_PREF, lightTheme)
        editor.apply()

        val intent = intent
        finish()

        startActivity(intent)
    }

    fun onMenuButtonClick(v: View){
       navigation_view_layout.openDrawer(Gravity.LEFT)
    }

    fun onDigit(view: View) {
        if(isVibrate) vibe.vibrate(50)
        if (stateError) {
            // If current state is Error, replace the error message
            txtInput.text = (view as Button).text
            stateError = false
        } else {
            val formula = txtInput.text
            // If not, already there is a valid expression so append to it
            txtInput.append((view as Button).text)
            if(isOperation) {
                resultTextView.text = getTransformedResult(calculate(formula.toString()))
            }
        }
        // Set the flag
        lastNumeric = true
    }

    /**
     * Append . to the TextView
     */
    fun onDecimalPoint(view: View) {
        if(isVibrate) vibe.vibrate(50)
        if (lastNumeric && !stateError && !lastDot) {
            txtInput.append(".")
            lastNumeric = false
            lastDot = true
        }
    }

    var isOperation = false
    /**
     * Append +,-,*,/ operators to the TextView
     */
    fun onOperator(view: View) {
        if(isVibrate) vibe.vibrate(50)
        if (lastNumeric && !stateError) {
            when((view as Button).text){
                "÷" -> txtInput.append("/")
                "×" -> txtInput.append("*")
                else -> {
                    txtInput.append(view.text)
                }
            }

            isOperation = true
            lastNumeric = false
            lastDot = false    // Reset the DOT flag
        }
    }


    /**
     * Clear the TextView
     */
    fun onClear(view: View) {
        if(isVibrate) vibe.vibrate(50)
        if(this.txtInput.text.length > 1) {
            this.txtInput.text = this.txtInput.text.substring(0, this.txtInput.text.length - 1)
        }
        else {
            txtInput.text = ""
            isOperation = false
            lastDot = false
            lastNumeric = false
            stateError = false
            resultTextView.text = ""
        }

        if(this.txtInput.text.isNotEmpty()) {
            if (txtInput.text[txtInput.text.length - 1].toString().toIntOrNull() != null) {
                lastNumeric = true
                if(isOperation) {
                    resultTextView.text = getTransformedResult(calculate(txtInput.text.toString()))
                }
            } else {
                if(isOperation) {
                    resultTextView.text = getTransformedResult(calculate(this.txtInput.text.substring(0, this.txtInput.text.length - 1)))
                }
                lastNumeric = false
            }
        }

        stateError = false
        lastDot = false
    }

    /**
     * Calculate the output using Exp4j
     */
    fun onEqual(view: View) {
        if(isVibrate) vibe.vibrate(50)
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError) {
            // Read the expression
            val txt = txtInput.text.toString()
            // Create an Expression (A class from exp4j library)
            try {
                txtInput.text = getTransformedResult(calculate(txt))
                resultTextView.text = ""
                lastDot = true // Result contains a dot
            } catch (ex: ArithmeticException) {
                // Display an error message
                txtInput.text = "Error"
                stateError = true
                lastNumeric = false
            }

            isOperation = false
        }
    }

    private fun calculate(value: String): Double{
        val expression = ExpressionBuilder(value).build()
        return expression.evaluate()
    }

    private fun getTransformedResult(value: Double): String{
        val b1 = BigDecimal(value)
        val m = MathContext(5)
        val result = b1.round(m)

        return result.stripTrailingZeros().toPlainString()
    }
}
