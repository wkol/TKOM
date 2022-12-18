package org.example.lexer.exceptions

import org.example.inputsource.CodePosition

class IdentifierLengthOverflow(override val position: CodePosition, maxLength: Int) : LexerException(
    message = "Identifier length overflow at position Line: ${position.line} Column: ${position.column} Maximum length is $maxLength",
    position = position
)