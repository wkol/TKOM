package org.example.parser.exception

import org.example.inputsource.CodePosition

class InvalidConditionException(override val codePosition: CodePosition) : ParserException(codePosition)