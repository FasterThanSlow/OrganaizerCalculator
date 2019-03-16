package com.greenkey.nehaj.organaizercalculator2

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import com.nineoldandroids.animation.*
import com.nineoldandroids.view.ViewHelper

class CalculatorActivityGB : CalculatorActivity() {
    private var mCurrentAnimator: Animator? = null
    private var revealColorView: RevealColorView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        displayView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                Utils.removeOnGlobalLayoutListenerCompat(displayView, this)
                revealColorView = RevealColorView(this@CalculatorActivityGB)
                val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, displayView.height)
                revealColorView!!.layoutParams = params
                revealColorView!!.setBackgroundResource(android.R.color.transparent)

                displayView.addView(revealColorView)
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
                mCurrentAnimator = null
            }
        })

        mCurrentAnimator = animatorSet
        animatorSet.start()
    }

    override fun onResult(result: String) {
        val resultScale = expressionTextView.textSize / resultTextView.textSize
        val resultTranslationX =
            (1.0f - resultScale) * (resultTextView.width / 2.0f - ViewCompat.getPaddingEnd(resultTextView))
        val resultTranslationY =
            (1.0f - resultScale) * (resultTextView.height / 2.0f - resultTextView.paddingBottom) +
                    (expressionTextView.bottom - resultTextView.bottom) +
                    (resultTextView.paddingBottom - expressionTextView.paddingBottom)
        val formulaTranslationY = -expressionTextView.bottom

        // Use a value animator to fade to the final text color over the course of the animation.
        val resultTextColor = resultTextView.currentTextColor
        val formulaTextColor = expressionTextView.currentTextColor
        val textColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), resultTextColor, formulaTextColor)
        textColorAnimator.addUpdateListener { valueAnimator -> resultTextView.setTextColor(valueAnimator.animatedValue as Int) }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            textColorAnimator,
            ObjectAnimator.ofFloat(resultTextView, "scaleX", resultScale),
            ObjectAnimator.ofFloat(resultTextView, "scaleY", resultScale),
            ObjectAnimator.ofFloat(resultTextView, "translationX", resultTranslationX),
            ObjectAnimator.ofFloat(resultTextView, "translationY", resultTranslationY),
            ObjectAnimator.ofFloat(expressionTextView, "translationY", formulaTranslationY.toFloat())
        )
        animatorSet.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()
        animatorSet.setInterpolator(AccelerateDecelerateInterpolator())
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                resultTextView.text = result
            }

            override fun onAnimationEnd(animation: Animator?) {
                // Reset all of the values modified during the animation.
                resultTextView.setTextColor(resultTextColor)
                ViewHelper.setScaleX(resultTextView, 1.0f)
                ViewHelper.setScaleY(resultTextView, 1.0f)
                ViewHelper.setTranslationX(resultTextView, 0.0f)
                ViewHelper.setTranslationY(resultTextView, 0.0f)
                ViewHelper.setTranslationY(expressionTextView, 0.0f)

                // Finally update the formula to use the current result.
                expressionTextView.text = result
                resultTextView.text = ""

                mCurrentAnimator = null
            }
        })

        mCurrentAnimator = animatorSet
        animatorSet.start()
    }
}