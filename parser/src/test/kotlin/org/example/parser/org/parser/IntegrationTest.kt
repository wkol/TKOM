package org.example.parser.org.parser

import org.example.errorhandler.ErrorHandler
import org.example.inputsource.InputSource
import org.example.inputsource.InputSourceImpl
import org.example.lexer.Lexer
import org.example.lexer.LexerImpl
import org.example.parser.Parser
import org.example.parser.ParserImpl
import org.example.parser.data.Expression
import org.example.parser.data.OperatorWithExpression
import org.example.parser.data.Statement
import org.example.parser.data.Operator
import org.example.parser.data.Type
import org.example.parser.exception.DuplicateFunctionDeclarationException
import org.example.parser.exception.InvalidExpressionException
import org.example.parser.exception.InvalidTypeException
import org.example.parser.exception.ParserException
import org.example.parser.exception.UnmatchedParenthesisException
import org.junit.jupiter.api.Test
import java.lang.Exception
import kotlin.test.assertEquals

class IntegrationTest {

    private lateinit var inputSource: InputSource
    private lateinit var lexer: Lexer
    private lateinit var errorHandler: ErrorHandler
    private lateinit var parser: Parser
    private var errors = mutableListOf<ParserException>()

    private fun setUpLexer(input: String) {
        inputSource = InputSourceImpl.fromString(input)
        lexer = LexerImpl(inputSource)
        errors.clear()
        errorHandler = object : ErrorHandler {
            override fun handleParserError(exception: Exception) {
                errors.add(exception as ParserException)
            }

            override fun handleLexerError(exception: Exception) {
            }
        }
        parser = ParserImpl(lexer, errorHandler)
    }

    @Test
    fun sampleProgramTest() {
        val input = """
            fun average(double a, double b) -> double {
                return (a + b) / 2.0
            }
            
            fun printText(string text) {
                print("Exiting program with i = " + text )
            }
            
            fun main() {
                var int i = 0
                const double c = 2.0
                while(i < 3) {
                    print("Type a number")
                    const string input = readInput() ?: "0"
                    if (isNumber(input) && input as int > 0) {
                        const double number = input as double
                        print("The average of 2 and " + number + " is " + average(c, number))
                    } else {
                        print("The input is not a number")
                    }
                    i = i + 1 // incrementing i
                }
                printText(i as string)
            }
            
            main()
        """
        setUpLexer(input)
        val program = parser.parse()

        assertEquals(0, errors.size)
        assertEquals(1, program.statements.size)
        assertEquals(3, program.functions.size)
    }

    @Test
    fun multipleParenthesisTest() {
        val input = """
            var int i = (1.0 * ((0 + 1) * (2 + 3) as double) / 4) as int
            
        """.trimIndent()
        setUpLexer(input)
        val program = parser.parse()

        assertEquals(0, errors.size)
        assertEquals(1, program.statements.size)
        assertEquals(0, program.functions.size)
        assertEquals(
            Statement.VariableDeclaration(
                isImmutable = false,
                name = "i",
                type = Type(
                    type = Type.TypePrimitive.INT,
                    isNullable = false
                ),
                initialValue = Expression.AsExpression(
                    type = Type(
                        type = Type.TypePrimitive.INT,
                        isNullable = false
                    ),
                    left = Expression.SimpleExpression.ParenthesizedExpression(
                        expression = Expression.MultiplicativeExpression(
                            left = Expression.SimpleExpression.Literal(
                                value = 1.0,
                            ),
                            right = listOf(
                                OperatorWithExpression(
                                    operator = Operator.Mul,
                                    expression = Expression.SimpleExpression.ParenthesizedExpression(
                                        expression = Expression.MultiplicativeExpression(
                                            left = Expression.SimpleExpression.ParenthesizedExpression(
                                                expression = Expression.AdditiveExpression(
                                                    left = Expression.SimpleExpression.Literal(
                                                        value = 0,
                                                    ),
                                                    right = listOf(
                                                        OperatorWithExpression(
                                                            operator = Operator.Plus,
                                                            expression = Expression.SimpleExpression.Literal(
                                                                value = 1,
                                                            )
                                                        )
                                                    )
                                                ),
                                            ),
                                            right = listOf(
                                                OperatorWithExpression(
                                                    operator = Operator.Mul,
                                                    expression = Expression.AsExpression(
                                                        left = Expression.SimpleExpression.ParenthesizedExpression(
                                                            expression = Expression.AdditiveExpression(
                                                                left = Expression.SimpleExpression.Literal(
                                                                    value = 2,
                                                                ),
                                                                right = listOf(
                                                                    OperatorWithExpression(
                                                                        operator = Operator.Plus,
                                                                        expression = Expression.SimpleExpression.Literal(
                                                                            value = 3,
                                                                        )
                                                                    )
                                                                )
                                                            )
                                                        ),
                                                        type = Type(
                                                            type = Type.TypePrimitive.DOUBLE,
                                                            isNullable = false
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                ),
                                OperatorWithExpression(
                                    operator = Operator.Div,
                                    expression = Expression.SimpleExpression.Literal(
                                        value = 4
                                    )
                                )
                            )
                        )
                    )
                )

            ), program.statements[0]
        )
    }

    @Test
    fun functionCallTest() {
        val input = """
            fun main() {
                print("Hello world")
            }
            
            main()
        """.trimIndent()

        setUpLexer(input)
        val program = parser.parse()

        assertEquals(0, errors.size)
        assertEquals(1, program.statements.size)
        assertEquals(1, program.functions.size)
        assertEquals(
            Statement.ExpressionStatement(
                expression = Expression.SimpleExpression.FunctionCall(
                    functionName = "main",
                    functionArgs = listOf()
                )
            ), program.statements[0]
        )
    }

    @Test
    fun invalidTypeErrorTest() {
        val input = """
            var float i = 1.0
        """.trimIndent()

        setUpLexer(input)
        val program = parser.parse()

        assertEquals(1, errors.size)
        assert(errors[0] is InvalidTypeException)
    }

    @Test
    fun invalidExpressionTest() {
        val input = """
            var int i = (
        """.trimIndent()

        setUpLexer(input)
        val program = parser.parse()

        assertEquals(2, errors.size)
        assert(errors[0] is InvalidExpressionException)
        assert(errors[1] is UnmatchedParenthesisException)
    }

    @Test
    fun duplicateFunctionNamesTest() {
        val input = """
            fun main() {
                print("Hello world")
            }
            
            fun main() {
                print("Hello world")
            }
            
            main()
        """.trimIndent()

        setUpLexer(input)

        val program = parser.parse()

        assertEquals(1, errors.size)
        assert(errors[0] is DuplicateFunctionDeclarationException)
    }
}