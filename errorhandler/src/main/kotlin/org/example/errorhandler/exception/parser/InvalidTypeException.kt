package org.example.errorhandler.exception.parser

import org.example.inputsource.CodePosition

class InvalidTypeException(override val codePosition: CodePosition) : ParserException(codePosition)
