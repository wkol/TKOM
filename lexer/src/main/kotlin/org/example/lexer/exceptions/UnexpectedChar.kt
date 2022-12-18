package org.example.lexer.exceptions

import org.example.inputsource.CodePosition

class UnexpectedChar(char: Char, position: CodePosition) : LexerException(
    message = "Unexpected character: $char at position Line: ${position.line} Column: ${position.column}",
    position = position
)