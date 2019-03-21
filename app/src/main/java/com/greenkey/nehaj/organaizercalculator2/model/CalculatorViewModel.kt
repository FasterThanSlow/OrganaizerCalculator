package com.greenkey.nehaj.organaizercalculator2.model

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.objecthunter.exp4j.ExpressionBuilder
import java.math.BigDecimal
import java.math.RoundingMode


class CalculatorViewModel : ViewModel(), LifecycleObserver {
    companion object {
        private const val EMPTY_STRING = ""
    }

    private val _result: MutableLiveData<String> = MutableLiveData()
    val result: LiveData<String>
        get() = _result

    private val _expression: MutableLiveData<String> = MutableLiveData()
    val expression: LiveData<String>
        get() = _expression

    private val _animationType: MutableLiveData<AnimationType> = MutableLiveData()
    val animationType: LiveData<AnimationType>
        get() = _animationType

    private val _history: MutableLiveData<ArrayList<String>> = MutableLiveData()
    val history: LiveData<ArrayList<String>>
        get() = _history

    private val _isVibrate:  MutableLiveData<Boolean> = MutableLiveData()
    val isVibrate: LiveData<Boolean>
        get() = _isVibrate

    private var lastState: State = State.CLEAR
    private var currentState: State = State.CLEAR

    init {
        _result.value = EMPTY_STRING
        _expression.value = EMPTY_STRING
    }

    fun onButtonClick(type: ButtonType){
        currentState = type.state
        _isVibrate.value = true

        if (State.isValid(lastState, currentState)){
            if (State.isDisplayed(currentState)) {
                if(lastState == State.EQUAL){
                    if(currentState == State.NUMERIC){
                        _expression.value = type.value
                    }
                    else {
                        _expression.value += type.value
                    }
                }
                else {
                    _expression.value += type.value
                }
            }
            else {
                doOperation(currentState)
            }

            if(State.isCanCalculate(lastState, currentState)) {
                if(isHasOperator(_expression.value!!)) {
                    val resultBuff = calculate(_expression.value!!)
                    if(resultBuff != null) {
                        _result.value = getTransformedResult(resultBuff)
                    }
                    else {
                        currentState = State.ERROR
                        _result.value = "Ошибка"
                    }
                }
                else {
                    _result.value = EMPTY_STRING
                }
            }

            if(_expression.value.isNullOrEmpty()){
                _result.value = EMPTY_STRING
            }
        }

        lastState = currentState
    }

    private fun doOperation(state: State){
        when(state){
            State.EQUAL -> {
                if (isHasOperator(_expression.value!!)) {
                    _history.value!!.add(_expression.value!!)
                    _history.value = _history.value
                    _animationType.value = AnimationType.RESULT
                }
            }
            State.CLEAR -> {
                _expression.value = removeLast(_expression.value!!)
                if (!_expression.value.isNullOrEmpty()) {
                    currentState =
                            State.getStateByValue(_expression.value!![_expression.value!!.length - 1].toString())
                }
            }
        }
    }

    fun onClearAll() : Boolean{
        lastState = State.CLEAR
        _animationType.value = AnimationType.CLEAR
        return true
    }

    private fun calculate(value: String): Double? {
        return try {
            val expression = ExpressionBuilder(value).build()
            expression.evaluate()
        } catch (ex: ArithmeticException){
            null
        }

    }

    private fun getTransformedResult(value: Double): String{
        val b1 = BigDecimal(value)
        val result = b1.setScale(5, RoundingMode.HALF_UP)

        return result.stripTrailingZeros().toPlainString()
    }

    private fun removeLast(str: String): String{
        return if (str.length > 1) str.substring(0, str.length - 1) else EMPTY_STRING
    }

    private fun removeFirst(str: String): String{
        return if (str.length > 1) str.substring(1, str.length) else EMPTY_STRING
    }

    fun onAnimationClearStarted(){
        _result.value = EMPTY_STRING
        _expression.value = EMPTY_STRING
    }

    fun onAnimationResultEnded(){
        _expression.value = _result.value
        _result.value = ""
    }

    fun copyHistoryToExpression(position: Int){
        if(isHasOperator(_expression.value!!)) {
            _expression.value += getTransformedResult(calculate(history.value!![position])!!)

            val resultBuff = calculate(_expression.value!!)
            if (resultBuff != null) {
                _result.value = getTransformedResult(resultBuff)
            }
            else {
                lastState = State.ERROR
                _result.value = "Ошибка"
            }
        }
        else{
            _expression.value = history.value!![position]
            _result.value = getTransformedResult(calculate(_expression.value!!)!!)
        }

        lastState = State.NUMERIC
    }

    private fun isHasOperator(str: String): Boolean{
        val result = removeFirst(str)
        return result.contains(ButtonType.SUBTRACTION.value) ||
                result.contains(ButtonType.ADDITION.value) ||
                result.contains(ButtonType.MULTIPLICATION.value) ||
                result.contains(ButtonType.DIVISION.value)
    }

    fun initHistory(list: ArrayList<String>){
        _history.value = list
    }

    fun clearHistory(){
        _history.value = ArrayList<String>()
    }

}