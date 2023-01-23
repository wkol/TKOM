package org.example.parser.exception

import org.example.inputsource.CodePosition

class InvalidFunctionParamDeclarationException(override val codePosition: CodePosition) : ParserException(codePosition)