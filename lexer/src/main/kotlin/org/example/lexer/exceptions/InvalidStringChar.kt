package org.example.lexer.exceptions

import org.example.inputsource.CodePosition

class InvalidStringChar(override val position: CodePosition, char: Char) : LexerException(
    message = "Invalid string char at position Line: ${position.line} Column: ${position.column} Char: $char",
    position = position
)