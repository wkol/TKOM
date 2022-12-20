package org.example.parser

import org.example.errorhandler.ErrorHandler
import org.example.lexer.Lexer
import org.example.parser.data.Program

abstract class Parser {
    abstract val lexer: Lexer
    abstract val errorHandler: ErrorHandler
    abstract fun parse(): Program
}