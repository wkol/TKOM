package org.example.parser.data


//parenthesized_expression        := "(", expression, ")" ;
//conjunction_expression          := equality_expression, {and_operator, equality_expression} ;
//disjunction_expression          := conjunction_expression, {or_operator, conjunction_expression} ;
//comparison_expression           := null_safety_expression, {comparison_operator, null_safety_expression} ;
//null_safety_expression          := additive_expression [null_safety_operator, expression] ;
//additive_expression             := multiplicative_expression, {additive_operator, multiplicative_expression} ;
//multiplicative_expression       := as_expression, {multiplicative_operator, as_expression} ;
//as_expression                   := unary_expression, [as_operator, type] ;
//unary_expression                := [unary_operator], simple_expression ;
//simple_expression               := literal_constant | identifier | function_call | parenthesized_expression | builtin_function_call ;


sealed class Expression {

    data class DisjunctionExpression (
        val left: Expression?,
        val right: List<Expression>
    ) : Expression()

    data class ConjunctionExpression(
        val left: Expression?,
        val right: List<Expression>
    ) : Expression()

    data class ComparisonExpression(
        val left: Expression?,
        val operator: Operator,
        val right: Expression?
    ) : Expression()

    data class NullSafetyExpression(
        val left: Expression?,
        val right: Expression?
    ) : Expression()

    data class AdditiveExpression(
        val left: Expression?,
        val right: List<OperatorWithExpression>
    ) : Expression()

    data class MultiplicativeExpression(
        val left: Expression?,
        val right: List<OperatorWithExpression>
    ) : Expression()

    data class AsExpression(
        val left: Expression?,
        val type: Type
    ) : Expression()

    data class UnaryExpression(
        val operator: Operator,
        val right: SimpleExpression?
    ) : Expression()

    //simple_expression               := literal_constant | identifier | function_call | parenthesized_expression;
    sealed class SimpleExpression : Expression() {
        data class Literal(val value: Any?) : SimpleExpression()
        data class Identifier(val identifier: String) : SimpleExpression()
        data class FunctionCall(val functionName: String, val functionArgs: List<Expression>) : SimpleExpression()
        data class ParenthesizedExpression(val expression: Expression?) : SimpleExpression()
    }
}