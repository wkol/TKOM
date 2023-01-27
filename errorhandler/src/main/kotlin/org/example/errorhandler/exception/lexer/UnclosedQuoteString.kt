package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

class UnclosedQuoteString(override val codePosition: CodePosition) : LexerException(codePosition)
