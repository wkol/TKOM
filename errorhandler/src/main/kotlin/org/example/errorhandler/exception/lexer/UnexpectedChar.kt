package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

class UnexpectedChar(
    override val codePosition: CodePosition,
    val char: Char
) : LexerException(codePosition)
