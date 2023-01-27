package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

class StringLengthOverflow(
    override val codePosition: CodePosition,
    val maxLength: Int
) : LexerException(codePosition)
