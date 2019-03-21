package com.greenkey.nehaj.organaizercalculator2.model

enum class ButtonType(val value: String, val text: String, val state: State) {
    ZERO("0","0", State.NUMERIC),
    ONE("1", "1", State.NUMERIC),
    TWO("2", "2", State.NUMERIC),
    THREE("3", "3", State.NUMERIC),
    FOUR("4", "4", State.NUMERIC),
    FIVE("5", "5", State.NUMERIC),
    SIX("6", "6", State.NUMERIC),
    SEVEN("7", "7", State.NUMERIC),
    EIGHT("8", "8", State.NUMERIC),
    NINE("9", "9", State.NUMERIC),
    MULTIPLICATION("*", "×", State.OPERATOR),
    DIVISION("/", "÷", State.OPERATOR),
    ADDITION("+", "+", State.OPERATOR),
    SUBTRACTION("-", "-", State.OPERATOR),
    EQUAL("=", "=", State.EQUAL),
    DELIMITER(".", ",", State.DELIMITER),
    CLEAR("C","C", State.CLEAR);
}