package org.example.parser.data

sealed class Statement {
    data class IfStatement(
        val condition: Expression?,
        val body: List<Statement>,
        val elseBody: List<Statement>? = null
    ) : Statement()

    data class WhileStatement(
        val condition: Expression?,
        val body: List<Statement>
    ) : Statement()

    data class ReturnStatement(
        val expression: Expression?
    ) : Statement()

    data class AssignmentStatement(
        val variable: String,
        val expression: Expression
    ) : Statement()

    data class ExpressionStatement(
        val expression: Expression
    ) : Statement()

    data class VariableDeclaration(
        val isImmutable: Boolean,
        val name: String,
        val type: Type?,
        val initialValue: Expression? = null
    ) : Statement()

}