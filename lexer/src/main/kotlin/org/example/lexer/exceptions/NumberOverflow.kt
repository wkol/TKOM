package org.example.lexer.exceptions

import org.example.inputsource.CodePosition

class NumberOverflow(override val position: CodePosition, maxInt: Long) : LexerException(
    message = "Number overflow at position Line: ${position.line} Column: ${position.column} Max value is $maxInt",
    position = position
)