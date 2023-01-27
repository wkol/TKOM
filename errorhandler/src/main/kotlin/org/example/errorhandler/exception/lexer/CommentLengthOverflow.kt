package org.example.errorhandler.exception.lexer

import org.example.inputsource.CodePosition

class CommentLengthOverflow(override val codePosition: CodePosition, val maxCommentLength: Int) :
    LexerException(codePosition)
