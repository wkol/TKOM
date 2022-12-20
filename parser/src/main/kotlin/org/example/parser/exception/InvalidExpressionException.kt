package org.example.parser.exception

import org.example.inputsource.CodePosition

class InvalidExpressionException(override val codePosition: CodePosition) : ParserException(codePosition)
