package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

class NumberOverflow(
    override val codePosition: CodePosition,
    val maxInt: Long
) : LexerException(codePosition)
