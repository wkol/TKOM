package org.example.parser.data

interface OperatorWithExpression {
    val operator: Operator
    val expression: Expression
}

data class MultiplicativeOperatorWithExpression(
    override val operator: MultiplicativeOperator,
    override val expression: Expression
) : OperatorWithExpression

data class AdditiveOperatorWithExpression(
    override val operator: AdditiveOperator,
    override val expression: Expression
) : OperatorWithExpression
