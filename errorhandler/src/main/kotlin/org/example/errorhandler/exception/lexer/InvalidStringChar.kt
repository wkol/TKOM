package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

class InvalidStringChar(
    override val codePosition: CodePosition,
    val char: Char
) : LexerException(codePosition)
