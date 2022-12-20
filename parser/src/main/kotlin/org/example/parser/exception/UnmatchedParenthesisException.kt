package org.example.parser.exception

import org.example.inputsource.CodePosition

class UnmatchedParenthesisException(override val codePosition: CodePosition) : ParserException(codePosition)