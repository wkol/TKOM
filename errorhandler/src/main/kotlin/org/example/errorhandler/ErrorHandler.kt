package org.example.errorhandler

import java.lang.Exception

interface ErrorHandler {
    fun handleParserError(exception: Exception)
    fun handleLexerError(exception: Exception)
}