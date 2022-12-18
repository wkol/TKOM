package org.example.lexer.exceptions

import org.example.inputsource.CodePosition

class CommentLengthOverflow(override val position: CodePosition, maxCommentLength: Int) : LexerException(
    message = "Comment length overflow at position Line: ${position.line} Column: ${position.column} Max comment length is $maxCommentLength",
    position = position
)