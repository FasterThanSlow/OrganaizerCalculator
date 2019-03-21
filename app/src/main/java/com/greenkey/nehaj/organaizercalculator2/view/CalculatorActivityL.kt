package com.greenkey.nehaj.organaizercalculator2.view

import android.animation.*
import android.annotation.TargetApi
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroupOverlay
import android.view.animation.AccelerateDecelerateInterpolator

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class CalculatorActivityL : CalculatorActivity() {

    private var mCurrentAnimator: Animator? = null

    override fun cancelAnimation() {
        if (mCurrentAnimator != null) {
            mCurrentAnimator!!.end()
        }
    }

    override fun reveal(sourceView: View, colorRes: Int, listener: AnimatorListenerWrapper) {
        val groupOverlay = window.decorView.overlay as ViewGroupOverlay

        val displayRect = Rect()
        binding.displayView.getGlobalVisibleRect(displayRect)

        // Make reveal cover the display and status bar.
        val revealView = View(this)
        revealView.bottom = displayRect.bottom
        revealView.left = displayRect.left
        revealView.right = displayRect.right
        revealView.setBackgroundColor(resources.getColor(colorRes))
        groupOverlay.add(revealView)

        val clearLocation = IntArray(2)
        sourceView.getLocationInWindow(clearLocation)
        clearLocation[0] += sourceView.width / 2
        clearLocation[1] += sourceView.height / 2

        val revealCenterX = clearLocation[0] - revealView.left
        val revealCenterY = clearLocation[1] - revealView.top

        val x1_2 = Math.pow((revealView.left - revealCenterX).toDouble(), 2.0)
        val x2_2 = Math.pow((revealView.right - revealCenterX).toDouble(), 2.0)
        val y_2 = Math.pow((revealView.top - revealCenterY).toDouble(), 2.0)
        val revealRadius = Math.max(Math.sqrt(x1_2 + y_2), Math.sqrt(x2_2 + y_2)).toFloat()

        val revealAnimator = ViewAnimationUtils.createCircularReveal(
            revealView,
            revealCenterX, revealCenterY, 0.0f, revealRadius
        )
        revealAnimator.duration = resources.getInteger(android.R.integer.config_longAnimTime).toLong()

        val alphaAnimator = ObjectAnimator.ofFloat(revealView, View.ALPHA, 0.0f)
        alphaAnimator.duration = resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
        alphaAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                listener.onAnimationStart()
            }
        })

        val animatorSet = AnimatorSet()
        animatorSet.play(revealAnimator).before(alphaAnimator)
        animatorSet.interpolator = AccelerateDecelerateInterpolator() as TimeInterpolator?
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animator: Animator) {
                groupOverlay.remove(revealView)
                mCurrentAnimator = null
            }
        })

        mCurrentAnimator = animatorSet
        animatorSet.start()
    }

    override fun onResult(result: String, listener: AnimatorListenerWrapper) {
        // Calculate the values needed to perform the scale and translation animations,
        // accounting for how the scale will affect the final position of the text.
        val resultScale = binding.expressionTextView.textSize / binding.resultTextView.textSize
        val resultTranslationX =
            (1.0f - resultScale) * (binding.resultTextView.width / 2.0f - binding.resultTextView.paddingEnd)
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
        animatorSet.interpolator = AccelerateDecelerateInterpolator()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                listener.onAnimationStart()
            }

            override fun onAnimationEnd(animation: Animator) {
                binding.resultTextView.setTextColor(resultTextColor)
                binding.resultTextView.scaleX = 1.0f
                binding.resultTextView.scaleY = 1.0f
                binding.resultTextView.translationX = 0.0f
                binding.resultTextView.translationY = 0.0f
                binding.expressionTextView.translationY = 0.0f

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