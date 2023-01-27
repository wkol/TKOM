package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

class InvalidIdentifier(
    override val codePosition: CodePosition,
    val identifier: String
) : LexerException(codePosition)

