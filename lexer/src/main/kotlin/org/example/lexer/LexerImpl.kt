package org.example.lexer

import org.example.errorhandler.ErrorHandler
import org.example.errorhandler.exception.lexer.CommentLengthOverflow
import org.example.errorhandler.exception.lexer.DoublePrecisionOverflow
import org.example.errorhandler.exception.lexer.IdentifierLengthOverflow
import org.example.errorhandler.exception.lexer.InvalidIdentifier
import org.example.errorhandler.exception.lexer.InvalidStringChar
import org.example.errorhandler.exception.lexer.NumberOverflow
import org.example.errorhandler.exception.lexer.StringLengthOverflow
import org.example.errorhandler.exception.lexer.UnclosedQuoteString
import org.example.errorhandler.exception.lexer.UnexpectedChar
import org.example.inputsource.CodePosition
import org.example.inputsource.EOF
import org.example.inputsource.InputSource
import org.example.lexer.token.Token
import org.example.lexer.utils.isIdentifierChar
import org.example.lexer.utils.isLogicalOperatorChar
import org.example.lexer.utils.isSpecialChar
import kotlin.math.pow

class LexerImpl(
    private val inputSource: InputSource,
    override val errorHandler: ErrorHandler,
    override val config: LexerConfig = LexerConfig()
) : Lexer() {

    private var codePosition = inputSource.getPosition()

    override var token: Token? = null

    override fun getNextToken() {
        skipWhitespaces()
        codePosition = inputSource.getPosition()
        if (inputSource.currentChar == EOF) {
            token = Token.EOF(inputSource.getPosition())
            return
        }

        if (tryBuildCommentOrDivision() ||
            tryBuildNumber() ||
            tryBuildString() ||
            tryBuildUnaryOperatorOrFunctionArrow() ||
            tryBuildComparisonOrAssignmentOperator() ||
            tryBuildSpecialTokenOrNullSafety() ||
            tryBuildLogicalOperator() ||
            tryBuildAdditiveOperator() ||
            tryBuildMultiplicativeOperator() ||
            tryBuildIdentifierOrKeyword()
        ) {
            return
        }
        val position = inputSource.getPosition()
        errorHandler.handleLexerError(UnexpectedChar(position, inputSource.currentChar))
        return
    }

    override fun getCodePosition(): CodePosition {
        return codePosition.copy()
    }

    fun getAllTokens(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (token !is Token.EOF) {
            getNextToken()
            tokens.add(token!!)
        }
        return tokens
    }

    private fun skipWhitespaces() {
        while (inputSource.currentChar.isWhitespace()) {
            inputSource.consumeCharacter()
        }
    }

    private fun tryBuildCommentOrDivision(): Boolean {
        if (inputSource.currentChar != '/') return false

        if (inputSource.consumeCharacter() != '/') {
            token = Token.MultiplicativeOperator(Token.MultiplicativeOperator.Type.DIVIDE, codePosition)
            return true
        }
        val value = StringBuilder()
        while (inputSource.consumeCharacter() !in listOf('\n', EOF)) {
            if (value.length >= config.maxCommentLength) {
                errorHandler.handleLexerError(
                    CommentLengthOverflow(codePosition, config.maxCommentLength)
                )
                break
            }
            value.append(inputSource.currentChar)
        }
        token = Token.Comment(value.toString(), codePosition)
        return true
    }

    private fun tryBuildNumber(): Boolean {
        if (!inputSource.currentChar.isDigit()) {
            return false
        }

        var value = inputSource.currentChar.digitToInt()
        if (inputSource.currentChar != '0') {
            while (inputSource.consumeCharacter().isDigit()) {
                if ((config.maxIntegerValue - inputSource.currentChar.digitToInt()) / 10 < value) {
                    errorHandler.handleLexerError(
                        NumberOverflow(codePosition, config.maxIntegerValue)
                    )
                }
                value = value * 10 + inputSource.currentChar.digitToInt()
            }
        } else {
            inputSource.consumeCharacter()
        }
        if (inputSource.currentChar != '.') {
            token = Token.Literal(value, codePosition, Token.Literal.Type.INTEGER)
            return true
        }
        var fractionPart = 0
        var fractionPartLength = 0
        while (inputSource.consumeCharacter().isDigit()) {
            if (fractionPartLength >= config.maxDoublePrecision) {
                errorHandler.handleLexerError(
                    DoublePrecisionOverflow(codePosition, config.maxDoublePrecision)
                )
            }
            fractionPart = fractionPart * 10 + inputSource.currentChar.digitToInt()
            fractionPartLength++
        }
        token =
            Token.Literal(value + fractionPart / 10.0.pow(fractionPartLength), codePosition, Token.Literal.Type.DOUBLE)
        return true
    }

    @Throws(IdentifierLengthOverflow::class)
    private fun tryBuildIdentifierOrKeyword(): Boolean {
        if (!inputSource.currentChar.isLetter() && inputSource.currentChar != '_') {
            return false
        }

        val value = StringBuilder(inputSource.currentChar.toString())
        while (inputSource.consumeCharacter() == '_') {
            if (value.length >= config.maxIdentifierLength) {
                errorHandler.handleLexerError(
                    IdentifierLengthOverflow(codePosition, config.maxIdentifierLength)
                )
                break
            }
            value.append(inputSource.currentChar)
        }
        if (!inputSource.currentChar.isLetterOrDigit()) {
            return if (value.last() == '_') {
                errorHandler.handleLexerError(
                    InvalidIdentifier(codePosition, value.toString())
                )
                true
            } else {
                token = Token.Identifier(value.toString(), codePosition)
                true
            }
        }
        value.append(inputSource.currentChar)
        while (inputSource.consumeCharacter().isIdentifierChar()) {
            if (value.length >= config.maxIdentifierLength) {
                errorHandler.handleLexerError(
                    IdentifierLengthOverflow(codePosition, config.maxIdentifierLength)
                )
                break
            }
            value.append(inputSource.currentChar)
        }
        token = when (val strValue = value.toString()) {
            "return" -> Token.Keyword(Token.Keyword.Type.RETURN, codePosition)
            "if" -> Token.Keyword(Token.Keyword.Type.IF, codePosition)
            "else" -> Token.Keyword(Token.Keyword.Type.ELSE, codePosition)
            "while" -> Token.Keyword(Token.Keyword.Type.WHILE, codePosition)
            "bool" -> Token.Keyword(Token.Keyword.Type.BOOL, codePosition)
            "int" -> Token.Keyword(Token.Keyword.Type.INT, codePosition)
            "double" -> Token.Keyword(Token.Keyword.Type.DOUBLE, codePosition)
            "string" -> Token.Keyword(Token.Keyword.Type.STRING, codePosition)
            "var" -> Token.Keyword(Token.Keyword.Type.VAR, codePosition)
            "const" -> Token.Keyword(Token.Keyword.Type.CONST, codePosition)
            "fun" -> Token.Keyword(Token.Keyword.Type.FUN, codePosition)
            "null" -> Token.Literal(null, codePosition, Token.Literal.Type.NULL)
            "as" -> Token.Keyword(Token.Keyword.Type.AS, codePosition)
            "true" -> Token.Literal(true, codePosition, Token.Literal.Type.BOOLEAN)
            "false" -> Token.Literal(false, codePosition, Token.Literal.Type.BOOLEAN)
            else -> Token.Identifier(strValue, codePosition)
        }
        return true
    }

    private fun tryBuildString(): Boolean {
        if (inputSource.currentChar != '"') {
            return false
        }

        val value = StringBuilder()
        while (inputSource.consumeCharacter() != '"') {
            if (inputSource.currentChar in listOf('\n', EOF)) {
                errorHandler.handleLexerError(UnclosedQuoteString(codePosition))
                break
            }
            if (value.length >= config.maxStringLength) {
                errorHandler.handleLexerError(
                    StringLengthOverflow(codePosition, config.maxStringLength)
                )
                break
            }
            val nextChar = inputSource.currentChar
            if (nextChar == '\\') {
                val escapedChar = tryBuildEscapeIdentifier()
                value.append(escapedChar)
            } else {
                value.append(inputSource.currentChar)
            }
        }

        inputSource.consumeCharacter()
        token = Token.Literal(value.toString(), codePosition, Token.Literal.Type.STRING)
        return true
    }

    private fun tryBuildEscapeIdentifier(): Char? {
        if (inputSource.currentChar != '\\') {
            errorHandler.handleLexerError(
                InvalidStringChar(inputSource.getPosition(), inputSource.currentChar)
            )
        }
        return when (inputSource.consumeCharacter()) {
            'n' -> '\n'
            't' -> '\t'
            'r' -> '\r'
            'b' -> '\b'
            '\\' -> '\\'
            '"' -> '"'
            else -> {
                errorHandler.handleLexerError(
                    InvalidStringChar(inputSource.getPosition(), inputSource.currentChar)
                )
                null
            }
        }
    }

    private fun tryBuildAdditiveOperator(): Boolean {
        if (inputSource.currentChar !in listOf('+', '-')) {
            return false
        }

        token = Token.AdditiveOperator(
            value = when (inputSource.currentChar) {
                '+' -> Token.AdditiveOperator.Type.PLUS
                else -> Token.AdditiveOperator.Type.MINUS
            },
            position = codePosition
        )
        inputSource.consumeCharacter()
        return true
    }

    private fun tryBuildMultiplicativeOperator(): Boolean {

        if (inputSource.currentChar !in listOf('*', '/', '%')) {
            return false
        }
        token = Token.MultiplicativeOperator(
            value = when (inputSource.currentChar) {
                '*' -> Token.MultiplicativeOperator.Type.MULTIPLY
                '/' -> Token.MultiplicativeOperator.Type.DIVIDE
                else -> Token.MultiplicativeOperator.Type.MODULO
            },
            position = codePosition
        )
        inputSource.consumeCharacter()
        return true
    }

    private fun tryBuildSpecialTokenOrNullSafety(): Boolean {
        if (!inputSource.currentChar.isSpecialChar()) {
            return false
        }

        token = when (inputSource.currentChar) {
            '(' -> Token.Special(value = Token.Special.Type.LPAREN, position = codePosition)
            ')' -> Token.Special(value = Token.Special.Type.RPAREN, position = codePosition)
            '{' -> Token.Special(value = Token.Special.Type.LBRACE, position = codePosition)
            '}' -> Token.Special(value = Token.Special.Type.RBRACE, position = codePosition)
            ',' -> Token.Special(value = Token.Special.Type.COMMA, position = codePosition)
            else -> {
                if (inputSource.consumeCharacter() == ':') {
                    Token.NullSafetyOperator(position = codePosition)
                } else {
                    Token.Special(value = Token.Special.Type.QUESTION_MARK, position = codePosition)
                }
            }
        }
        inputSource.consumeCharacter()
        return true
    }

    private fun tryBuildUnaryOperatorOrFunctionArrow(): Boolean {
        if ((inputSource.currentChar !in listOf('!', '-'))) {
            return false
        }

        token = when (inputSource.currentChar) {
            '!' -> {
                if (inputSource.consumeCharacter() == '=') {
                    inputSource.consumeCharacter()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.NOT_EQUAL,
                        position = codePosition
                    )
                } else {
                    Token.UnaryOperator(
                        value = Token.UnaryOperator.Type.NOT,
                        position = codePosition
                    )
                }
            }

            else -> {
                if (inputSource.consumeCharacter() == '>') {
                    inputSource.consumeCharacter()
                    Token.FunReturnTypeArrow(codePosition)
                } else {
                    Token.UnaryOperator(Token.UnaryOperator.Type.MINUS, codePosition)
                }
            }
        }
        return true
    }

    private fun tryBuildComparisonOrAssignmentOperator(): Boolean {

        val operator = inputSource.currentChar
        token = when (operator) {
            '=' -> {
                inputSource.consumeCharacter()
                if (inputSource.currentChar != '=') {
                    inputSource.consumeCharacter()
                    Token.Special(Token.Special.Type.ASSIGN, codePosition)
                } else {
                    inputSource.consumeCharacter()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.EQUAL,
                        position = codePosition
                    )
                }
            }

            '!' -> {
                inputSource.consumeCharacter()
                if (inputSource.currentChar != '=') {
                    inputSource.consumeCharacter()
                    Token.UnaryOperator(Token.UnaryOperator.Type.NOT, codePosition)
                } else {
                    inputSource.consumeCharacter()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.NOT_EQUAL,
                        position = codePosition
                    )
                }
            }

            '<' -> {
                inputSource.consumeCharacter()

                if (inputSource.currentChar == '=') {
                    inputSource.consumeCharacter()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.LESS_OR_EQUAL,
                        position = codePosition
                    )
                } else {
                    inputSource.consumeCharacter()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.LESS,
                        position = codePosition
                    )
                }
            }

            '>' -> {
                inputSource.consumeCharacter()
                if (inputSource.currentChar == '=') {
                    inputSource.consumeCharacter()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.GREATER_OR_EQUAL,
                        position = codePosition
                    )
                } else {
                    inputSource.consumeCharacter()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.GREATER,
                        position = codePosition
                    )
                }
            }

            else -> return false
        }

        return true
    }

    private fun tryBuildLogicalOperator(): Boolean {
        if (!inputSource.currentChar.isLogicalOperatorChar()) return false
        val position = inputSource.getPosition()
        token = when (inputSource.currentChar) {
            '&' -> {
                if (inputSource.consumeCharacter() != '&') {
                    return false
                }
                Token.Conjunction(position)
            }

            else -> {
                if (inputSource.consumeCharacter() != '|') {
                    return false
                }
                Token.Disjunction(position)
            }
        }
        inputSource.consumeCharacter()
        return true
    }
}
