package org.example.parser.org.parser

import org.example.lexer.token.Token
import org.example.lexer.Lexer
import org.example.lexer.LexerConfig
import org.example.errorhandler.ErrorHandler
import org.example.inputsource.CodePosition
import org.example.parser.ParserImpl
import org.example.parser.data.Expression
import org.example.parser.data.Operator
import org.example.parser.data.Statement
import org.example.parser.data.Type
import org.junit.jupiter.api.Test
import java.lang.Exception
import kotlin.test.assertEquals

class ParserTest {

    private lateinit var parser: ParserImpl
    private lateinit var lexer: Lexer
    var errors: Int = 0


    private fun setUpParser(tokens: List<Token>) {
        errors = 0
        lexer = TestLexerMock(tokens.toMutableList())
        parser = ParserImpl(lexer, testHandler)
    }

    @Test
    fun parseReturnStatement() {
        val tokens = listOf(
            Token.Keyword(
                value = Token.Keyword.Type.RETURN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "a",
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.AS,
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.INT,
                position = CodePosition(0, 0, 0)
            ),
        )
        setUpParser(tokens)
        val parserResult = parser.tryParseReturnStatement()

        assertEquals(0, errors)
        assertEquals(
            Statement.ReturnStatement(
                expression = Expression.AsExpression(
                    left = Expression.SimpleExpression.Identifier(
                        identifier = "a",
                    ),
                    type = Type(
                        type = Type.TypePrimitive.INT,
                        isNullable = false
                    )
                )
            ),
            parserResult
        )
    }

    @Test
    fun parseAssignmentStatement() {
        val tokens = listOf(
            Token.Identifier(
                value = "a",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.ASSIGN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "b",
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.AS,
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.INT,
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.QUESTION_MARK,
                position = CodePosition(0, 0, 0)
            )
        )
        setUpParser(tokens)
        val parserResult = parser.tryParseAssignmentStatement()

        assertEquals(0, errors)
        assertEquals(
            Statement.AssignmentStatement(
                variable = "a",
                expression = Expression.AsExpression(
                    left = Expression.SimpleExpression.Identifier(
                        identifier = "b",
                    ),
                    type = Type(
                        type = Type.TypePrimitive.INT,
                        isNullable = true
                    )
                )
            ),
            parserResult
        )
    }

    @Test
    fun parseIfWithoutElseStatementTest() {
        val tokens = listOf(
            Token.Keyword(
                value = Token.Keyword.Type.IF,
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.LPAREN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "a",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.RPAREN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.LBRACE,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "b",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.RBRACE,
                position = CodePosition(0, 0, 0)
            )
        )
        setUpParser(tokens)
        val parserResult = parser.tryParseIfStatement()

        assertEquals(0, errors)
        assertEquals(
            Statement.IfStatement(
                condition = Expression.SimpleExpression.Identifier(
                    identifier = "a"
                ),
                body = listOf(
                    Statement.ExpressionStatement(
                        expression = Expression.SimpleExpression.Identifier(
                            identifier = "b"
                        )
                    )
                ),
                elseBody = null
            ),
            parserResult
        )
    }

    @Test
    fun parseIfWithElseStatement() {
        val tokens = listOf(
            Token.Keyword(
                value = Token.Keyword.Type.IF,
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.LPAREN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "a",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.RPAREN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.LBRACE,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "b",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.RBRACE,
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.ELSE,
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.LBRACE,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "c",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.RBRACE,
                position = CodePosition(0, 0, 0)
            )
        )
        setUpParser(tokens)
        val parserResult = parser.tryParseIfStatement()

        assertEquals(0, errors)
        assertEquals(
            Statement.IfStatement(
                condition = Expression.SimpleExpression.Identifier(
                    identifier = "a"
                ),
                body = listOf(
                    Statement.ExpressionStatement(
                        expression = Expression.SimpleExpression.Identifier(
                            identifier = "b"
                        )
                    )
                ),
                elseBody = listOf(
                    Statement.ExpressionStatement(
                        expression = Expression.SimpleExpression.Identifier(
                            identifier = "c"
                        )
                    )
                )
            ),
            parserResult
        )
    }

    @Test
    fun parseWhileStatement() {
        val tokens = listOf(
            Token.Keyword(
                value = Token.Keyword.Type.WHILE,
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.LPAREN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "a",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.RPAREN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.LBRACE,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "b",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.RBRACE,
                position = CodePosition(0, 0, 0)
            )
        )
        setUpParser(tokens)
        val parserResult = parser.tryParseWhileStatement()

        assertEquals(0, errors)
        assertEquals(
            Statement.WhileStatement(
                condition = Expression.SimpleExpression.Identifier(
                    identifier = "a"
                ),
                body = listOf(
                    Statement.ExpressionStatement(
                        expression = Expression.SimpleExpression.Identifier(
                            identifier = "b"
                        )
                    )
                )
            ),
            parserResult
        )
    }

    @Test
    fun parseVarDeclaration() {
        val tokens = listOf(
            Token.Keyword(
                value = Token.Keyword.Type.VAR,
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.INT,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "_a",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.ASSIGN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Literal(
                value = 1,
                literalType = Token.Literal.Type.INTEGER,
                position = CodePosition(0, 0, 0)
            )
        )
        setUpParser(tokens)
        val parserResult = parser.tryParseVariableDeclaration()

        assertEquals(0, errors)
        assertEquals(
            Statement.VariableDeclaration(
                type = Type(
                    type = Type.TypePrimitive.INT,
                    isNullable = false
                ),
                isImmutable = false,
                name = "_a",
                initialValue = Expression.SimpleExpression.Literal(
                    value = 1
                )
            ),
            parserResult
        )
    }

    @Test
    fun parseConstDeclaration() {
        val tokens = listOf(
            Token.Keyword(
                value = Token.Keyword.Type.CONST,
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.INT,
                position = CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                value = "_a",
                position = CodePosition(0, 0, 0)
            ),
            Token.Special(
                value = Token.Special.Type.ASSIGN,
                position = CodePosition(0, 0, 0)
            ),
            Token.Literal(
                value = 12.00,
                literalType = Token.Literal.Type.INTEGER,
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.AS,
                position = CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                value = Token.Keyword.Type.INT,
                position = CodePosition(0, 0, 0)
            )
        )
        setUpParser(tokens)
        val parserResult = parser.tryParseVariableDeclaration()

        assertEquals(0, errors)
        assertEquals(
            Statement.VariableDeclaration(
                type = Type(
                    type = Type.TypePrimitive.INT,
                    isNullable = false
                ),
                isImmutable = true,
                name = "_a",
                initialValue = Expression.AsExpression(
                    left = Expression.SimpleExpression.Literal(
                        value = 12.00
                    ),
                    type = Type(
                        type = Type.TypePrimitive.INT,
                        isNullable = false
                    )
                )
            ),
            parserResult
        )
    }

    @Test
    fun parametersListTest() {
        val tokens = listOf(
            Token.Keyword(
                Token.Keyword.Type.STRING,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "paramName1",
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.COMMA,
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.INT,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "paramName2",
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.RPAREN,
                CodePosition(0, 0, 0)
            ),
        )
        val parser = ParserImpl(TestLexerMock(tokens.toMutableList()), testHandler)
        val parserResult = parser.tryParseFunctionParams()

        assertEquals(2, parserResult.size)
        assertEquals("paramName1", parserResult[0].name)
        assertEquals(Type.TypePrimitive.STRING, parserResult[0].type.type)
        assertEquals("paramName2", parserResult[1].name)
        assertEquals(Type.TypePrimitive.INT, parserResult[1].type.type)
    }

    @Test
    fun emptyParametersListTest() {
        val tokens = listOf(
            Token.Special(
                Token.Special.Type.RPAREN,
                CodePosition(0, 0, 0)
            ),
        )
        val parser = ParserImpl(TestLexerMock(tokens.toMutableList()), testHandler)
        val parserResult = parser.tryParseFunctionParams()

        assertEquals(0, parserResult.size)
    }

    @Test
    fun parseFunctionReturnType() {
        val tokens = listOf(
            Token.Special(
                Token.Special.Type.LBRACE,
                CodePosition(0, 0, 0)
            ),
            Token.FunReturnTypeArrow(
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.INT,
                CodePosition(0, 0, 0)
            ),
            Token.FunReturnTypeArrow(
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.DOUBLE,
                CodePosition(0, 0, 0)
            ),
            Token.FunReturnTypeArrow(
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.BOOL,
                CodePosition(0, 0, 0)
            ),
            Token.FunReturnTypeArrow(
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.STRING,
                CodePosition(0, 0, 0)
            ),
        )

        val parser = ParserImpl(TestLexerMock(tokens.toMutableList()), testHandler)
        val parserResult = mutableListOf<Type?>().apply {
            add(parser.tryParseFunctionReturnType())
            parser.lexer.getNextToken()
            add(parser.tryParseFunctionReturnType())
            add(parser.tryParseFunctionReturnType())
            add(parser.tryParseFunctionReturnType())
            add(parser.tryParseFunctionReturnType())
        }

        assertEquals(Type.TypePrimitive.VOID, parserResult[0]!!.type)
        assertEquals(Type.TypePrimitive.INT, parserResult[1]!!.type)
        assertEquals(Type.TypePrimitive.DOUBLE, parserResult[2]!!.type)
        assertEquals(Type.TypePrimitive.BOOL, parserResult[3]!!.type)
        assertEquals(Type.TypePrimitive.STRING, parserResult[4]!!.type)
    }

    @Test
    fun parseFunctionTest() {
        val tokens = listOf(
            Token.Keyword(
                Token.Keyword.Type.FUN,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "funName",
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.LPAREN,
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.STRING,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "paramName1",
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.COMMA,
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.INT,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "paramName2",
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.RPAREN,
                CodePosition(0, 0, 0)
            ),
            Token.FunReturnTypeArrow(
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.INT,
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.LBRACE,
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.RETURN,
                CodePosition(0, 0, 0)
            ),

            Token.Literal(
                1,
                CodePosition(0, 0, 0),
                Token.Literal.Type.INTEGER
            ),

            Token.Special(
                Token.Special.Type.RBRACE,
                CodePosition(0, 0, 0)
            ),
        )

        val parser = ParserImpl(TestLexerMock(tokens.toMutableList()), testHandler)
        val parserOutCome = parser.parse()
        val parserResult = parserOutCome.functions["funName"]!!

        assertEquals("funName", parserResult.name)
        assertEquals(Type.TypePrimitive.INT, parserResult.returnType!!.type)
        assertEquals(2, parserResult.parameters.size)
        assertEquals("paramName1", parserResult.parameters[0].name)
        assertEquals(Type.TypePrimitive.STRING, parserResult.parameters[0].type.type)
        assertEquals("paramName2", parserResult.parameters[1].name)
        assertEquals(Type.TypePrimitive.INT, parserResult.parameters[1].type.type)
        assertEquals(1, parserResult.body.size)
    }

    @Test
    fun parseVarDeclarationTest() {
        val tokens = listOf(
            Token.Keyword(
                Token.Keyword.Type.VAR,
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.BOOL,
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.QUESTION_MARK,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "varName",
                CodePosition(0, 0, 0)
            ),
        )
        val parser = ParserImpl(TestLexerMock(tokens.toMutableList()), testHandler)
        val parserResult = parser.tryParseVariableDeclaration()
        assertEquals("varName", parserResult!!.name)
        assertEquals(Type.TypePrimitive.BOOL, parserResult.type!!.type)
        assertEquals(true, parserResult.type!!.isNullable)
        assertEquals(false, parserResult.isImmutable)
    }

    @Test
    fun parseConstDeclarationTest() {
        val tokens = listOf(
            Token.Keyword(
                Token.Keyword.Type.CONST,
                CodePosition(0, 0, 0)
            ),
            Token.Keyword(
                Token.Keyword.Type.BOOL,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "varName",
                CodePosition(0, 0, 0)
            ),
        )
        val parser = ParserImpl(TestLexerMock(tokens.toMutableList()), testHandler)
        val parserResult = parser.tryParseVariableDeclaration()
        assertEquals("varName", parserResult!!.name)
        assertEquals(Type.TypePrimitive.BOOL, parserResult.type!!.type)
        assertEquals(false, parserResult.type!!.isNullable)
        assertEquals(true, parserResult.isImmutable)
    }

    @Test
    fun parseFunctionCallTest() {
        val tokens = mutableListOf<Token?>(
            Token.Identifier(
                "funName",
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.LPAREN,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "paramName1",
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.COMMA,
                CodePosition(0, 0, 0)
            ),
            Token.Identifier(
                "paramName2",
                CodePosition(0, 0, 0)
            ),
            Token.Special(
                Token.Special.Type.RPAREN,
                CodePosition(0, 0, 0)
            ),
        )

        val parser = ParserImpl(TestLexerMock(tokens), testHandler)
        val parserResult = parser.tryParseExpressionStatement()!!
        assert(parserResult.expression is Expression.SimpleExpression.FunctionCall)

        with(parserResult.expression as Expression.SimpleExpression.FunctionCall) {
            assertEquals("funName", functionName)
            assertEquals(2, functionArgs.size)
            assertEquals("paramName1", (functionArgs[0] as Expression.SimpleExpression.Identifier).identifier)
            assertEquals("paramName2", (functionArgs[1] as Expression.SimpleExpression.Identifier).identifier)
        }
    }

    @Test
    fun parseIdentifierTest() {
        val tokens = mutableListOf<Token?>(
            Token.Identifier(
                "varName",
                CodePosition(0, 0, 0)
            ),
        )

        val parser = ParserImpl(TestLexerMock(tokens), testHandler)
        val parserResult = parser.tryParseExpressionStatement()!!
        assert(parserResult.expression is Expression.SimpleExpression.Identifier)
        assertEquals("varName", (parserResult.expression as Expression.SimpleExpression.Identifier).identifier)
    }

    @Test
    fun parseLiteralConstant() {
        val tokens = mutableListOf<Token?>(
            Token.Literal(
                value = true,
                position = CodePosition(0, 0, 0),
                literalType = Token.Literal.Type.BOOLEAN
            ),
        )

        val parser = ParserImpl(TestLexerMock(tokens), testHandler)
        val parserResult = parser.tryParseExpressionStatement()!!
        assert(parserResult.expression is Expression.SimpleExpression.Literal)

        assertEquals(true, (parserResult.expression as Expression.SimpleExpression.Literal).value)
    }

    @Test
    fun parseUnaryExpression() {
        val tokens = mutableListOf<Token?>(
            Token.UnaryOperator(
                value = Token.UnaryOperator.Type.MINUS,
                position = CodePosition(0, 0, 0)
            ),
            Token.Literal(
                value = 1,
                position = CodePosition(0, 0, 0),
                literalType = Token.Literal.Type.INTEGER
            ),
        )

        val parser = ParserImpl(TestLexerMock(tokens), testHandler)
        val parserResult = parser.tryParseExpressionStatement()!!
        assertEquals(Operator.Minus, (parserResult.expression as Expression.UnaryExpression).operator)
        assertEquals(1, ((parserResult.expression as Expression.UnaryExpression).right as Expression.SimpleExpression.Literal).value)
    }

    private val testHandler = object : ErrorHandler {
        override fun handleParserError(exception: Exception) {
            errors++
        }

        override fun handleLexerError(exception: Exception) {

        }
    }


    class TestLexerMock(private val tokens: MutableList<Token?>) : Lexer() {

        init {
            tokens.add(0, null)
        }

        override val config: LexerConfig
            get() = LexerConfig()
        override var token: Token? = null
            get() = tokens.firstOrNull() ?: Token.EOF(CodePosition(0, 0, 0))

        override fun getNextToken() {
            tokens.removeAt(0)
        }

        override fun getCodePosition(): CodePosition {
            return CodePosition(0, 0, 0)
        }
    }

}