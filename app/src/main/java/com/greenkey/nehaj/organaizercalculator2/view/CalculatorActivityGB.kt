package com.greenkey.nehaj.organaizercalculator2.view

import android.os.Bundle
import androidx.core.view.ViewCompat
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.greenkey.nehaj.organaizercalculator2.utils.RevealColorView
import com.greenkey.nehaj.organaizercalculator2.utils.Utils
import com.nineoldandroids.animation.*
import com.nineoldandroids.view.ViewHelper

class CalculatorActivityGB : CalculatorActivity() {

    private var mCurrentAnimator: Animator? = null
    private var revealColorView: RevealColorView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.displayView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                Utils.removeOnGlobalLayoutListenerCompat(binding.displayView, this)
                revealColorView =
                        RevealColorView(this@CalculatorActivityGB)
                val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, binding.displayView.height)
                revealColorView!!.layoutParams = params
                revealColorView!!.setBackgroundResource(android.R.color.transparent)

                binding.displayView.addView(revealColorView)
            }
        })
    }

    override fun cancelAnimation() {
        if (mCurrentAnimator != null) {
            mCurrentAnimator!!.end()
        }
    }

    override fun reveal(sourceView: View, colorRes: Int, listener: AnimatorListenerWrapper) {
        val clearLocation = IntArray(2)
        sourceView.getLocationInWindow(clearLocation)
        clearLocation[0] += sourceView.width / 2
        clearLocation[1] += sourceView.height / 2

        val revealCenterX = clearLocation[0] - revealColorView!!.left
        val revealCenterY = clearLocation[1] - revealColorView!!.top

        val revealAnimator = revealColorView!!.createCircularReveal(
            revealCenterX, revealCenterY, resources.getColor(colorRes)
        )
        revealAnimator.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()

        val alphaAnimator = ObjectAnimator.ofFloat(revealColorView,"alpha", 1.toFloat(), 0.toFloat())
        alphaAnimator.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        alphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                listener.onAnimationStart()
            }
        })

        val animatorSet = AnimatorSet()
        animatorSet.play(revealAnimator).before(alphaAnimator)
        animatorSet.setInterpolator(AccelerateDecelerateInterpolator())
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator: Animator?) {
                listener.onAnimationEnd()
                mCurrentAnimator = null
            }
        })

        mCurrentAnimator = animatorSet
        animatorSet.start()
    }

    override fun onResult(result: String, listener: AnimatorListenerWrapper) {
        val resultScale = binding.expressionTextView.textSize / binding.resultTextView.textSize
        val resultTranslationX =
            (1.0f - resultScale) * (binding.resultTextView.width / 2.0f - ViewCompat.getPaddingEnd(binding.resultTextView))
        val resultTranslationY =
            (1.0f - resultScale) * (binding.resultTextView.height / 2.0f - binding.resultTextView.paddingBottom) +
                    (binding.expressionTextView.bottom - binding.resultTextView.bottom) +
                    (binding.resultTextView.paddingBottom - binding.expressionTextView.paddingBottom)
        val formulaTranslationY = -binding.expressionTextView.bottom

        // Use a value animator to fade to the final text color over the course of the animation.
        val resultTextColor = binding.resultTextView.currentTextColor
        val formulaTextColor = binding.expressionTextView.currentTextColor
        val textColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), resultTextColor, formulaTextColor)
        textColorAnimator.addUpdateListener { valueAnimator -> binding.resultTextView.setTextColor(valueAnimator.animatedValue as Int) }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            textColorAnimator,
            ObjectAnimator.ofFloat(binding.resultTextView, "scaleX", resultScale),
            ObjectAnimator.ofFloat(binding.resultTextView, "scaleY", resultScale),
            ObjectAnimator.ofFloat(binding.resultTextView, "translationX", resultTranslationX),
            ObjectAnimator.ofFloat(binding.resultTextView, "translationY", resultTranslationY),
            ObjectAnimator.ofFloat(binding.expressionTextView, "translationY", formulaTranslationY.toFloat())
        )
        animatorSet.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        animatorSet.setInterpolator(AccelerateDecelerateInterpolator())
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                listener.onAnimationStart()
            }

            override fun onAnimationEnd(animation: Animator?) {
                // Reset all of the values modified during the animation.
                binding.resultTextView.setTextColor(resultTextColor)
                ViewHelper.setScaleX(binding.resultTextView, 1.0f)
                ViewHelper.setScaleY(binding.resultTextView, 1.0f)
                ViewHelper.setTranslationX(binding.resultTextView, 0.0f)
                ViewHelper.setTranslationY(binding.resultTextView, 0.0f)
                ViewHelper.setTranslationY(binding.expressionTextView, 0.0f)

                binding.expressionTextView.text = binding.resultTextView.text
                binding.resultTextView.text = ""

                listener.onAnimationEnd()

                mCurrentAnimator = null
            }
        })

        mCurrentAnimator = animatorSet
        animatorSet.start()
    }
}