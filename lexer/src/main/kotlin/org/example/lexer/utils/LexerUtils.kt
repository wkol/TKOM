package org.example.lexer.utils

fun Char.isEscapedChar(): Boolean {
    return this in listOf('n', 't', 'r', 'b', '\\', '"')
}

fun Char.isIdentifierChar(): Boolean {
    return this.isLetterOrDigit() || this == '_'
}

fun Char.isSpecialChar(): Boolean {
    return this in listOf('(', ')', '{', '}', ',', '?')
}

fun Char.isLogicalOperatorChar(): Boolean {
    return this in listOf('&', '|')
}
