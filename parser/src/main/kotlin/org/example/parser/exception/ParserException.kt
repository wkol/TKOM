package org.example.parser.exception

import org.example.inputsource.CodePosition

abstract class ParserException(open val codePosition: CodePosition) : Exception()