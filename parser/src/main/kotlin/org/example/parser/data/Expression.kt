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

// interface Visitable

sealed interface Expression : Visitable {

    data class DisjunctionExpression(
        val left: Expression,
        val right: List<Expression>,
    ) : SimpleExpression() {
        override fun accept(visitor: Visitor) =
            visitor.visit(this)
    }

    data class ConjunctionExpression(
        val left: Expression,
        val right: List<Expression>,
    ) : Expression {
        override fun accept(visitor: Visitor) =
            visitor.visit(this)

    }

    data class ComparisonExpression(
        val left: Expression,
        val operator: ComparisonOperator,
        val right: Expression,
    ) : Expression {
        override fun accept(visitor: Visitor) =
            visitor.visit(this)

    }

    data class NullSafetyExpression(
        val left: Expression,
        val right: Expression,
    ) : Expression {
        override fun accept(visitor: Visitor) =
            visitor.visit(this)

    }

    data class AdditiveExpression(
        val left: Expression,
        val right: List<AdditiveOperatorWithExpression>,
    ) : Expression {
        override fun accept(visitor: Visitor) =
            visitor.visit(this)

    }

    data class MultiplicativeExpression(
        val left: Expression,
        val right: List<MultiplicativeOperatorWithExpression>,
    ) : Expression {
        override fun accept(visitor: Visitor) =
            visitor.visit(this)

    }

    data class AsExpression(
        val left: Expression,
        val type: Type,
    ) : Expression {
        override fun accept(visitor: Visitor) =
            visitor.visit(this)
    }

    data class UnaryExpression(
        val operator: UnaryOperator,
        val right: Expression,
    ) : Expression {
        override fun accept(visitor: Visitor) =
            visitor.visit(this)
    }

    sealed class SimpleExpression : Expression {
        data class Literal(
            val value: Any?,
        ) : SimpleExpression() {
            override fun accept(visitor: Visitor) =
                visitor.visit(this)

        }

        data class FunctionCall(
            val identifier: String, val args: List<Expression>? = null,
        ) : SimpleExpression() {
            override fun accept(visitor: Visitor) =
                visitor.visit(this)
        }

        data class Identifier(
            val identifier: String,
        ) : SimpleExpression() {
            override fun accept(visitor: Visitor) =
                visitor.visit(this)
        }
    }
}
