package org.example.errorhandler.exception.parser

import org.example.inputsource.CodePosition

abstract class ParserException(open val codePosition: CodePosition) : Exception()
