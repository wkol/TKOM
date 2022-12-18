package org.example.lexer.exceptions

import org.example.inputsource.CodePosition

class UnclosedQuoteString(override val position: CodePosition) : LexerException(
    message = "Unclosed quote string at position Line: ${position.line} Column: ${position.column}",
    position = position
)