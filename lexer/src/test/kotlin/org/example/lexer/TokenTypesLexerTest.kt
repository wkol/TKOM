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
        assert(lexer.lastToken is Token.EOF)
    }

    @Test
    fun `Integer token`() {
        val string = "123"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Literal)
        assertEquals((lexer.lastToken as Token.Literal).value, 123)
        assertEquals((lexer.lastToken as Token.Literal).literalType, Token.Literal.Type.INTEGER)
    }

    @Test
    fun `Invalid integer token`() {
        val string = "123a"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        assertThrows<UnexpectedChar>("Unexpected character: a at position Line: 1 Column: 4") { lexer.getNextToken() }
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
        val string = "123.456"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Literal)
        assertEquals((lexer.lastToken as Token.Literal).value, 123.456)
        assertEquals((lexer.lastToken as Token.Literal).literalType, Token.Literal.Type.DOUBLE)
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
            assert(lexer.lastToken is Token.Identifier)
            assertEquals(result, (lexer.lastToken as Token.Identifier).value)
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
        assert(lexer.lastToken is Token.Literal)
        assertEquals("Hello world", (lexer.lastToken as Token.Literal).value)
        assertEquals((lexer.lastToken as Token.Literal).literalType, Token.Literal.Type.STRING)
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
        assert(lexer.lastToken is Token.Literal)
        assertEquals((lexer.lastToken as Token.Literal).value, "Hello \t world")
        assertEquals((lexer.lastToken as Token.Literal).literalType, Token.Literal.Type.STRING)
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
        assert(lexer.lastToken is Token.Literal)
        assertEquals((lexer.lastToken as Token.Literal).value, null)
        assertEquals((lexer.lastToken as Token.Literal).literalType, Token.Literal.Type.NULL)
    }

    @Test
    fun `Null safety operator token`() {
        val string = "?:"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.NullSafetyOperator)
    }

    @Test
    fun `Additive operators token`() {
        val string = "+"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.AdditiveOperator)
        assertEquals((lexer.lastToken as Token.AdditiveOperator).value, "+")
    }

    @Test
    fun `Multiplicative operator token`() {
        val string = "* / %"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.MultiplicativeOperator)
        assertEquals(
            (lexer.lastToken as Token.MultiplicativeOperator).value, Token.MultiplicativeOperator.Type.MULTIPLY
        )
        lexer.getNextToken()
        assert(lexer.lastToken is Token.MultiplicativeOperator)
        assertEquals((lexer.lastToken as Token.MultiplicativeOperator).value, Token.MultiplicativeOperator.Type.DIVIDE)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.MultiplicativeOperator)
        assertEquals((lexer.lastToken as Token.MultiplicativeOperator).value, Token.MultiplicativeOperator.Type.MODULO)
    }

    @Test
    fun `Comparison operators token`() {
        val string = "== != > < >= <="

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.ComparisonOperator)
        assertEquals((lexer.lastToken as Token.ComparisonOperator).value, Token.ComparisonOperator.Type.EQUAL)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.ComparisonOperator)
        assertEquals((lexer.lastToken as Token.ComparisonOperator).value, Token.ComparisonOperator.Type.NOT_EQUAL)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.ComparisonOperator)
        assertEquals((lexer.lastToken as Token.ComparisonOperator).value, Token.ComparisonOperator.Type.GREATER)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.ComparisonOperator)
        assertEquals((lexer.lastToken as Token.ComparisonOperator).value, Token.ComparisonOperator.Type.LESS)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.ComparisonOperator)
        assertEquals(
            (lexer.lastToken as Token.ComparisonOperator).value, Token.ComparisonOperator.Type.GREATER_OR_EQUAL
        )
        lexer.getNextToken()
        assert(lexer.lastToken is Token.ComparisonOperator)
        assertEquals((lexer.lastToken as Token.ComparisonOperator).value, Token.ComparisonOperator.Type.LESS_OR_EQUAL)
    }

    @Test
    fun `Special operators token`() {
        val string = "? , () {} ="

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Special)
        assertEquals((lexer.lastToken as Token.Special).value, Token.Special.Type.QUESTION_MARK)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Special)
        assertEquals((lexer.lastToken as Token.Special).value, Token.Special.Type.COMMA)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Special)
        assertEquals((lexer.lastToken as Token.Special).value, Token.Special.Type.LPAREN)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Special)
        assertEquals((lexer.lastToken as Token.Special).value, Token.Special.Type.RPAREN)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Special)
        assertEquals((lexer.lastToken as Token.Special).value, Token.Special.Type.LBRACE)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Special)
        assertEquals((lexer.lastToken as Token.Special).value, Token.Special.Type.RBRACE)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Special)
        assertEquals((lexer.lastToken as Token.Special).value, Token.Special.Type.ASSIGN)
    }

    @Test
    fun `Conjunction operator token`() {
        val string = "&&"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Conjunction)
    }

    @Test
    fun `Disjunction operator token`() {
        val string = "||"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Disjunction)
    }

    @Test
    fun `Unary operators token`() {
        val string = "! -"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.UnaryOperator)
        assertEquals((lexer.lastToken as Token.UnaryOperator).value, Token.UnaryOperator.Type.NOT)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.UnaryOperator)
        assertEquals((lexer.lastToken as Token.UnaryOperator).value, Token.UnaryOperator.Type.MINUS)
    }

    @Test
    fun `As operator token`() {
        val string = "as"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.AsOperator)
    }

    @Test
    fun `Keywords tokens`() {
        val string = "if else while return fun var const bool int double string"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.IF)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.ELSE)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.WHILE)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.RETURN)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.FUN)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.VAR)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.CONST)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.BOOL)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.INT)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.DOUBLE)
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Keyword)
        assertEquals((lexer.lastToken as Token.Keyword).value, Token.Keyword.Type.STRING)
    }

    @Test
    fun `Fun return type arrow token`() {
        val string = "->"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.FunReturnTypeArrow)
    }

    @Test
    fun `Comment token`() {
        val string = "// comment"

        val inputSource = InputSourceImpl.fromString(string)
        val lexer = LexerImpl(inputSource, LexerConfig())
        lexer.getNextToken()
        assert(lexer.lastToken is Token.Comment)
        assertEquals(" comment", (lexer.lastToken as Token.Comment).value)
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
