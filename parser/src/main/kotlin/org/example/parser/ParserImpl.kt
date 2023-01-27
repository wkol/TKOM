package org.example.parser

import org.example.errorhandler.ErrorHandler
import org.example.errorhandler.exception.parser.DuplicateFunctionDeclarationException
import org.example.errorhandler.exception.parser.InvalidConditionException
import org.example.errorhandler.exception.parser.InvalidExpressionException
import org.example.errorhandler.exception.parser.InvalidFunctionDeclarationException
import org.example.errorhandler.exception.parser.InvalidFunctionParamDeclarationException
import org.example.errorhandler.exception.parser.InvalidTypeException
import org.example.errorhandler.exception.parser.InvalidVariableDeclaration
import org.example.errorhandler.exception.parser.MissingExpressionException
import org.example.errorhandler.exception.parser.UnmatchedParenthesisException
import org.example.lexer.Lexer
import org.example.lexer.token.Token
import org.example.lexer.token.TokenType
import org.example.parser.data.AdditiveOperator
import org.example.parser.data.AdditiveOperatorWithExpression
import org.example.parser.data.ComparisonOperator
import org.example.parser.data.Expression
import org.example.parser.data.MultiplicativeOperator
import org.example.parser.data.MultiplicativeOperatorWithExpression
import org.example.parser.data.Parameter
import org.example.parser.data.Program
import org.example.parser.data.ProgramFunction
import org.example.parser.data.Statement
import org.example.parser.data.Type
import org.example.parser.data.UnaryOperator
import org.example.parser.data.UserDefinedFunction

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
        val functions = mutableMapOf<String, ProgramFunction>()
        do {
            val functionOrStatement = tryParseFunction() ?: tryParseStatement()
            if (functionOrStatement is ProgramFunction) {
                if (functions.containsKey(functionOrStatement.name)) {
                    errorHandler.handleParserError(
                        DuplicateFunctionDeclarationException(
                            lexer.getCodePosition(),
                            functionOrStatement.name
                        )
                    )
                } else {
                    functions[functionOrStatement.name] = functionOrStatement
                }
            } else if (functionOrStatement is Statement) {
                statements.add(functionOrStatement)
            }
        } while (functionOrStatement != null)
        return Program(statements, functions)
    }


    private fun tryParseFunction(): ProgramFunction? {
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.FUN)) return null

        if (lexer.token?.type != TokenType.IDENTIFIER) {
            errorHandler.handleParserError(InvalidFunctionDeclarationException(lexer.getCodePosition()))
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

        val returnType = tryParseFunctionReturnType() ?: Type(Type.TypePrimitive.VOID, false)

        val body = tryParseBlockWithBraces()

        return UserDefinedFunction(name, args, returnType, body)
    }

    fun tryParseFunctionReturnType(): Type? {
        if (!lexer.token.checkAndGoNext("->")) {
            return Type(Type.TypePrimitive.VOID, true)
        }
        return tryParseType()
    }

    private fun tryParseBlock(): List<Statement> {
        return mutableListOf<Statement>().apply {
            do {
                val statement = tryParseStatement()
                statement?.let { add(statement) }
            } while (statement != null)
        }
    }

    private fun tryParseStatement(): Statement? {
        return tryParseIfStatement()
            ?: tryParseWhileStatement()
            ?: tryParseReturnStatement()
            ?: tryParseVariableDeclaration()
            ?: tryParseExpressionStatement()
    }

    fun tryParseExpressionStatement(): Statement.ExpressionStatement? {
        val expression = tryParseDisjunctionExpression() ?: return null
        val assignedExpression = if (lexer.token.checkAndGoNext(Token.Special.Type.ASSIGN)) {
            tryParseDisjunctionExpression() ?: return null
        } else {
            null
        }
        return Statement.ExpressionStatement(expression, assignedExpression)
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
        val body = tryParseBlockWithBraces()
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.ELSE)) return Statement.IfStatement(
            condition = condition,
            body = body,
        )
        val elseBody = tryParseBlockWithBraces()
        return Statement.IfStatement(condition, body, elseBody)
    }

    fun tryParseVariableDeclaration(): Statement.VariableDeclaration? {
        var isImmutable = false
        val codePosition = lexer.getCodePosition()
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
            errorHandler.handleParserError(InvalidTypeException(codePosition))
            null
        }
        if (lexer.token?.type != TokenType.IDENTIFIER) {
            errorHandler.handleParserError(InvalidVariableDeclaration(codePosition))
        }
        val name = lexer.token?.value as String
        nextToken()
        if (!lexer.token.checkAndGoNext(Token.Special.Type.ASSIGN)) {
            return Statement.VariableDeclaration(
                isImmutable = isImmutable,
                name = name,
                type = type,
            )
        }
        val expression = tryParseDisjunctionExpression()
        return Statement.VariableDeclaration(
            isImmutable = isImmutable,
            name = name,
            type = type,
            initialValue = expression,
        )
    }

    fun tryParseFunctionParams(): List<Parameter> {
        if (lexer.token?.value == Token.Special.Type.RPAREN) {
            return emptyList()
        }
        if (!lexer.token.isTypeKeyword()) {
            errorHandler.handleParserError(InvalidTypeException(lexer.getCodePosition()))
        }
        val parameters = mutableListOf<Parameter>()
        while (lexer.token?.value != Token.Special.Type.RPAREN) { // TODO refactor while
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
        val body = tryParseBlockWithBraces()
        return Statement.WhileStatement(condition ?: return null, body)
    }

    fun tryParseReturnStatement(): Statement.ReturnStatement? {
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.RETURN)) return null

        val expression = tryParseDisjunctionExpression()
        return Statement.ReturnStatement(expression)
    }

    private fun tryParseDisjunctionExpression(): Expression? {
        val left = tryParseConjunctionExpression() ?: return null
        val right = mutableListOf<Expression>()
        while (lexer.token is Token.Disjunction) {
            nextToken()
            tryParseConjunctionExpression()?.let {
                right.add(it)
            } ?: run {
                errorHandler.handleParserError(MissingExpressionException(lexer.getCodePosition()))
            }
        }
        return if (right.isEmpty()) left else Expression.DisjunctionExpression(left, right)
    }

    private fun tryParseConjunctionExpression(): Expression? {
        val left = tryParseComparisonExpression() ?: return null
        val right = mutableListOf<Expression>()
        while (lexer.token is Token.Conjunction) {
            nextToken()
            tryParseComparisonExpression()?.let {
                right.add(it)
            } ?: run {
                errorHandler.handleParserError(MissingExpressionException(lexer.getCodePosition()))
            }
        }
        return if (right.isEmpty()) left else Expression.ConjunctionExpression(left, right)
    }

    private fun tryParseComparisonExpression(): Expression? {
        val left = tryParseNullSafetyExpression() ?: return null
        val operator = ComparisonOperator.fromToken(lexer.token) ?: return left
        nextToken()
        val right = tryParseNullSafetyExpression()
        if (right == null) {
            errorHandler.handleParserError(MissingExpressionException(lexer.getCodePosition()))
            return null
        } else {
            return Expression.ComparisonExpression(left, operator, right)
        }
    }

    private fun tryParseNullSafetyExpression(): Expression? {
        val left = tryParseAdditiveExpression() ?: return null
        if (!lexer.token.checkAndGoNext("?:")) return left
        val expression = tryParseDisjunctionExpression()
        if (expression == null) {
            errorHandler.handleParserError(InvalidExpressionException(lexer.getCodePosition()))
            return null
        }
        return Expression.NullSafetyExpression(left, expression)
    }

    private fun tryParseAdditiveExpression(): Expression? {
        val left = tryParseMultiplicativeExpression() ?: return null
        val right = mutableListOf<AdditiveOperatorWithExpression>()
        while (lexer.token.isAdditiveOperator()) {
            val operator = AdditiveOperator.fromToken(lexer.token) ?: return null
            nextToken()
            tryParseMultiplicativeExpression()?.let {
                right.add(AdditiveOperatorWithExpression(operator, it)) // TODO maybe refactor
            } ?: run {
                errorHandler.handleParserError(MissingExpressionException(lexer.getCodePosition()))
            }
        }
        if (right.isEmpty()) {
            return left
        }
        return Expression.AdditiveExpression(left, right)
    }

    private fun tryParseMultiplicativeExpression(): Expression? {
        val left = tryParseAsExpression() ?: return null
        val right = mutableListOf<MultiplicativeOperatorWithExpression>()
        while (lexer.token.isMultiplicativeOperator()) {
            val operator = when (lexer.token?.value) {
                Token.MultiplicativeOperator.Type.MULTIPLY -> MultiplicativeOperator.Mul
                Token.MultiplicativeOperator.Type.DIVIDE -> MultiplicativeOperator.Div
                else -> MultiplicativeOperator.Mod
            }
            nextToken()
            tryParseAsExpression()?.let {
                right.add(MultiplicativeOperatorWithExpression(operator, it))
            } ?: run {
                errorHandler.handleParserError(MissingExpressionException(lexer.getCodePosition()))
            }
        }
        if (right.isEmpty()) {
            return left
        }
        return Expression.MultiplicativeExpression(left, right)
    }

    private fun tryParseAsExpression(): Expression? {
        val unaryExpression = tryParseUnaryExpression() ?: return null
        if (!lexer.token.checkAndGoNext(Token.Keyword.Type.AS)) return unaryExpression
        val type = tryParseType() ?: run {
            errorHandler.handleParserError(InvalidTypeException(lexer.getCodePosition()))
            return null
        }
        return Expression.AsExpression(unaryExpression, type)
    }

    private fun tryParseUnaryOperator(): UnaryOperator? {
        val operator = UnaryOperator.fromToken(lexer.token) ?: return null
        nextToken()
        return operator
    }

    private fun tryParseUnaryExpression(): Expression? {
        val operator = tryParseUnaryOperator()
        val expression = tryParseSimpleExpression()
        if (operator == null) {
            return expression
        } else if (expression == null) {
            errorHandler.handleParserError(MissingExpressionException(lexer.getCodePosition()))
            return null
        } else {
            return Expression.UnaryExpression(operator, expression)
        }
    }

    private fun tryParseSimpleExpression(): Expression? = when (val token = lexer.token) {
        is Token.Literal -> {
            val literal = token.value
            nextToken()
            Expression.SimpleExpression.Literal(literal)
        }

        is Token.Identifier -> {
            val identifier = token.value
            nextToken()
            if (lexer.token.checkAndGoNext(Token.Special.Type.LPAREN)) {
                val args = tryParseFunctionCallArgs() ?: emptyList()
                if (!lexer.token.checkAndGoNext(Token.Special.Type.RPAREN)) {
                    errorHandler.handleParserError(UnmatchedParenthesisException(lexer.getCodePosition()))
                }
                Expression.SimpleExpression.FunctionCall(identifier, args)
            } else {
                Expression.SimpleExpression.Identifier(identifier)
            }
        }

        else -> {
            if (lexer.token.checkAndGoNext(Token.Special.Type.LPAREN)) {
                val expression = tryParseDisjunctionExpression()
                if (!lexer.token.checkAndGoNext(Token.Special.Type.RPAREN)) {
                    errorHandler.handleParserError(UnmatchedParenthesisException(lexer.getCodePosition()))
                    null
                } else {
                    expression
                }
            } else {
                null
            }
        }
    }

    private fun tryParseFunctionCallArgs(): List<Expression>? {
        return mutableListOf(tryParseDisjunctionExpression() ?: return null).apply {
            while (lexer.token.checkAndGoNext(Token.Special.Type.COMMA)) {
                tryParseDisjunctionExpression()?.let { add(it) } ?: errorHandler.handleParserError(
                    MissingExpressionException(lexer.getCodePosition())
                )
            }
        }
    }

    private fun tryParseBlockWithBraces(): List<Statement> {
        if (!lexer.token.checkAndGoNext(Token.Special.Type.LBRACE)) {
            errorHandler.handleParserError(InvalidFunctionDeclarationException(lexer.getCodePosition()))
        }
        val body = tryParseBlock()
        if (!lexer.token.checkAndGoNext(Token.Special.Type.RBRACE)) {
            errorHandler.handleParserError(InvalidFunctionDeclarationException(lexer.getCodePosition()))
        }
        return body
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

    private fun Token?.isAdditiveOperator() = this is Token.AdditiveOperator ||
            this?.value == Token.UnaryOperator.Type.MINUS
}
