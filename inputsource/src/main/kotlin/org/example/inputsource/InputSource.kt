package org.example.inputsource

const val EOF = '\u0004'

interface InputSource {
    var currentChar: Char

    fun consumeCharacter(): Char

    fun getPosition(): CodePosition
}
