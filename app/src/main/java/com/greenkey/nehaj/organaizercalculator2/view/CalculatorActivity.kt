package com.greenkey.nehaj.organaizercalculator2.view

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProviders
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_calculator.*
import androidx.appcompat.widget.SwitchCompat
import android.os.Vibrator
import android.text.method.ScrollingMovementMethod
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.databinding.DataBindingUtil
import android.net.Uri
import android.util.TypedValue
import android.view.Gravity
import android.widget.ArrayAdapter
import androidx.lifecycle.Observer
import com.greenkey.nehaj.organaizercalculator2.model.CalculatorViewModel
import com.greenkey.nehaj.organaizercalculator2.R
import com.greenkey.nehaj.organaizercalculator2.databinding.ActivityCalculatorBinding
import com.greenkey.nehaj.organaizercalculator2.model.AnimationType
import com.greenkey.nehaj.organaizercalculator2.utils.getStringArrayList
import com.greenkey.nehaj.organaizercalculator2.utils.putStringArrayList
import android.view.animation.Animation
import android.view.animation.AnimationUtils


abstract class CalculatorActivity : AppCompatActivity() {

    companion object {
        private const val CALCULATOR_PREFERENCE = "calculator_preference"

        private const val VIBRATION_PREF = "vibration_theme"
        private const val THEME_PREF = "theme_preferences"
        private const val HISTORY_PREF = "history_string"
    }

    protected lateinit var binding: ActivityCalculatorBinding

    private lateinit var preferences: SharedPreferences
    private lateinit var drawerVibrationSwitch: SwitchCompat

    private lateinit var vibe: Vibrator
    private lateinit var viewModel: CalculatorViewModel
    private var isVibrate = false
    private var themeType: String? = "dark"

    override fun onCreate(savedInstanceState: Bundle?) {
        preferences = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE)
        isVibrate = preferences.getBoolean(VIBRATION_PREF, true)
        themeType = preferences.getString(THEME_PREF, "dark")

        when(themeType){
            "dark" -> setTheme(R.style.DarkTheme)
            "light" -> setTheme(R.style.LightTheme)
            "blue" -> setTheme(R.style.BlueTheme)
            "gold" -> setTheme(R.style.GoldTheme)
            else -> setTheme(R.style.DarkTheme)
        }

        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CalculatorViewModel::class.java)
        vibe = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        initColors()

        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_calculator
        )

        binding.vm = viewModel
        binding.setLifecycleOwner(this)

        viewModel.animationType.observe(this, Observer {onAnimationUse(it)})

        viewModel.initHistory(preferences.getStringArrayList(HISTORY_PREF, ArrayList())!!)
        viewModel.history.observe(this, Observer {
            preferences.edit().putStringArrayList(HISTORY_PREF,it).apply()
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, it)
            binding.historyListView.adapter = adapter
        })

        viewModel.isVibrate.observe(this, Observer {
            if(isVibrate){
                vibe.vibrate(50)
            }
        })

        expression_text_view.movementMethod = ScrollingMovementMethod()

        drawerVibrationSwitch = binding.navigationView.menu.findItem(R.id.menu_vibration_switch).actionView as SwitchCompat
        drawerVibrationSwitch.isChecked = isVibrate

        val drawerShareLink = binding.navigationView.menu.findItem(R.id.menu_share_link)
        drawerShareLink.setOnMenuItemClickListener {
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Попробуй новое приложение калькулятор https://play.google.com/store/apps/details?id=com.speedreading.alexander.speedreading")
            sendIntent.type = "text/plain"
            startActivity(sendIntent)

            return@setOnMenuItemClickListener true
        }

        val drawerFeedbackLink = binding.navigationView.menu.findItem(R.id.menu_feedback_link)
        drawerFeedbackLink.setOnMenuItemClickListener {
            val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.speedreading.alexander.speedreading"))
            startActivity(sendIntent)

            return@setOnMenuItemClickListener true
        }

        binding.historyListView.setOnItemClickListener { _, _, position, _ -> viewModel.copyHistoryToExpression(position) }
        drawerVibrationSwitch.setOnCheckedChangeListener { _, isChecked -> toggleVibration(isChecked) }
        binding.displayView.setOnClickListener { showHistory() }
        binding.resultTextView.setOnClickListener { showHistory() }
        binding.expressionTextView.setOnClickListener { showHistory() }
        binding.clearHistoryButton.setOnClickListener { clearHistory() }
        binding.hideHistoryButton.setOnClickListener { closeHistory() }

        binding.navigationView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.theme_dark -> setTheme("dark")
                R.id.theme_light -> setTheme("light")
                R.id.theme_gold -> setTheme("gold")
                R.id.theme_blue -> setTheme("blue")
            }
            return@setNavigationItemSelectedListener true
        }

        when(themeType) {
            "dark" ->  binding.navigationView.menu.findItem(R.id.theme_dark).isChecked = true
            "light" -> binding.navigationView.menu.findItem(R.id.theme_light).isChecked = true
            "blue" -> binding.navigationView.menu.findItem(R.id.theme_blue).isChecked = true
            "gold" -> binding.navigationView.menu.findItem(R.id.theme_gold).isChecked = true
            else -> binding.navigationView.menu.findItem(R.id.theme_dark).isChecked = true

        }
    }

    private fun toggleVibration(vibration: Boolean) {
        val editor = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE).edit()
        editor.putBoolean(VIBRATION_PREF, vibration)
        editor.apply()
        isVibrate = vibration
    }

    private fun setTheme(theme: String) {
        val editor = getSharedPreferences(CALCULATOR_PREFERENCE, Context.MODE_PRIVATE).edit()
        editor.putString(THEME_PREF,theme)
        editor.apply()

        val intent = intent
        finish()

        startActivity(intent)
    }

    @SuppressLint("RtlHardcoded")
    fun onMenuButtonClick(v: View){
       navigation_view_layout.openDrawer(Gravity.LEFT)
    }

    abstract fun cancelAnimation()

    abstract fun reveal(sourceView: View, colorRes: Int, listener: AnimatorListenerWrapper)

    abstract fun onResult(result: String, listener: AnimatorListenerWrapper)

    private fun clearHistory(): Boolean{
        viewModel.clearHistory()
        closeHistory()

        return true
    }

    private fun showHistory(): Boolean{
        val animSlideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)

        binding.numbersContainerLayout.visibility = View.GONE
        binding.historyContainer.visibility = View.VISIBLE
        binding.operatorEqualsHistory.visibility = View.VISIBLE
        binding.clearButton.visibility = View.GONE

        binding.buttonsContainerLayout.setBackgroundResource(backgroundDisplay)
        binding.historyContainer.startAnimation(animSlideDown)


        return true
    }

    private var backgroundDisplay: Int = 0
    private var backgroundButtons: Int = 0

    private fun initColors() {
        val typedValue = TypedValue()
        val typedArray = themedContext.obtainStyledAttributes(
            typedValue.data, intArrayOf(
                R.attr.colorPrimary,
                R.attr.buttonsBackground))

        var index = 0

        backgroundDisplay = typedArray.getResourceId(index, 0)
        backgroundButtons = typedArray.getResourceId(++index, 0)

        typedArray.recycle()
    }


    private fun closeHistory(): Boolean{
        val animSlideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up)

        animSlideDown.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                binding.displayView.isClickable = false
                binding.resultTextView.isClickable = false
                binding.expressionTextView.isClickable = false
            }

            override fun onAnimationEnd(animation: Animation?) {
                binding.buttonsContainerLayout.setBackgroundResource(backgroundButtons)
                binding.historyContainer.visibility = View.GONE
                binding.operatorEqualsHistory.visibility = View.GONE
                binding.clearButton.visibility = View.VISIBLE
                binding.numbersContainerLayout.visibility = View.VISIBLE
                binding.displayView.isClickable = true
                binding.resultTextView.isClickable = true
                binding.expressionTextView.isClickable = true
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })

        binding.historyContainer.startAnimation(animSlideDown)

        return true
    }

    private fun onAnimationUse(type: AnimationType){
        when(type){
            AnimationType.CLEAR -> {
                reveal(binding.clearButton, R.color.colorReveal, object : AnimatorListenerWrapper {
                    override fun onAnimationStart() {
                        viewModel.onAnimationClearStarted()
                    }

                    override fun onAnimationEnd() {}
                })
            }
            AnimationType.RESULT -> {
                onResult(viewModel.result.value!!, object : AnimatorListenerWrapper {
                    override fun onAnimationStart() {}

                    override fun onAnimationEnd() {
                        viewModel.onAnimationResultEnded()
                    }
                })
            }
        }
    }
}
