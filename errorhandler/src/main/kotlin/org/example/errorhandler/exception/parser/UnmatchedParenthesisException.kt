package org.example.errorhandler.exception.parser

import org.example.inputsource.CodePosition

class UnmatchedParenthesisException(override val codePosition: CodePosition) : ParserException(codePosition)
