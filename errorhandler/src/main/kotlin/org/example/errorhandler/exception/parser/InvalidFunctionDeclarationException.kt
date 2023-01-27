package org.example.errorhandler.exception.parser

import org.example.inputsource.CodePosition

class InvalidFunctionDeclarationException(
    override val codePosition: CodePosition
) : ParserException(codePosition)
