package org.example.interpreter

import org.example.errorhandler.ErrorHandler
import org.example.errorhandler.ErrorHandlerConfig
import org.example.errorhandler.exception.interpreter.InterpreterException
import org.example.errorhandler.exception.interpreter.InvalidRuntimeTypeException
import org.example.errorhandler.exception.interpreter.StandardLibraryFunctionOverriddenException
import org.example.errorhandler.exception.lexer.LexerException
import org.example.errorhandler.exception.parser.ParserException
import org.example.inputsource.InputSource
import org.example.inputsource.InputSourceImpl
import org.example.lexer.Lexer
import org.example.lexer.LexerImpl
import org.example.parser.Parser
import org.example.parser.ParserImpl
import org.example.parser.data.BuiltInFunction
import org.example.parser.data.Parameter
import org.example.parser.data.RuntimeValue
import org.example.parser.data.Type
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Stack
import kotlin.test.assertEquals

class IntegrationTest {

    private lateinit var inputSource: InputSource
    private lateinit var lexer: Lexer
    private lateinit var errorHandler: ErrorHandler
    private lateinit var parser: Parser
    private lateinit var interpreter: Interpreter
    private var errors = mutableListOf<InterpreterException>()
    val outputs = mutableListOf<String>()


    private fun setUpLexerAndParser(input: String) {
        outputs.clear()
        errors.clear()
        errorHandler = object : ErrorHandler {
            override var errorCount: Int
                get() = errors.size
                set(_) {}

            override val errorHandlerConfig: ErrorHandlerConfig
                get() = ErrorHandlerConfig()

            override fun handleInterpreterError(exception: InterpreterException) {
                errors.add(exception)
                throw RuntimeException()
            }

            override fun handleParserError(exception: ParserException) = Unit

            override fun handleLexerError(exception: LexerException) = Unit
        }
        inputSource = InputSourceImpl.fromString(input)
        lexer = LexerImpl(inputSource, errorHandler)
        parser = ParserImpl(lexer, errorHandler)
    }


    @Test
    fun functionDeclarationOverridesBuiltin() {
        val input = """
            fun print(string text) {
                return
            }
        """.trimIndent()
        setUpLexerAndParser(input)
        val program = parser.parse()

        interpreter = InterpreterImpl(errorHandler, StandardLibraryImpl())
        assertThrows<Exception> { interpreter.interpret(program) }

        assertEquals(1, errors.size)
        assert(errors[0] is StandardLibraryFunctionOverriddenException)
    }

    @Test
    fun helloWorldExample() {
        val input = """
            fun helloWorld(string? name) {
                print("Hello " + (name ?: "") + "!")
            }
            const string? nullValue = null
            const double? a = 2.0
            helloWorld(a as string?)
            helloWorld(nullValue)
        """.trimIndent()

        setUpLexerAndParser(input)
        val program = parser.parse()
        interpreter = InterpreterImpl(errorHandler, getStandardLibrary(emptyList()))
        interpreter.interpret(program)

        assertEquals(2, outputs.size)
        assertEquals("Hello 2.0!", outputs[0])
        assertEquals("Hello !", outputs[1])
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
                var int i = 3
                const double c = 2.0
                while(i >= 0) {
                    print("Type a number")
                    const string input = readInput() ?: "0"
                    if (isNumber(input)) {
                        const double number = input as double
                        print("The average of 2 and " + number as string + " is " + average(c, number) as string)
                    } else {
                        print("The input is not a number")
                    }
                    i = i - 1 // incrementing i
                }
                printText(i as string)
            }
            
            main()
        """
        setUpLexerAndParser(input)
        val program = parser.parse()

        interpreter = InterpreterImpl(
            errorHandler, getStandardLibrary(
                listOf(
                    "12",
                    "1",
                    "1",
                    "-100",
                )
            )
        )
        interpreter.interpret(program)

        assertEquals(
            expected =
            "Type a number\n" +
                    "The average of 2 and 12.0 is 7.0\n" +
                    "Type a number\n" +
                    "The average of 2 and 1.0 is 1.5\n" +
                    "Type a number\n" +
                    "The average of 2 and 1.0 is 1.5\n" +
                    "Type a number\n" +
                    "The average of 2 and -100.0 is -49.0\n" +
                    "Exiting program with i = -1",
            actual = outputs.joinToString("\n")
        )
    }

    @Test
    fun overrideScopeVariableTest() {
        val input = """
           var int x = 1
           if (x == 1) {
               var bool x = true
               print(x as string)
           }
           """
        setUpLexerAndParser(input)
        val program = parser.parse()

        interpreter = InterpreterImpl(errorHandler, getStandardLibrary(emptyList()))
        interpreter.interpret(program)

        assertEquals(
            expected =
            "true",
            actual = outputs.joinToString("\n")
        )
    }

    @Test
    fun invalidTypeExceptionTest() {
        val input = """
            if (1 == 1.0) {
                print("true")
            }
        """.trimIndent()

        setUpLexerAndParser(input)
        val program = parser.parse()
        interpreter = InterpreterImpl(errorHandler, getStandardLibrary(emptyList()))
        assertThrows<Exception> { interpreter.interpret(program) }
        assertEquals(1, errors.size)
        assert(errors[0] is InvalidRuntimeTypeException)
    }

    private fun getStandardLibrary(inputs: List<String>) =
        object : StandardLibrary {
            val standardIns = Stack<String>()

            init {
                inputs.reversed().forEach { standardIns.push(it) }
            }

            override val isNumber = BuiltInFunction(
                invokeFunction = { args ->
                    RuntimeValue(
                        value = args[0].getCastedValue<String>().toIntOrNull() != null,
                        type = Type(
                            type = Type.TypePrimitive.BOOL,
                            isNullable = false
                        )
                    )
                },
                name = "isNumber",
                parameters = listOf(
                    Parameter(
                        name = "value",
                        type = Type(
                            type = Type.TypePrimitive.STRING,
                            isNullable = false
                        )
                    )
                ),
                returnType = Type(
                    type = Type.TypePrimitive.BOOL,
                    isNullable = false
                )
            )

            override val print = BuiltInFunction(
                invokeFunction = { args ->
                    println(args[0].getCastedValue<String>())
                    outputs.add(args[0].getCastedValue())
                    RuntimeValue(
                        value = null,
                        type = Type(
                            type = Type.TypePrimitive.VOID,
                            isNullable = false
                        )
                    )
                },
                name = "print",
                parameters = listOf(
                    Parameter(
                        name = "value",
                        type = Type(
                            type = Type.TypePrimitive.STRING,
                            isNullable = false
                        )
                    )
                ),
                returnType = Type(
                    type = Type.TypePrimitive.VOID,
                    isNullable = false
                )
            )

            override val readInput = BuiltInFunction(
                invokeFunction = { _ ->
                    RuntimeValue(
                        value = standardIns.pop(),
                        type = Type(
                            type = Type.TypePrimitive.STRING,
                            isNullable = true
                        )
                    )
                },
                name = "readInput",
                parameters = emptyList(),
                returnType = Type(
                    type = Type.TypePrimitive.STRING,
                    isNullable = true
                )
            )

            override val type = BuiltInFunction(
                invokeFunction = { args ->
                    RuntimeValue(
                        value = args[0].type.type.name,
                        type = Type(
                            type = Type.TypePrimitive.STRING,
                            isNullable = false
                        )
                    )
                },
                name = "type",
                parameters = listOf(
                    Parameter(
                        name = "value",
                        type = Type(
                            type = Type.TypePrimitive.STRING,
                            isNullable = false
                        )
                    )
                ),
                returnType = Type(
                    type = Type.TypePrimitive.STRING,
                    isNullable = false
                )
            )

            override fun tryGetFunction(name: String): BuiltInFunction? = when (name) {
                print.name -> print
                readInput.name -> readInput
                isNumber.name -> isNumber
                type.name -> type
                else -> null
            }
        }
}
