package org.example.parser.data

sealed class Operator(
    val value: String
) {
    object Plus : Operator("+")
    object Minus : Operator("-")
    object Mul : Operator("*")
    object Div : Operator("/")
    object Mod : Operator("%")
    object Equal : Operator("==")
    object NotEqual : Operator("!=")
    object Less : Operator("<")
    object LessOrEqual : Operator("<=")
    object Greater : Operator(">")
    object GreaterOrEqual : Operator(">=")
    object And : Operator("&&")
    object Or : Operator("||")
    object Not : Operator("!")
}