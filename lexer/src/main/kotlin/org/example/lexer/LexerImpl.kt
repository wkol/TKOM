package org.example.lexer

import org.example.inputsource.EOF
import org.example.inputsource.InputSource
import org.example.lexer.exceptions.CommentLengthOverflow
import org.example.lexer.exceptions.DoublePrecisionOverflow
import org.example.lexer.exceptions.IdentifierLengthOverflow
import org.example.lexer.exceptions.InvalidStringChar
import org.example.lexer.exceptions.NumberOverflow
import org.example.lexer.exceptions.StringLengthOverflow
import org.example.lexer.exceptions.UnclosedQuoteString
import org.example.lexer.exceptions.UnexpectedChar

import org.example.lexer.token.Token
import kotlin.math.pow
import org.example.lexer.utils.isIdentifierChar
import org.example.lexer.utils.isLogicalOperatorChar
import org.example.lexer.utils.isSpecialChar
import org.example.lexer.utils.isEscapedChar
import kotlin.jvm.Throws

class LexerImpl(
    private val inputSource: InputSource,
    private val config: LexerConfig
) : Lexer() {

    override var lastToken: Token? = null

    override fun getNextToken() {
        skipWhitespaces()
        if (inputSource.peekNextChar() == EOF) {
            lastToken = Token.EOF(inputSource.getPosition())
            return
        }
        if (tryBuildComment() ||
            tryBuildNumber() ||
            tryBuildString() ||
            tryBuildFunReturnTypeArrow() ||
            tryBuildComparisonOrAssignmentOperator() ||
            tryBuildUnaryOperator() ||
            tryBuildSpecialToken() ||
            tryBuildNullSafetyOperator() ||
            tryBuildLogicalOperator() ||
            tryBuildAdditiveOperator() ||
            tryBuildMultiplicativeOperator() ||
            tryBuildIdentifierOrKeyword()
        ) {
            return
        }
        val position = inputSource.getPosition()
        throw UnexpectedChar(inputSource.peekNextChar(), position)
    }

    fun getAllTokens(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (lastToken !is Token.EOF) {
            getNextToken()
            tokens.add(lastToken ?: throw Exception("Unexpected end of file"))
        }
        return tokens
    }

    private fun skipWhitespaces() {
        while (inputSource.peekNextChar().isWhitespace()) {
            inputSource.getNextChar()
        }
    }

    private fun tryBuildComment(): Boolean {
        if (inputSource.peekNextChars(2) != "//") return false
        val start = inputSource.getPosition()
        inputSource.getNextChar()
        inputSource.getNextChar()
        val value = StringBuilder()
        while (inputSource.peekNextChar() !in listOf('\n', EOF)) {
            value.append(inputSource.getNextChar())
            if (value.length > config.maxCommentLength) {
                throw CommentLengthOverflow(start, config.maxCommentLength)
            }
        }
        lastToken = Token.Comment(value.toString(), start)
        return true
    }

    @Throws(NumberOverflow::class)
    private fun tryBuildNumber(): Boolean {
        if (!inputSource.peekNextChar().isDigit()) {
            return false
        }
        val start = inputSource.getPosition()
        var value = 0.0
        if (inputSource.peekNextChar() != '0') {
            while (inputSource.peekNextChar().isDigit()) {
                value = value * 10 + inputSource.getNextChar().digitToInt()
                if (value > config.maxIntegerValue) {
                    throw NumberOverflow(start, config.maxIntegerValue)
                }
            }
        } else {
            inputSource.getNextChar()
        }
        if (inputSource.peekNextChar() == '.') {
            inputSource.getNextChar()
            var fractionPart = 0
            var fractionPartLength = 0
            while (inputSource.peekNextChar().isDigit()) {
                fractionPart = fractionPart * 10 + inputSource.getNextChar().digitToInt()
                fractionPartLength++
                if (fractionPartLength > config.maxDoublePrecision) {
                    throw DoublePrecisionOverflow(start, config.maxDoublePrecision)
                }
            }
            value += fractionPart / 10.0.pow(fractionPartLength)
            if (inputSource.peekNextChar().isLetter()) {
                throw UnexpectedChar(inputSource.peekNextChar(), inputSource.getPosition())
            }
            lastToken = Token.Literal(value, start, Token.Literal.Type.DOUBLE)
            return true
        }
        if (inputSource.peekNextChar().isLetter()) {
            throw UnexpectedChar(inputSource.peekNextChar(), inputSource.getPosition())
        }
        lastToken = Token.Literal(value.toInt(), start, Token.Literal.Type.INTEGER)
        return true
    }

    @Throws(IdentifierLengthOverflow::class)
    private fun tryBuildIdentifierOrKeyword(): Boolean {
        if (inputSource.peekNextChar().isDigit()) {
            return false
        }
        val start = inputSource.getPosition()
        val value = StringBuilder()
        while (inputSource.peekNextChar() == '_') {
            value.append(inputSource.getNextChar())
            if (value.length > config.maxIdentifierLength) {
                throw IdentifierLengthOverflow(start, config.maxIdentifierLength)
            }
        }
        if (!inputSource.peekNextChar().isLetterOrDigit()) {
            return false
        }
        while (inputSource.peekNextChar().isIdentifierChar()) {
            value.append(inputSource.getNextChar())
            if (value.length > config.maxIdentifierLength) {
                throw IdentifierLengthOverflow(start, config.maxIdentifierLength)
            }
        }
        lastToken = when (val strValue = value.toString()) {
            "return" -> Token.Keyword(Token.Keyword.Type.RETURN, start)
            "if" -> Token.Keyword(Token.Keyword.Type.IF, start)
            "else" -> Token.Keyword(Token.Keyword.Type.ELSE, start)
            "while" -> Token.Keyword(Token.Keyword.Type.WHILE, start)
            "bool" -> Token.Keyword(Token.Keyword.Type.BOOL, start)
            "int" -> Token.Keyword(Token.Keyword.Type.INT, start)
            "double" -> Token.Keyword(Token.Keyword.Type.DOUBLE, start)
            "string" -> Token.Keyword(Token.Keyword.Type.STRING, start)
            "var" -> Token.Keyword(Token.Keyword.Type.VAR, start)
            "const" -> Token.Keyword(Token.Keyword.Type.CONST, start)
            "fun" -> Token.Keyword(Token.Keyword.Type.FUN, start)
            "null" -> Token.Literal(null, start, Token.Literal.Type.NULL)
            "as" -> Token.AsOperator(start)
            else -> Token.Identifier(strValue, start)
        }
        return true
    }

    @Throws(StringLengthOverflow::class, InvalidStringChar::class)
    private fun tryBuildString(): Boolean {
        val start = inputSource.getPosition()
        val value = StringBuilder()
        if (inputSource.peekNextChar() != '"') {
            return false
        }
        inputSource.getNextChar()
        while (inputSource.peekNextChar() != '"') {
            if (value.length > config.maxStringLength) {
                throw StringLengthOverflow(start, config.maxStringLength)
            }
            val nextChar = inputSource.peekNextChar()
            if (nextChar == '\\') {
                val escapedChar = tryBuildEscapeIdentifier()
                value.append(escapedChar)
            }
            if (nextChar in listOf('\n', EOF)) throw UnclosedQuoteString(start)
            value.append(inputSource.getNextChar())
        }

        inputSource.getNextChar()
        lastToken = Token.Literal(value.toString(), start, Token.Literal.Type.STRING)
        return true
    }

    @Throws(InvalidStringChar::class)
    private fun tryBuildEscapeIdentifier(): String {
        val value = StringBuilder()
        if (inputSource.peekNextChar() != '\\') {
            throw InvalidStringChar(inputSource.getPosition(), inputSource.peekNextChar())
        }
        inputSource.getNextChar()
        if (!inputSource.peekNextChar().isEscapedChar()) throw InvalidStringChar(inputSource.getPosition(), inputSource.peekNextChar())
        when (inputSource.getNextChar()) {
            'n' -> value.append('\n')
            't' -> value.append('\t')
            'r' -> value.append('\r')
            'b' -> value.append('\b')
            '\\' -> value.append('\\')
            '"' -> value.append('"')
        }
        return value.toString()
    }

    private fun tryBuildAdditiveOperator(): Boolean {
        val start = inputSource.getPosition()
        if (inputSource.peekNextChar() != '+') {
            return false
        }
        inputSource.getNextChar()
        lastToken = Token.AdditiveOperator(position = start)
        return true
    }

    private fun tryBuildMultiplicativeOperator(): Boolean {
        val start = inputSource.getPosition()
        if (inputSource.peekNextChar() !in listOf('*', '/', '%')) {
            return false
        }
        val operator = inputSource.getNextChar()
        lastToken = Token.MultiplicativeOperator(
            value = when (operator) {
                '*' -> Token.MultiplicativeOperator.Type.MULTIPLY
                '/' -> Token.MultiplicativeOperator.Type.DIVIDE
                else -> Token.MultiplicativeOperator.Type.MODULO
            },
            position = start
        )
        return true
    }

    private fun tryBuildNullSafetyOperator(): Boolean {
        val start = inputSource.getPosition()
        if (inputSource.peekNextChar() != '?') {
            return false
        }
        inputSource.getNextChar()
        if (inputSource.peekNextChar() != ':') {
            return false
        }
        inputSource.getNextChar()
        lastToken = Token.NullSafetyOperator(position = start)
        return true
    }

    private fun tryBuildSpecialToken(): Boolean {
        if (!inputSource.peekNextChar().isSpecialChar() || inputSource.peekNextChars(2) == "?:") {
            return false
        }
        val start = inputSource.getPosition()
        lastToken = when (inputSource.getNextChar()) {
            '(' -> Token.Special(value = Token.Special.Type.LPAREN, position = start)
            ')' -> Token.Special(value = Token.Special.Type.RPAREN, position = start)
            '{' -> Token.Special(value = Token.Special.Type.LBRACE, position = start)
            '}' -> Token.Special(value = Token.Special.Type.RBRACE, position = start)
            ',' -> Token.Special(value = Token.Special.Type.COMMA, position = start)
            else -> Token.Special(value = Token.Special.Type.QUESTION_MARK, position = start)
        }
        return true
    }

    private fun tryBuildUnaryOperator(): Boolean {
        if ((inputSource.peekNextChar() !in listOf('!', '-'))) {
            return false
        }
        val start = inputSource.getPosition()
        val operator = inputSource.getNextChar()
        lastToken = Token.UnaryOperator(
            value = if (operator == '-') Token.UnaryOperator.Type.MINUS else Token.UnaryOperator.Type.NOT,
            position = start
        )
        return true
    }

    private fun tryBuildComparisonOrAssignmentOperator(): Boolean {
        if (inputSource.peekNextChar() !in listOf('=', '!', '<', '>')) {
            return false
        }
        val start = inputSource.getPosition()
        val operator = inputSource.getNextChar()
        lastToken = when (operator) {
            '=' -> {
                if (inputSource.peekNextChar() != '=') {
                    Token.Special(Token.Special.Type.ASSIGN, start)
                } else {
                    inputSource.getNextChar()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.EQUAL,
                        position = start
                    )
                }
            }

            '!' -> {
                if (inputSource.peekNextChar() != '=') {
                    Token.UnaryOperator(Token.UnaryOperator.Type.NOT, start)
                } else {
                    inputSource.getNextChar()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.NOT_EQUAL,
                        position = start
                    )
                }
            }

            '<' -> {
                if (inputSource.peekNextChar() == '=') {
                    inputSource.getNextChar()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.LESS_OR_EQUAL,
                        position = start
                    )
                } else {
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.LESS,
                        position = start
                    )
                }
            }

            '>' -> {
                if (inputSource.peekNextChar() == '=') {
                    inputSource.getNextChar()
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.GREATER_OR_EQUAL,
                        position = start
                    )
                } else {
                    Token.ComparisonOperator(
                        value = Token.ComparisonOperator.Type.GREATER,
                        position = start
                    )
                }
            }

            else -> return false
        }
        return true
    }

    private fun tryBuildLogicalOperator(): Boolean {
        if (!inputSource.peekNextChar().isLogicalOperatorChar()) return false
        val position = inputSource.getPosition()
        val operator = inputSource.getNextChar()
        lastToken = when (operator) {
            '&' -> {
                if (inputSource.peekNextChar() != '&') {
                    return false
                }
                inputSource.getNextChar()
                Token.Conjunction(position)
            }

            else -> {
                if (inputSource.peekNextChar() != '|') {
                    return false
                }
                inputSource.getNextChar()
                Token.Disjunction(position)
            }
        }
        return true
    }

    private fun tryBuildFunReturnTypeArrow(): Boolean {
        if (inputSource.peekNextChars(2) != "->") {
            return false
        }
        val position = inputSource.getPosition()
        inputSource.getNextChar()
        inputSource.getNextChar()
        lastToken = Token.FunReturnTypeArrow(position)
        return true
    }
}