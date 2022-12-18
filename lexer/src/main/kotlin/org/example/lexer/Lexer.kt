package org.example.lexer

import org.example.lexer.token.Token

abstract class Lexer {
    abstract var lastToken: Token?
    abstract fun getNextToken()
}