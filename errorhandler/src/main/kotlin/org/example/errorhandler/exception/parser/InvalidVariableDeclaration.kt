package org.example.errorhandler.exception.parser

import org.example.inputsource.CodePosition

class InvalidVariableDeclaration(override val codePosition: CodePosition) : ParserException(codePosition)
