package org.example.parser.exception

import org.example.inputsource.CodePosition

class InvalidFunctionDeclarationException(override val codePosition: CodePosition) : ParserException(codePosition)