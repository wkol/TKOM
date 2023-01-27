package org.example.errorhandler.exception.parser

import org.example.inputsource.CodePosition

class InvalidExpressionException(override val codePosition: CodePosition) : ParserException(codePosition)
