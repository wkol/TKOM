package org.example.interpreter

import org.example.errorhandler.ErrorHandlerConfig
import org.example.inputsource.InputSourceImpl
import org.example.lexer.LexerImpl
import org.example.parser.ParserImpl

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: interpreter <file> or interpreter --std-in \"<code>\"")
        return
    }
    val inputSource = if (args.contains("--std-in")) {
        InputSourceImpl.fromString(args[args.indexOf("--std-in") + 1])
    } else {
        InputSourceImpl.fromFile(args[0])
    }
    val errorHandler = ErrorHandlerImpl(ErrorHandlerConfig())
    val lexer = LexerImpl(inputSource, errorHandler)
    val parser = ParserImpl(lexer, errorHandler)
    val interpreter = InterpreterImpl(
        errorHandler, StandardLibraryImpl()
    )

    val program = parser.parse()
    if (errorHandler.errorCount == 0) {
        interpreter.interpret(program)
    }
}
