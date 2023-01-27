package org.example.interpreter

import org.example.errorhandler.ErrorHandler
import org.example.errorhandler.ErrorHandlerConfig
import org.example.errorhandler.exception.interpreter.FunctionNotFoundException
import org.example.errorhandler.exception.interpreter.ImmutableVariableException
import org.example.errorhandler.exception.interpreter.InterpreterException
import org.example.errorhandler.exception.interpreter.InvalidCastException
import org.example.errorhandler.exception.interpreter.InvalidFunctionArgumentsNumberException
import org.example.errorhandler.exception.interpreter.InvalidOperationException
import org.example.errorhandler.exception.interpreter.InvalidRuntimeTypeException
import org.example.errorhandler.exception.interpreter.NoReturnFromFunctionException
import org.example.errorhandler.exception.interpreter.VariableNotFoundException
import org.example.errorhandler.exception.interpreter.ZeroDivisionException
import org.example.errorhandler.exception.lexer.LexerException
import org.example.errorhandler.exception.parser.ParserException
import org.example.interpreter.utils.TypeConst
import org.example.parser.data.AdditiveOperator
import org.example.parser.data.AdditiveOperatorWithExpression
import org.example.parser.data.Expression
import org.example.parser.data.MultiplicativeOperator
import org.example.parser.data.MultiplicativeOperatorWithExpression
import org.example.parser.data.Parameter
import org.example.parser.data.Program
import org.example.parser.data.Statement
import org.example.parser.data.UserDefinedFunction
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class InterpreterTest {

    private val errors: MutableList<InterpreterException> = mutableListOf()
    private lateinit var errorHandler: ErrorHandler
    private lateinit var interpreter: Interpreter

    @BeforeEach
    fun setup() {
        errorHandler = object : ErrorHandler {
            override var errorCount: Int
                get() = errors.size
                set(_) {}
            override val errorHandlerConfig: ErrorHandlerConfig = ErrorHandlerConfig()

            override fun handleParserError(exception: ParserException) {
            }

            override fun handleLexerError(exception: LexerException) {

            }

            override fun handleInterpreterError(exception: InterpreterException) {
                errors.add(exception)
                throw Exception()
            }
        }
        interpreter = InterpreterImpl(
            errorHandler,
            StandardLibraryImpl()
        )
        errors.clear()
    }

    @Test
    fun assignInvalidTypeToVariableTest() {
        val program = Program(
            statements = listOf(
                Statement.VariableDeclaration(
                    isImmutable = false,
                    name = "b",
                    type = TypeConst.INT_NULLABLE,
                    initialValue = Expression.SimpleExpression.Literal(
                        value = 1
                    )
                ),
                Statement.ExpressionStatement(
                    expression = Expression.SimpleExpression.Identifier(
                        identifier = "b",
                    ),
                    assignedExpression = Expression.SimpleExpression.Literal(
                        value = "a"
                    )
                )
            ),
            functions = emptyMap()
        )

        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is InvalidRuntimeTypeException)
        assertEquals("int", (errors[0] as InvalidRuntimeTypeException).expected)
        assertEquals("string", (errors[0] as InvalidRuntimeTypeException).actual)
    }


    @Test
    fun functionNotFoundException() {
        val program = Program(
            statements = listOf(
                Statement.ExpressionStatement(
                    expression = Expression.SimpleExpression.FunctionCall(
                        identifier = "foo",
                        args = listOf()
                    )
                )
            ),
            functions = emptyMap()
        )

        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is FunctionNotFoundException)
        assertEquals("foo", (errors[0] as FunctionNotFoundException).name)
    }

    @Test
    fun invalidCastExceptionTest() {
        val program = Program(
            statements = listOf(
                Statement.ExpressionStatement(
                    expression = Expression.AsExpression(
                        left = Expression.SimpleExpression.Literal(
                            value = "foo"
                        ),
                        type = TypeConst.INT
                    )
                )
            ),
            functions = emptyMap()
        )

        assertThrows<Exception> { interpreter.interpret(program) }

        assertEquals(1, errors.size)
        assert(errors[0] is InvalidCastException)
        assertEquals("foo", (errors[0] as InvalidCastException).actual)
        assertEquals("int", (errors[0] as InvalidCastException).expected)
    }

    @Test
    fun invalidFunctionArgumentsNumber() {
        val program = Program(
            statements = listOf(
                Statement.ExpressionStatement(
                    expression = Expression.SimpleExpression.FunctionCall(
                        identifier = "foo",
                        args = listOf(
                            Expression.SimpleExpression.Literal(
                                value = "foo"
                            ),
                            Expression.SimpleExpression.Literal(
                                value = "bar"
                            )
                        )
                    )
                )
            ),
            functions = mapOf(
                "foo" to UserDefinedFunction(
                    name = "foo",
                    parameters = listOf(
                        Parameter("a", TypeConst.INT)
                    ),
                    returnType = TypeConst.VOID,
                    body = listOf()
                )
            )
        )

        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is InvalidFunctionArgumentsNumberException)
        assertEquals(1, (errors[0] as InvalidFunctionArgumentsNumberException).expected)
        assertEquals(2, (errors[0] as InvalidFunctionArgumentsNumberException).actual)
    }

    @Test
    fun invalidOperationConcatIntAndString() {
        val program = Program(
            statements = listOf(
                Statement.ExpressionStatement(
                    expression = Expression.AdditiveExpression(
                        left = Expression.SimpleExpression.Literal(
                            value = "foo"
                        ),
                        right = listOf(
                            AdditiveOperatorWithExpression(
                                operator = AdditiveOperator.Minus,
                                expression = Expression.SimpleExpression.Literal(
                                    value = "1"
                                )
                            )
                        ),
                    )
                )
            ),
            functions = emptyMap()
        )


        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is InvalidOperationException)
        assertEquals("-", (errors[0] as InvalidOperationException).operation)
        assertEquals("string", (errors[0] as InvalidOperationException).leftType)
    }

    @Test
    fun invalidTypeExceptionTest() {
        val program = Program(
            statements = listOf(
                Statement.ExpressionStatement(
                    expression = Expression.AdditiveExpression(
                        left = Expression.SimpleExpression.Literal(
                            value = 1
                        ),
                        right = listOf(
                            AdditiveOperatorWithExpression(
                                operator = AdditiveOperator.Plus,
                                expression = Expression.SimpleExpression.Literal(
                                    value = 2.0
                                )
                            )
                        ),
                    )
                )
            ),
            functions = emptyMap()
        )

        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is InvalidRuntimeTypeException)
        assertEquals("int", (errors[0] as InvalidRuntimeTypeException).expected)
        assertEquals("double", (errors[0] as InvalidRuntimeTypeException).actual)
    }

    @Test
    fun noReturnFromFunctionException() {
        val program = Program(
            statements = listOf(
                Statement.ExpressionStatement(
                    expression = Expression.SimpleExpression.FunctionCall(
                        identifier = "foo",
                        args = listOf()
                    )
                )
            ),
            functions = mapOf(
                "foo" to UserDefinedFunction(
                    name = "foo",
                    parameters = emptyList(),
                    returnType = TypeConst.INT,
                    body = listOf()
                )
            ),
        )

        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is NoReturnFromFunctionException)
        assertEquals("foo", (errors[0] as NoReturnFromFunctionException).functionName)
    }

    @Test
    fun variableNotFoundException() {
        val program = Program(
            statements = listOf(
                Statement.ExpressionStatement(
                    expression = Expression.SimpleExpression.Identifier(
                        identifier = "foo",
                    )
                )
            ),
            functions = emptyMap()
        )

        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is VariableNotFoundException)
        assertEquals("foo", (errors[0] as VariableNotFoundException).variableName)
    }

    @Test
    fun zeroDivisionExceptionTest() {
        val program = Program(
            statements = listOf(
                Statement.ExpressionStatement(
                    expression = Expression.MultiplicativeExpression(
                        left = Expression.SimpleExpression.Literal(
                            value = 1
                        ),
                        right = listOf(
                            MultiplicativeOperatorWithExpression(
                                operator = MultiplicativeOperator.Div,
                                expression = Expression.SimpleExpression.Literal(
                                    value = 0
                                )
                            )
                        ),
                    )
                )
            ),
            functions = emptyMap()
        )

        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is ZeroDivisionException)
    }

    @Test
    fun immutableVariableAssignmentExpression() {
        val program = Program(
            statements = listOf(
                Statement.VariableDeclaration(
                    isImmutable = true,
                    name = "a",
                    type = TypeConst.INT_NULLABLE,
                    initialValue = Expression.SimpleExpression.Literal(
                        value = 1
                    )
                ),
                Statement.ExpressionStatement(
                    expression = Expression.SimpleExpression.Identifier(
                        identifier = "a",
                    ),
                    assignedExpression = Expression.SimpleExpression.Literal(
                        value = 1
                    )
                )
            ),
            functions = emptyMap()
        )

        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is ImmutableVariableException)
        assertEquals("a", (errors[0] as ImmutableVariableException).variableName)
    }
}
