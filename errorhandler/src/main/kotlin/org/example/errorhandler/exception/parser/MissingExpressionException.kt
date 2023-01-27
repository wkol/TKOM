package org.example.errorhandler.exception.parser

import org.example.inputsource.CodePosition

class MissingExpressionException(override val codePosition: CodePosition) : ParserException(codePosition)
