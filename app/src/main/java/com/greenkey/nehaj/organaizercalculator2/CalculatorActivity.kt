package com.greenkey.nehaj.organaizercalculator2

import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.view.Gravity
import android.view.View
import kotlinx.android.synthetic.main.activity_calculator.*
import android.support.v7.widget.SwitchCompat
import android.os.Vibrator
import android.support.constraint.ConstraintLayout
import android.text.method.ScrollingMovementMethod
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.BigDecimal
import java.math.RoundingMode
import android.content.Intent
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.net.Uri
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.MenuItem
import android.widget.*
import android.widget.ArrayAdapter
import com.greenkey.nehaj.organaizercalculator2.databinding.ActivityCalculatorBinding

abstract class CalculatorActivity : AppCompatActivity() {
    lateinit var expressionTextView: TextView
    lateinit var resultTextView: TextView

    lateinit var displayView: ConstraintLayout
    private lateinit var clearButton: Button
    private lateinit var clearHistoryButton: Button
    private lateinit var buttonsContainerLayout: LinearLayout
    private lateinit var numbersContainerLayout: ConstraintLayout
    private lateinit var operatorsContainerLayout: ConstraintLayout
    private lateinit var historyCloseButton: ImageView
    private lateinit var historyListView: ListView
    private lateinit var historyContainer: LinearLayout
    private lateinit var operatorEqualHistory: Button
    private lateinit var hideHistoryButton: Button
    private lateinit var menuThemeSelector: MenuItem

    private var history: ArrayList<String> = java.util.ArrayList()

    private var lastNumeric: Boolean = false

    private var stateError: Boolean = false

    // If true, do not allow to add another DOT
    private var lastDot: Boolean = false
    private lateinit var preferences: SharedPreferences

    private lateinit var navigationView: NavigationView
    private lateinit var drawerThemeSwitch: SwitchCompat
    private lateinit var drawerVibrationSwitch: SwitchCompat

    private lateinit var vibe: Vibrator
    private var isVibrate = false

    private val CALCULATOR_PREFERENCE = "calculator_preference"
    private val LIGHT_THEME_PREF = "light_theme"
    private val VIBRATION_PREF = "vibration_theme"

    private val HISTORY_PREF = "history_string"

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE)
        val useLightTheme = preferences.getBoolean(LIGHT_THEME_PREF, false)
        isVibrate = preferences.getBoolean(VIBRATION_PREF, true)
        val historyBufString = preferences.getString(HISTORY_PREF, "")

        if (historyBufString != "") {
            if(historyBufString.contains(",")) {
                history = historyBufString.split(",") as ArrayList<String>
            }
            else{
                history.add(historyBufString)
            }
        }


        if(useLightTheme) {
            setTheme(R.style.LightTheme)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)
        /*val binding = DataBindingUtil.setContentView<ActivityCalculatorBinding>(this, R.layout.activity_calculator)
        binding.vm = ViewModelProviders.of(this).get(CalculatorViewModel::class.java)*/

        vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        expressionTextView = findViewById(R.id.formulaTextView)
        resultTextView = findViewById(R.id.resultTextView)
        displayView = findViewById(R.id.display_view)
        historyCloseButton = findViewById(R.id.history_close_button)
        historyContainer = findViewById(R.id.history_container)
        clearHistoryButton = findViewById(R.id.clear_history_button)
        operatorEqualHistory = findViewById(R.id.operator_equals_history)

        buttonsContainerLayout = findViewById(R.id.buttons_container_layout)
        operatorsContainerLayout = findViewById(R.id.operators_container_layout)
        numbersContainerLayout = findViewById(R.id.numbers_container_layout)
        historyListView = findViewById(R.id.history_list_view)
        hideHistoryButton = findViewById(R.id.hide_history_button)

        historyListView.setOnItemClickListener { parent, view, position, id ->
            if(isVibrate) vibe.vibrate(50)
            if(isAfterEqual){
                isOperation = false
                lastDot = false
                lastNumeric = false
                stateError = false
                isAfterEqual = false

                expressionTextView.text = ""
            }
            if (stateError) {
                // If current state is Error, replace the error message
                getTransformedResult(calculate(history[position]))
                stateError = false
            } else {
                expressionTextView.append(getTransformedResult(calculate(history[position])))
                if(isOperation) {
                    try {
                        resultTextView.text = getTransformedResult(calculate(expressionTextView.text.toString()))
                    } catch (ex: ArithmeticException) {
                        // Display an error message
                        resultTextView.text = "Error"
                    }
                }
            }
            // Set the flag
            lastNumeric = true
        }

        expressionTextView.movementMethod = ScrollingMovementMethod()

        clearButton = findViewById(R.id.clear_button)

        clearButton.setOnLongClickListener{
            clear()
            isAfterEqual = false
            return@setOnLongClickListener true }

        navigationView = findViewById(R.id.navigation_view)

        drawerVibrationSwitch = navigationView.menu.findItem(R.id.menu_vibration_switch).actionView as SwitchCompat
        menuThemeSelector = navigationView.menu.findItem(R.id.menu_theme_selector)
        drawerVibrationSwitch.isChecked = isVibrate

        val drawerShareLink = navigationView.menu.findItem(R.id.menu_share_link)
        drawerShareLink.setOnMenuItemClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Попробуй новое приложение калькулятор https://play.google.com/store/apps/details?id=com.speedreading.alexander.speedreading")
            sendIntent.type = "text/plain"
            startActivity(sendIntent)

            return@setOnMenuItemClickListener true
        }

        val drawerFeedbackLink = navigationView.menu.findItem(R.id.menu_feedback_link)
        drawerFeedbackLink.setOnMenuItemClickListener {
            val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.speedreading.alexander.speedreading"))
            startActivity(sendIntent)

            return@setOnMenuItemClickListener true
        }

        drawerVibrationSwitch.setOnCheckedChangeListener { _, isChecked -> toggleVibration(isChecked) }
        displayView.setOnLongClickListener { showHistory() }
        historyCloseButton.setOnClickListener { closeHistory() }
        clearHistoryButton.setOnClickListener { clearHistory() }
        hideHistoryButton.setOnClickListener { closeHistory() }
        menuThemeSelector.setOnMenuItemClickListener { item -> item.isChecked = true; return@setOnMenuItemClickListener item.isChecked }
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

    private fun saveHistory(list: ArrayList<String>){
        val editor = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE).edit()

        val sb = StringBuilder()
        for (i in 0 until list.size) {
            if(!list[i].isEmpty()) {
                if(i == list.size - 1) {
                    sb.append(list[i])
                }
                else{
                    sb.append(list[i]).append(",")
                }
            }
        }

        editor.putString(HISTORY_PREF, sb.toString())
        editor.apply()
    }

    private fun clear(){
        isOperation = false
        lastDot = false
        lastNumeric = false
        stateError = false

        reveal(clearButton, R.color.colorReveal,
            AnimatorListenerWrapper {
                expressionTextView.text = ""
                resultTextView.text = ""
            })
    }

    @SuppressLint("RtlHardcoded")
    fun onMenuButtonClick(v: View){
       //navigation_view_layout.openDrawer(Gravity.LEFT)
    }

    fun onDigit(view: View) {
        if(isVibrate) vibe.vibrate(50)
        if(isAfterEqual){
            isOperation = false
            lastDot = false
            lastNumeric = false
            stateError = false
            isAfterEqual = false

            expressionTextView.text = ""
        }
        if (stateError) {
            // If current state is Error, replace the error message
            expressionTextView.text = (view as Button).text
            stateError = false
        } else {
            expressionTextView.append((view as Button).text)
            if(isOperation) {
                try {
                    resultTextView.text = getTransformedResult(calculate(expressionTextView.text.toString()))
                } catch (ex: ArithmeticException) {
                    // Display an error message
                    resultTextView.text = "Error"
                }
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
        if (isAfterEqual) {
            isAfterEqual = false
            lastDot = false
        }
        if (lastNumeric && !stateError && !lastDot) {
            expressionTextView.append(".")
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
        if (isAfterEqual) isAfterEqual = false
        if (lastNumeric && !stateError) {
            when((view as Button).text){
                "÷" -> expressionTextView.append("/")
                "×" -> expressionTextView.append("*")
                else -> {
                    expressionTextView.append(view.text)
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
        if (isAfterEqual) isAfterEqual = false
        if(this.expressionTextView.text.length > 1) {
            this.expressionTextView.text = this.expressionTextView.text.substring(0, this.expressionTextView.text.length - 1)
        }
        else {
            expressionTextView.text = ""
            isOperation = false
            lastDot = false
            lastNumeric = false
            stateError = false
            resultTextView.text = ""
        }

        if(this.expressionTextView.text.isNotEmpty()) {
            if (expressionTextView.text[expressionTextView.text.length - 1].toString().toIntOrNull() != null) {
                lastNumeric = true
                if(isOperation) {
                    try {
                        resultTextView.text = getTransformedResult(calculate(expressionTextView.text.toString()))
                    } catch (ex: ArithmeticException) {
                        // Display an error message
                        resultTextView.text = "Error"
                    }
                }
            } else {
                if(isOperation) {
                    try {
                        resultTextView.text = getTransformedResult(calculate(this.expressionTextView.text.substring(0, this.expressionTextView.text.length - 1)))
                    } catch (ex: ArithmeticException) {
                        // Display an error message
                        resultTextView.text = "Error"
                    }
                }
                lastNumeric = false
            }
        }

        stateError = false
        lastDot = false
    }

    var isAfterEqual = false

    /**
     * Calculate the output using Exp4j
     */
    fun onEqual(view: View) {
        if(isVibrate) vibe.vibrate(50)
        // If the current state is error, nothing to do.
        // If the last input is a number only, solution can be found.
        if (lastNumeric && !stateError && !isAfterEqual && resultTextView.text != "") {
            // Read the expression
            val txt = expressionTextView.text.toString()
            // Create an Expression (A class from exp4j library)
            try {
                onResult(getTransformedResult(calculate(txt)))
                history.add(txt)
                saveHistory(history)
                lastDot = true // Result contains a dot
            } catch (ex: ArithmeticException) {
                // Display an error message
                expressionTextView.text = "Error"
                stateError = true
                lastNumeric = false
            }

            isOperation = false
            isAfterEqual = true
        }
    }

    abstract fun cancelAnimation()

    abstract fun reveal(sourceView: View, colorRes: Int, listener: AnimatorListenerWrapper)

    internal abstract fun onResult(result: String)

    private fun calculate(value: String): Double{
        val expression = ExpressionBuilder(value).build()
        return expression.evaluate()
    }

    private fun getTransformedResult(value: Double): String{
        val b1 = BigDecimal(value)
        val result = b1.setScale(5, RoundingMode.HALF_UP)

        return result.stripTrailingZeros().toPlainString()
    }

    private fun clearHistory(): Boolean{
        val editor = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE).edit()
        editor.putString(HISTORY_PREF, "")
        editor.apply()
        history.clear()
        closeHistory()

        return true
    }

    lateinit var adapter: ListAdapter

    private fun showHistory(): Boolean{
        numbersContainerLayout.visibility = View.GONE
        buttonsContainerLayout.setBackgroundColor(resources.getColor(R.color.colorPrimary))
        //operatorsContainerLayout.background = resources.getDrawable(R.drawable.calculator_buttons_background)

        val defaultPadding = resources.getDimension(R.dimen.default_margin).toInt()
        operatorsContainerLayout.setPadding(defaultPadding,0, 0, 0)

        adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1, history)

        historyListView.adapter = adapter as ListAdapter?
        historyContainer.visibility = View.VISIBLE
        operatorEqualHistory.visibility = View.VISIBLE
        clearButton.visibility = View.GONE

        return true
    }


    private fun closeHistory(): Boolean{
        numbersContainerLayout.visibility = View.VISIBLE
        buttonsContainerLayout.background = resources.getDrawable(R.drawable.calculator_buttons_background)
        operatorsContainerLayout.background = null
        operatorsContainerLayout.setPadding(0,0, 0, 0)
        historyCloseButton.visibility = View.INVISIBLE
        historyContainer.visibility = View.GONE
        operatorEqualHistory.visibility = View.GONE
        clearButton.visibility = View.VISIBLE

        return true
    }
}
