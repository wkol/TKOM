package org.example.parser.data

import org.example.lexer.token.Token

interface Operator

sealed class UnaryOperator : Operator {
    object Not : UnaryOperator()
    object Minus : UnaryOperator()

    companion object {
        fun fromToken(token: Token?): UnaryOperator? {
            return when (token?.value) {
                Token.UnaryOperator.Type.NOT -> Not
                Token.UnaryOperator.Type.MINUS -> Minus
                else -> null
            }
        }
    }
}

sealed class AdditiveOperator : Operator {
    object Plus : AdditiveOperator()
    object Minus : AdditiveOperator()

    override fun toString(): String {
        return when (this) {
            is Plus -> "+"
            is Minus -> "-"
        }
    }

    companion object {
        fun fromToken(token: Token?): AdditiveOperator? {
            return when (token?.value) {
                Token.AdditiveOperator.Type.PLUS -> Plus
                Token.UnaryOperator.Type.MINUS -> Minus
                else -> null
            }
        }
    }
}

sealed class MultiplicativeOperator : Operator {
    object Mul : MultiplicativeOperator()
    object Div : MultiplicativeOperator()
    object Mod : MultiplicativeOperator()

    companion object {
        fun fromToken(token: Token?): MultiplicativeOperator? {
            return when (token?.value) {
                Token.MultiplicativeOperator.Type.MULTIPLY -> Mul
                Token.MultiplicativeOperator.Type.DIVIDE -> Div
                Token.MultiplicativeOperator.Type.MODULO -> Mod
                else -> null
            }
        }
    }
}

sealed class ComparisonOperator : Operator {
    object Equal : ComparisonOperator()
    object NotEqual : ComparisonOperator()
    object Less : ComparisonOperator()
    object LessOrEqual : ComparisonOperator()
    object Greater : ComparisonOperator()
    object GreaterOrEqual : ComparisonOperator()

    companion object {
        fun fromToken(token: Token?): ComparisonOperator? {
            return when (token?.value) {
                Token.ComparisonOperator.Type.EQUAL -> Equal
                Token.ComparisonOperator.Type.NOT_EQUAL -> NotEqual
                Token.ComparisonOperator.Type.LESS -> Less
                Token.ComparisonOperator.Type.LESS_OR_EQUAL -> LessOrEqual
                Token.ComparisonOperator.Type.GREATER -> Greater
                Token.ComparisonOperator.Type.GREATER_OR_EQUAL -> GreaterOrEqual
                else -> null
            }
        }
    }
}
