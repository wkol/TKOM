package org.example.errorhandler.exception.parser

import org.example.inputsource.CodePosition

class InvalidConditionException(override val codePosition: CodePosition) : ParserException(codePosition)
