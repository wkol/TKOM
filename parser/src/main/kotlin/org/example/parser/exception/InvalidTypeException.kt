package org.example.parser.exception

import org.example.inputsource.CodePosition

class InvalidTypeException(override val codePosition: CodePosition) : ParserException(codePosition)