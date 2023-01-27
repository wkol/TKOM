package org.example.lexer

import org.example.errorhandler.ErrorHandler
import org.example.errorhandler.ErrorHandlerConfig
import org.example.errorhandler.exception.interpreter.InterpreterException
import org.example.errorhandler.exception.lexer.LexerException
import org.example.errorhandler.exception.parser.ParserException
import org.example.inputsource.InputSourceImpl
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ComplexLexerTests {

    val errorHandler = object : ErrorHandler {
        override var errorCount: Int = 0

        override val errorHandlerConfig: ErrorHandlerConfig
            get() = ErrorHandlerConfig()

        override fun handleInterpreterError(exception: InterpreterException) {
        }

        override fun handleParserError(exception: ParserException) {
        }

        override fun handleLexerError(exception: LexerException) {
        }
    }

    @Test
    fun `Variable declaration`() {
        println("Testing variable declaration:\n${Expected.VARIABLE_DECLARATION.input}")
        val inputStream = InputSourceImpl.fromString(Expected.VARIABLE_DECLARATION.input)
        val lexer = LexerImpl(inputStream, errorHandler, LexerConfig())
        val tokens = lexer.getAllTokens()
        assertEquals(Expected.VARIABLE_DECLARATION.tokens, tokens)
    }

    @Test
    fun `Const declaration`() {
        println("Testing const declaration:\n${Expected.CONST_DECLARATION.input}")
        val inputStream = InputSourceImpl.fromString(Expected.CONST_DECLARATION.input)
        val lexer = LexerImpl(inputStream, errorHandler, LexerConfig())
        val tokens = lexer.getAllTokens()
        assertEquals(Expected.CONST_DECLARATION.tokens, tokens)
    }

    @Test
    fun `Function declaration`() {
        println("Testing function declaration:\n${Expected.FUNCTION_DECLARATION.input}")
        val inputStream = InputSourceImpl.fromString(Expected.FUNCTION_DECLARATION.input)
        val lexer = LexerImpl(inputStream, errorHandler, LexerConfig())
        val tokens = lexer.getAllTokens()
        assertEquals(Expected.FUNCTION_DECLARATION.tokens, tokens)
    }
}
