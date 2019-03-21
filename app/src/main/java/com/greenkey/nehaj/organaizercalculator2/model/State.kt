package com.greenkey.nehaj.organaizercalculator2.model

import java.lang.IllegalArgumentException

enum class State {
    NUMERIC,
    OPERATOR,
    DELIMITER,
    EQUAL,
    ERROR,
    CLEAR;

    companion object {
        fun isValid(lastState: State, currentState: State): Boolean{
            return when(lastState){
                OPERATOR -> currentState != OPERATOR && currentState != DELIMITER && currentState != EQUAL
                DELIMITER -> currentState != OPERATOR && currentState != DELIMITER && currentState != EQUAL
                ERROR -> currentState != OPERATOR && currentState != DELIMITER && currentState != EQUAL
                CLEAR -> currentState != OPERATOR && currentState != DELIMITER && currentState != EQUAL
                EQUAL -> currentState != EQUAL
                else -> true
            }
        }

        fun isCanCalculate(lastState: State, currentState: State): Boolean {
            return currentState != State.OPERATOR && currentState != DELIMITER
        }

        fun isDisplayed(state: State): Boolean{
            return state == NUMERIC || state == DELIMITER || state == OPERATOR
        }

        fun getStateByValue(value: String) : State{
            ButtonType.values().forEach {
                if (it.value == value)
                    return@getStateByValue it.state
            }

            throw IllegalArgumentException("Can't find state with value: $value")
        }
    }
}