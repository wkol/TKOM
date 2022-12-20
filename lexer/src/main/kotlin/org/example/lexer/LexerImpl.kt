package org.example.lexer

import org.example.inputsource.CodePosition
import org.example.inputsource.EOF
import org.example.inputsource.InputSource
import org.example.lexer.exceptions.CommentLengthOverflow
import org.example.lexer.exceptions.DoublePrecisionOverflow
import org.example.lexer.exceptions.IdentifierLengthOverflow
import org.example.lexer.exceptions.InvalidStringChar
import org.example.lexer.exceptions.LexerException
import org.example.lexer.exceptions.NumberOverflow
import org.example.lexer.exceptions.StringLengthOverflow
import org.example.lexer.exceptions.UnclosedQuoteString
import org.example.lexer.exceptions.UnexpectedChar

import org.example.lexer.token.Token
import kotlin.math.pow
import org.example.lexer.utils.isIdentifierChar
import org.example.lexer.utils.isLogicalOperatorChar
import org.example.lexer.utils.isSpecialChar
import kotlin.jvm.Throws

class LexerImpl(
    private val inputSource: InputSource,
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
        throw UnexpectedChar(inputSource.currentChar, position)
    }

    override fun getCodePosition(): CodePosition {
        return codePosition.copy()
    }

    @Throws(Exception::class)
    fun getAllTokens(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (token !is Token.EOF) {
            getNextToken()
            tokens.add(token ?: throw Exception("Unexpected end of file"))
        }
        return tokens
    }

    private fun skipWhitespaces() {
        while (inputSource.currentChar.isWhitespace()) {
            inputSource.consumeCharacter()
        }
    }

    @Throws(CommentLengthOverflow::class)
    // TODO Refactor this method to commentOrDivisi
    private fun tryBuildCommentOrDivision(): Boolean {
        if (inputSource.currentChar != '/') return false

        if (inputSource.consumeCharacter() != '/') {
            token = Token.MultiplicativeOperator(Token.MultiplicativeOperator.Type.DIVIDE, codePosition)
            return true
        }
        val value = StringBuilder()
        while (inputSource.consumeCharacter() !in listOf('\n', EOF)) {
            if (value.length >= config.maxCommentLength) {
                throw CommentLengthOverflow(codePosition, config.maxCommentLength)
            }
            value.append(inputSource.currentChar)
        }
        token = Token.Comment(value.toString(), codePosition)
        return true
    }

    @Throws(NumberOverflow::class)
    private fun tryBuildNumber(): Boolean {
        if (!inputSource.currentChar.isDigit()) {
            return false
        }

        var value = inputSource.currentChar.digitToInt()
        if (inputSource.currentChar != '0') {
            while (inputSource.consumeCharacter().isDigit()) {
                if ((config.maxIntegerValue - inputSource.currentChar.digitToInt()) / 10 < value) {
                    throw NumberOverflow(codePosition, config.maxIntegerValue)
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
                throw DoublePrecisionOverflow(codePosition, config.maxDoublePrecision)
            }
            fractionPart = fractionPart * 10 + inputSource.currentChar.digitToInt()
            fractionPartLength++
        }
        token = Token.Literal(value + fractionPart / 10.0.pow(fractionPartLength), codePosition, Token.Literal.Type.DOUBLE)
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
                throw IdentifierLengthOverflow(codePosition, config.maxIdentifierLength)
            }
            value.append(inputSource.currentChar)
        }
        if (!inputSource.currentChar.isLetterOrDigit()) {
            if (value.last() == '_') {
                throw LexerException("Invalid identifier", codePosition)
            } else {
                token = Token.Identifier(value.toString(), codePosition)
                return true
            }
        }
        value.append(inputSource.currentChar)
        while (inputSource.consumeCharacter().isIdentifierChar()) {
            if (value.length >= config.maxIdentifierLength) {
                throw IdentifierLengthOverflow(codePosition, config.maxIdentifierLength)
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

    @Throws(StringLengthOverflow::class, InvalidStringChar::class)
    private fun tryBuildString(): Boolean {
        if (inputSource.currentChar != '"') {
            return false
        }

        val value = StringBuilder()
        while (inputSource.consumeCharacter() != '"') {
            if (inputSource.currentChar in listOf('\n', EOF)) throw UnclosedQuoteString(codePosition)
            if (value.length >= config.maxStringLength) {
                throw StringLengthOverflow(codePosition, config.maxStringLength)
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

    @Throws(InvalidStringChar::class)
    private fun tryBuildEscapeIdentifier(): Char {
        if (inputSource.currentChar != '\\') {
            throw InvalidStringChar(inputSource.getPosition(), inputSource.currentChar)
        }
        return when (inputSource.consumeCharacter()) {
            'n' -> '\n'
            't' -> '\t'
            'r' -> '\r'
            'b' -> '\b'
            '\\' ->'\\'
            '"' -> '"'
            else -> throw InvalidStringChar(inputSource.getPosition(), inputSource.currentChar)
        }
    }

    private fun tryBuildAdditiveOperator(): Boolean {
        if (inputSource.currentChar != '+') {
            return false
        }

        inputSource.consumeCharacter()
        token = Token.AdditiveOperator(position = codePosition)
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
        // TODO token = actions[operator]
        token = when (operator) { // TODO add arrowTypeFunction
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