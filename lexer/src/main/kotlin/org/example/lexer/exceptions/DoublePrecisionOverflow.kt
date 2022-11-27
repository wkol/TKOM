package org.example.lexer.exceptions

import org.example.inputsource.CodePosition

class DoublePrecisionOverflow(override val position: CodePosition, maxDoublePrecision: Short) : LexerException(
    message = "Double precision overflow at position Line: ${position.line} Column: ${position.column} Max double precision is $maxDoublePrecision",
    position = position
)
