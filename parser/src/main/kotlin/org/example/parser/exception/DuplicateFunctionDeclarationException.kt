package org.example.parser.exception

import org.example.inputsource.CodePosition

class DuplicateFunctionDeclarationException(override val codePosition: CodePosition, val name: String) : ParserException(codePosition)