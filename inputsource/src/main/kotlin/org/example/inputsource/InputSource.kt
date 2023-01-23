package org.example.inputsource

const val EOF = '\u0004'

interface InputSource {
    // TODO convert it to getting only current character as property
    var currentChar: Char

    fun consumeCharacter(): Char

    fun getPosition(): CodePosition
}
