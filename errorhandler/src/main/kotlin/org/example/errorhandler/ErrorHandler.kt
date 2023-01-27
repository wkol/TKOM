package org.example.errorhandler

import org.example.errorhandler.exception.interpreter.InterpreterException
import org.example.errorhandler.exception.lexer.LexerException
import org.example.errorhandler.exception.parser.ParserException

interface ErrorHandler {
    var errorCount: Int
    val errorHandlerConfig: ErrorHandlerConfig

    fun handleParserError(exception: ParserException)
    fun handleLexerError(exception: LexerException)
    fun handleInterpreterError(exception: InterpreterException)
}
