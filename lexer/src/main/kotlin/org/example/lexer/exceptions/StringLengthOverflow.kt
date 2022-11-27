package org.example.lexer.exceptions

import org.example.inputsource.CodePosition

class StringLengthOverflow(override val position: CodePosition, maxLength: Int) : LexerException(
    message = "String length overflow at position Line: ${position.line} Column: ${position.column}. Max length is $maxLength",
    position = position
)