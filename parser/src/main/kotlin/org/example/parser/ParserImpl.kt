package org.example.parser

import org.example.errorhandler.ErrorHandler
import org.example.lexer.Lexer
import org.example.lexer.token.Token
import org.example.lexer.token.TokenType
import org.example.parser.data.Expression
import org.example.parser.data.Parameter
import org.example.parser.data.Function
import org.example.parser.data.Operator
import org.example.parser.data.OperatorWithExpression
import org.example.parser.data.Program
import org.example.parser.data.Statement
import org.example.parser.data.Type
import org.example.parser.exception.DuplicateFunctionDeclarationException
import org.example.parser.exception.InvalidConditionException
import org.example.parser.exception.InvalidExpressionException
import org.example.parser.exception.InvalidFunctionDeclarationException
import org.example.parser.exception.InvalidFunctionParamDeclarationException
import org.example.parser.exception.InvalidTypeException
import org.example.parser.exception.InvalidVariableDeclaration
import org.example.parser.exception.UnmatchedParenthesisException

class ParserImpl(
    override val lexer: Lexer,
    override val errorHandler: ErrorHandler
) : Parser() {

    init {
        nextToken()
    }

    private fun nextToken() {
        lexer.getNextToken()
        while (lexer.token?.type == TokenType.COMMENT) {
            lexer.getNextToken()
        }
    }

    override fun parse(): Program {
        val statements = mutableListOf<Statement>()
        val functions = mutableMapOf<String, Function>()
        while (lexer.token?.value != "EOF") {
            tryParseFunction()?.let {
                if (functions.containsKey(it.name)) {
                    errorHandler.handleParserError(DuplicateFunctionDeclarationException(lexer.getCodePosition(), it.name))
                } else {
                    functions[it.name] = it
                }
            } ?: tryParseStatement()?.let {
                statements.add(it)
            }
        }
        return Program(statements, functions)
    }


    private fun tryParseFunction(): Function? {
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.FUN)) return null

        if (lexer.token?.type != TokenType.IDENTIFIER) {
            return null
        }
        val name = lexer.token?.value as String
        nextToken()
        if (!lexer.token.checkAndGoNext(Token.Special.Type.LPAREN)) {
            errorHandler.handleParserError(InvalidFunctionDeclarationException(lexer.getCodePosition()))
        }
        val args = tryParseFunctionParams()
        if (!lexer.token.checkAndGoNext(Token.Special.Type.RPAREN)) {
            errorHandler.handleParserError(InvalidExpressionException(lexer.getCodePosition()))
        }
        val returnType = tryParseFunctionReturnType()

        val body = tryParseBlock()
        return Function(name, args, returnType, body)
    }

    fun tryParseFunctionReturnType(): Type? {
        if (lexer.token?.value == Token.Special.Type.LBRACE) {
            return Type(Type.TypePrimitive.VOID, false)
        }
        if (!lexer.token.checkAndGoNext("->")) {
            errorHandler.handleParserError(InvalidExpressionException(lexer.getCodePosition()))
        }
        return tryParseType()
    }

    private fun tryParseBlock(): List<Statement> {
        if (!lexer.token.checkAndGoNext(Token.Special.Type.LBRACE)) {
            errorHandler.handleParserError(InvalidExpressionException(lexer.getCodePosition()))
        }
        val statements = mutableListOf<Statement>()
        while (lexer.token?.value != Token.Special.Type.RBRACE) {
            tryParseStatement()?.let {
                statements.add(it)
            }
        }
        nextToken()
        return statements
    }

    private fun tryParseStatement(): Statement? {
        return tryParseIfStatement()
            ?: tryParseWhileStatement()
            ?: tryParseReturnStatement()
            ?: tryParseVariableDeclaration()
            ?: tryParseAssignmentStatement()
            ?: tryParseExpressionStatement()
    }

    fun tryParseExpressionStatement(): Statement.ExpressionStatement? {
        val expression = tryParseDisjunctionExpression() ?: return null
        return Statement.ExpressionStatement(expression)
    }


    fun tryParseIfStatement(): Statement.IfStatement? {
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.IF)) return null

        if (!lexer.token.checkAndGoNext(Token.Special.Type.LPAREN)) {
            errorHandler.handleParserError(InvalidConditionException(lexer.getCodePosition()))
        }
        val condition = tryParseDisjunctionExpression()
        if (!lexer.token.checkAndGoNext(Token.Special.Type.RPAREN)) {
            errorHandler.handleParserError(InvalidConditionException(lexer.getCodePosition()))
        }
        val body = tryParseBlock()
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.ELSE)) return Statement.IfStatement(condition, body)
        val elseBody = tryParseBlock()

        return Statement.IfStatement(condition, body, elseBody)
    }

    fun tryParseVariableDeclaration(): Statement.VariableDeclaration? {
        var isImmutable = false
        when (lexer.token?.value) {
            Token.Keyword.Type.VAR -> {
                nextToken()
            }
            Token.Keyword.Type.CONST -> {
                isImmutable = true
                nextToken()
            }
            else -> {
                return null
            }
        }
        val type = tryParseType() ?: run {
            errorHandler.handleParserError(InvalidTypeException(lexer.getCodePosition()))
            null
        }
        if (lexer.token?.type != TokenType.IDENTIFIER) {
            errorHandler.handleParserError(InvalidVariableDeclaration(lexer.getCodePosition()))
        }
        val name = lexer.token?.value as String
        nextToken()
        if (!lexer.token.checkAndGoNext(Token.Special.Type.ASSIGN)) {
            return Statement.VariableDeclaration(isImmutable, name, type)
        }
        val expression = tryParseDisjunctionExpression()
        return Statement.VariableDeclaration(isImmutable, name, type, expression)
    }

    fun tryParseFunctionParams(): List<Parameter> {
        if (lexer.token?.value == Token.Special.Type.RPAREN) {
            return emptyList()
        }
        if (!lexer.token.isTypeKeyword()) {
            errorHandler.handleParserError(InvalidTypeException(lexer.getCodePosition()))
        }
        val parameters = mutableListOf<Parameter>()
        while (lexer.token?.value != Token.Special.Type.RPAREN) {
            val type = tryParseType()
            if (type == null || lexer.token?.type != TokenType.IDENTIFIER) {
                errorHandler.handleParserError(InvalidFunctionParamDeclarationException(lexer.getCodePosition()))
            }
            val name = lexer.token?.value as String
            nextToken()
            type?.let {
                parameters.add(Parameter(name, it))
            }
            lexer.token.checkAndGoNext(Token.Special.Type.COMMA)
        }
        return parameters
    }

    fun tryParseWhileStatement(): Statement.WhileStatement? {
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.WHILE)) return null

        if (!lexer.token.checkAndGoNext(Token.Special.Type.LPAREN)) {
            errorHandler.handleParserError(InvalidConditionException(lexer.getCodePosition()))
        }
        val condition = tryParseDisjunctionExpression()
        if (!lexer.token.checkAndGoNext(Token.Special.Type.RPAREN)) {
            errorHandler.handleParserError(InvalidConditionException(lexer.getCodePosition()))
            return null
        }
        val body = tryParseBlock()
        return Statement.WhileStatement(condition ?: return null, body)
    }

    fun tryParseAssignmentStatement(): Statement? {
        if (lexer.token?.type != TokenType.IDENTIFIER) {
            return null
        }
        val variable = lexer.token?.value as String
        nextToken()
        if (lexer.token?.value != Token.Special.Type.ASSIGN) {
            return if (lexer.token?.value == Token.Special.Type.LPAREN) {
                val arguments = tryParseFunctionCallParams() ?: emptyList()
                Statement.ExpressionStatement(Expression.SimpleExpression.FunctionCall(variable, arguments))
            } else {
                Statement.ExpressionStatement(Expression.SimpleExpression.Identifier(variable))
            }
        }
        nextToken()
        val expression = tryParseDisjunctionExpression() ?: throw InvalidExpressionException(lexer.getCodePosition())
        return Statement.AssignmentStatement(variable, expression)
    }

    fun tryParseReturnStatement(): Statement.ReturnStatement? {
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.RETURN)) return null

        val expression = tryParseDisjunctionExpression()
        return Statement.ReturnStatement(expression)
    }

    private fun tryParseParenthesizedExpression(): Expression? {
        if (!lexer.token.checkAndGoNext(Token.Special.Type.LPAREN)) return null
        val expression = tryParseDisjunctionExpression()
        if (!lexer.token.checkAndGoNext(Token.Special.Type.RPAREN)) {
            errorHandler.handleParserError(UnmatchedParenthesisException(lexer.getCodePosition()))
            return null
        }
        return expression
    }

    private fun tryParseDisjunctionExpression(): Expression? {
        val left = tryParseConjunctionExpression()
        val right = mutableListOf<Expression>()
        while (lexer.token is Token.Disjunction) {
            nextToken()
            tryParseConjunctionExpression()?.let {
                right.add(it)
            }
        }
        return if (right.isEmpty()) left else Expression.DisjunctionExpression(left, right)
    }

    private fun tryParseConjunctionExpression(): Expression? {
        val left = tryParseComparisonExpression()
        val right = mutableListOf<Expression>()
        while (lexer.token is Token.Conjunction) {
            nextToken()
            tryParseComparisonExpression()?.let {
                right.add(it)
            }
        }
        return if (right.isEmpty()) left else Expression.ConjunctionExpression(left, right)
    }

    private fun tryParseComparisonExpression(): Expression? {
        val left = tryParseNullSafetyExpression()
        val operator = tryParseComparisonOperator() ?: return left
        val right = tryParseNullSafetyExpression()
        return Expression.ComparisonExpression(left, operator, right)
    }

    private fun tryParseComparisonOperator(): Operator? {
        if (lexer.token !is Token.ComparisonOperator) {
            return null
        }
        return when (lexer.token?.value) {
            Token.ComparisonOperator.Type.EQUAL -> {
                nextToken()
                Operator.Equal
            }
            Token.ComparisonOperator.Type.NOT_EQUAL -> {
                nextToken()
                Operator.NotEqual
            }
            Token.ComparisonOperator.Type.GREATER -> {
                nextToken()
                Operator.Greater
            }
            Token.ComparisonOperator.Type.GREATER_OR_EQUAL -> {
                nextToken()
                Operator.GreaterOrEqual
            }
            Token.ComparisonOperator.Type.LESS -> {
                nextToken()
                Operator.Less
            }
            else -> {
                nextToken()
                Operator.LessOrEqual
            }
        }
    }

    private fun tryParseNullSafetyExpression(): Expression? {
        val left = tryParseAdditiveExpression()
        if (!lexer.token.checkAndGoNext("?:")) return left
        val expression = tryParseDisjunctionExpression()
        if (expression == null) {
            errorHandler.handleParserError(InvalidExpressionException(lexer.getCodePosition()))
            return null
        }
        return Expression.NullSafetyExpression(left, expression)
    }

    private fun tryParseAdditiveExpression(): Expression? {
        val left = tryParseMultiplicativeExpression()
        val right = mutableListOf<OperatorWithExpression>()
        while (lexer.token.isAdditiveOperator()) {
            val operator = Operator.Plus
            nextToken()
            tryParseMultiplicativeExpression()?.let {
                right.add(OperatorWithExpression(operator, it))
            }
        }
        if (right.isEmpty()) {
            return left
        }
        return Expression.AdditiveExpression(left, right)
    }

    private fun tryParseMultiplicativeExpression(): Expression? {
        val left = tryParseAsExpression()
        val right = mutableListOf<OperatorWithExpression>()
        while (lexer.token.isMultiplicativeOperator()) {
            val operator = when(lexer.token?.value) {
                Token.MultiplicativeOperator.Type.MULTIPLY -> Operator.Mul
                Token.MultiplicativeOperator.Type.DIVIDE -> Operator.Div
                else -> Operator.Mod
            }
            nextToken()
            tryParseAsExpression()?.let {
                right.add(OperatorWithExpression(operator, it))
            }
        }
        if (right.isEmpty()) {
            return left
        }
        return Expression.MultiplicativeExpression(left, right)
    }

    private fun tryParseAsExpression(): Expression? {
        val unaryExpression = tryParseUnaryExpression()
        val type = if (lexer.token.checkAndGoNext(Token.Keyword.Type.AS)) {
            tryParseType()
        } else {
            null
        }
        if (type == null) {
            return unaryExpression
        }
        return Expression.AsExpression(unaryExpression, type)
    }

    private fun tryParseUnaryExpression(): Expression? {
        val operator = if (lexer.token is Token.UnaryOperator) {
            if (lexer.token?.value == Token.UnaryOperator.Type.NOT) {
                nextToken()
                Operator.Not
            } else {
                nextToken()
                Operator.Minus
            }
        } else {
            null
        }
        val expression = tryParseSimpleExpression()
        if (operator == null) {
            return expression
        }
        return Expression.UnaryExpression(operator, expression)
    }

    private fun tryParseSimpleExpression(): Expression.SimpleExpression? = when (lexer.token) {
            is Token.Literal -> {
                val literal = lexer.token?.value
                nextToken()
                Expression.SimpleExpression.Literal(literal)
            }
            is Token.Identifier -> {
                val identifier = lexer.token?.value as String
                nextToken()
                if (lexer.token?.value == Token.Special.Type.LPAREN) {
                    val params = tryParseFunctionCallParams() ?: emptyList()
                    Expression.SimpleExpression.FunctionCall(identifier, params)
                } else {
                    Expression.SimpleExpression.Identifier(identifier)
                }
            }

            else -> {
                if (lexer.token?.value == Token.Special.Type.LPAREN) {
                    val expression = tryParseParenthesizedExpression()
                    Expression.SimpleExpression.ParenthesizedExpression(expression)
                } else {
                    errorHandler.handleParserError(InvalidExpressionException(lexer.getCodePosition()))
                    null
                }
            }
    }

    private fun tryParseFunctionCallParams(): List<Expression>? {
        if (!lexer.token.checkAndGoNext(Token.Special.Type.LPAREN)) {
            return null
        }
        if (lexer.token.checkAndGoNext(Token.Special.Type.RPAREN)) return emptyList()

        val params = mutableListOf<Expression>()
        while (lexer.token?.value != Token.Special.Type.RPAREN) {
            tryParseDisjunctionExpression()?.let {
                params.add(it)
            }
            lexer.token.checkAndGoNext(Token.Special.Type.COMMA)
        }
        nextToken()
        return params
    }

    private fun tryParseType(): Type? {
        if (!lexer.token.isTypeKeyword()) {
            return null
        }
        val type = lexer.token.getParameterType()
        if (type == null) {
            errorHandler.handleParserError(InvalidTypeException(lexer.getCodePosition()))
            return null
        }
        nextToken()

        val isNullable = lexer.token.checkAndGoNext(Token.Special.Type.QUESTION_MARK)
        return Type(type, isNullable)
    }

    private fun Token?.getParameterType(): Type.TypePrimitive? {
        return when (this?.value) {
            Token.Keyword.Type.BOOL -> Type.TypePrimitive.BOOL
            Token.Keyword.Type.STRING -> Type.TypePrimitive.STRING
            Token.Keyword.Type.INT -> Type.TypePrimitive.INT
            Token.Keyword.Type.DOUBLE -> Type.TypePrimitive.DOUBLE
            else -> null
        }
    }

    private fun Token?.checkAndGoNext(value: Any): Boolean {
        if (this?.value == value) {
            nextToken()
            return true
        }
        return false
    }

    private fun Token?.isTypeKeyword() = this != null && value in listOf(
        Token.Keyword.Type.INT,
        Token.Keyword.Type.DOUBLE,
        Token.Keyword.Type.STRING,
        Token.Keyword.Type.BOOL,
    )

    private fun Token?.isMultiplicativeOperator() = this != null && value in listOf(
        Token.MultiplicativeOperator.Type.MULTIPLY,
        Token.MultiplicativeOperator.Type.DIVIDE,
        Token.MultiplicativeOperator.Type.MODULO,
    )

    private fun Token?.isAdditiveOperator() = this != null && this is Token.AdditiveOperator
}