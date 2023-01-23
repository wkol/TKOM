package org.example.lexer


import org.example.inputsource.InputSourceImpl
import org.example.lexer.exceptions.CommentLengthOverflow
import org.example.lexer.exceptions.DoublePrecisionOverflow
import org.example.lexer.exceptions.IdentifierLengthOverflow
import org.example.lexer.exceptions.InvalidStringChar
import org.example.lexer.exceptions.LexerException
import org.example.lexer.exceptions.NumberOverflow
import org.example.lexer.exceptions.UnclosedQuoteString
import org.example.lexer.exceptions.UnexpectedChar
import org.example.lexer.token.Token
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class TokenTypesLexerTest {

    @Test
    fun `EOF token`() {
        val string = "     "
        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.EOF)
    }

    @Test
    fun `Integer token`() {
        val string = "123 0"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals(lexer.token?.value, 123)
        assertEquals((lexer.token as Token.Literal).literalType, Token.Literal.Type.INTEGER)
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals(lexer.token?.value, 0)
        assertEquals((lexer.token as Token.Literal).literalType, Token.Literal.Type.INTEGER)
    }

    @Test
    fun `Integer overflow token`() {
        val string = "\n 123"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig(maxIntegerValue = 100))
        assertThrows<NumberOverflow>("Number overflow at position Line: 1 Column: 1 Max value is 100") { lexer.getNextToken() }
    }

    @Test
    fun `Double token`() {
        val string = "123.456 0.0"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals(lexer.token?.value, 123.456)
        assertEquals((lexer.token as Token.Literal).literalType, Token.Literal.Type.DOUBLE)
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals(lexer.token?.value, 0.0)
        assertEquals((lexer.token as Token.Literal).literalType, Token.Literal.Type.DOUBLE)
    }

    @Test
    fun `Double precision overflow`() {
        val string = "123.456"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig(maxDoublePrecision = 2))
        assertThrows<DoublePrecisionOverflow>("Double precision overflow at position Line: 1 Column: 1 Max double precision is 2") { lexer.getNextToken() }
    }

    @Test
    fun `Identifier token`() {
        val string = """ __asdgsag_ashdhj
                abcdef
                bbbasdb
                _343
                ____hsagdh__216735
                aaa1
            """.trimIndent()
        val results = string.split("\n").map { it.trim() }

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        for (result in results) {
            lexer.getNextToken()
            assert(lexer.token is Token.Identifier)
            assertEquals(result, lexer.token?.value)
        }
    }


    @Test
    fun `Too long identifier token`() {
        val string = "_aaaaaaaa"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig(maxIdentifierLength = 5))
        assertThrows<IdentifierLengthOverflow>("Identifier length overflow at position Line: 1 Column: 1 Maximum length is 5") { lexer.getNextToken() }
    }

    @Test
    fun `String literal token`() {
        val string = "\"Hello world\""

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals("Hello world", lexer.token?.value)
        assertEquals((lexer.token as Token.Literal).literalType, Token.Literal.Type.STRING)
    }

    @Test
    fun `Too long string literal`() {
        val string = "\"Hello world\""

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig(maxStringLength = 5))
        assertThrows<LexerException>("String length overflow at position Line: 1 Column: 1. Max length is 5") { lexer.getNextToken() }
    }

    @Test
    fun `String literal with escaped token`() {
        val string = """ "Hello \t world" """

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals(lexer.token?.value, "Hello \t world")
        assertEquals((lexer.token as Token.Literal).literalType, Token.Literal.Type.STRING)
    }

    @Test
    fun `Unknown escaped character`() {
        val string = """ "Hello \x world" """

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        assertThrows<InvalidStringChar>("Invalid string char at position Line: 1 Column: 8 Char: x") { lexer.getNextToken() }
    }

    @Test
    fun `Unclosed string literal`() {
        val string = """ "Hello world """

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        assertThrows<UnclosedQuoteString>("Unclosed quote string at position Line: 1 Column: 1") { lexer.getNextToken() }
    }

    @Test
    fun `Null token`() {
        val string = "null"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals(lexer.token?.value, null)
        assertEquals((lexer.token as Token.Literal).literalType, Token.Literal.Type.NULL)
    }

    @Test
    fun `Null safety operator token`() {
        val string = "?:"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.NullSafetyOperator)
    }

    @Test
    fun `Additive operators token`() {
        val string = "+"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.AdditiveOperator)
        assertEquals(lexer.token?.value, "+")
    }

    @Test
    fun `Multiplicative operator token`() {
        val string = "* / %"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.MultiplicativeOperator)
        assertEquals(
            lexer.token?.value, Token.MultiplicativeOperator.Type.MULTIPLY
        )
        lexer.getNextToken()
        assert(lexer.token is Token.MultiplicativeOperator)
        assertEquals(lexer.token?.value, Token.MultiplicativeOperator.Type.DIVIDE)
        lexer.getNextToken()
        assert(lexer.token is Token.MultiplicativeOperator)
        assertEquals(lexer.token?.value, Token.MultiplicativeOperator.Type.MODULO)
    }

    @Test
    fun `Comparison operators token`() {
        val string = "== != > < >= <="

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.ComparisonOperator)
        assertEquals(lexer.token?.value, Token.ComparisonOperator.Type.EQUAL)
        lexer.getNextToken()
        assert(lexer.token is Token.ComparisonOperator)
        assertEquals(lexer.token?.value, Token.ComparisonOperator.Type.NOT_EQUAL)
        lexer.getNextToken()
        assert(lexer.token is Token.ComparisonOperator)
        assertEquals(lexer.token?.value, Token.ComparisonOperator.Type.GREATER)
        lexer.getNextToken()
        assert(lexer.token is Token.ComparisonOperator)
        assertEquals(lexer.token?.value, Token.ComparisonOperator.Type.LESS)
        lexer.getNextToken()
        assert(lexer.token is Token.ComparisonOperator)
        assertEquals(
            lexer.token?.value, Token.ComparisonOperator.Type.GREATER_OR_EQUAL
        )
        lexer.getNextToken()
        assert(lexer.token is Token.ComparisonOperator)
        assertEquals(lexer.token?.value, Token.ComparisonOperator.Type.LESS_OR_EQUAL)
    }

    @Test
    fun `Special operators token`() {
        val string = "? , () {} ="

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Special)
        assertEquals(lexer.token?.value, Token.Special.Type.QUESTION_MARK)
        lexer.getNextToken()
        assert(lexer.token is Token.Special)
        assertEquals(lexer.token?.value, Token.Special.Type.COMMA)
        lexer.getNextToken()
        assert(lexer.token is Token.Special)
        assertEquals(lexer.token?.value, Token.Special.Type.LPAREN)
        lexer.getNextToken()
        assert(lexer.token is Token.Special)
        assertEquals(lexer.token?.value, Token.Special.Type.RPAREN)
        lexer.getNextToken()
        assert(lexer.token is Token.Special)
        assertEquals(lexer.token?.value, Token.Special.Type.LBRACE)
        lexer.getNextToken()
        assert(lexer.token is Token.Special)
        assertEquals(lexer.token?.value, Token.Special.Type.RBRACE)
        lexer.getNextToken()
        assert(lexer.token is Token.Special)
        assertEquals(lexer.token?.value, Token.Special.Type.ASSIGN)
    }

    @Test
    fun `Conjunction operator token`() {
        val string = "&&"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Conjunction)
    }

    @Test
    fun `Disjunction operator token`() {
        val string = "||"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Disjunction)
    }

    @Test
    fun `Unary operators token`() {
        val string = "! -"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.UnaryOperator)
        assertEquals(lexer.token?.value, Token.UnaryOperator.Type.NOT)
        lexer.getNextToken()
        assert(lexer.token is Token.UnaryOperator)
        assertEquals(lexer.token?.value, Token.UnaryOperator.Type.MINUS)
    }

    @Test
    fun `As operator token`() {
        val string = "as"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assertEquals(lexer.token?.value, Token.Keyword.Type.AS)
    }

    @Test
    fun `Keywords tokens`() {
        val string = "if else while return fun var const bool int double string true false"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.IF)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.ELSE)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.WHILE)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.RETURN)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.FUN)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.VAR)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.CONST)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.BOOL)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.INT)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.DOUBLE)
        lexer.getNextToken()
        assert(lexer.token is Token.Keyword)
        assertEquals(lexer.token?.value, Token.Keyword.Type.STRING)
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals(lexer.token?.value, true)
        assert(lexer.token?.value == true)
        lexer.getNextToken()
        assert(lexer.token is Token.Literal)
        assertEquals(lexer.token?.value, false)
    }

    @Test
    fun `Fun return type arrow token`() {
        val string = "->"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.FunReturnTypeArrow)
    }

    @Test
    fun `Comment token`() {
        val string = "// comment"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.token is Token.Comment)
        assertEquals(" comment", lexer.token?.value)
    }

    @Test
    fun `Comment length overflow`() {
        val string = "// aaaaaaaa"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig(maxCommentLength = 5))
        assertThrows<CommentLengthOverflow>("Comment length overflow at position Line: 1 Column: 1 Max comment length is 5") { lexer.getNextToken() }
    }

    @Test
    fun `Unexpected char`() {
        val string = "  #"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        assertThrows<UnexpectedChar>("Unexpected character at position Line: 1 Column: 3") { lexer.getNextToken() }
    }
}
