package org.example.parser.exception

import org.example.inputsource.CodePosition

class InvalidVariableDeclaration(override val codePosition: CodePosition) : ParserException(codePosition)