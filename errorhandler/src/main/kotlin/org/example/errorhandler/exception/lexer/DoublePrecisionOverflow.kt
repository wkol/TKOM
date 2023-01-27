package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

class DoublePrecisionOverflow(
    override val codePosition: CodePosition,
    val maxDoublePrecision: Short
) : LexerException(codePosition = codePosition)
