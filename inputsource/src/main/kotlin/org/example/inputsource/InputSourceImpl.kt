package org.example.inputsource

import okio.BufferedSource
import okio.EOFException
import okio.buffer
import okio.source
import kotlin.jvm.Throws

class InputSourceImpl private constructor(private val source: BufferedSource) : InputSource {
    // TODO add support for multiplatform newlines (CR, LF, CRLF, 155, 25)

    private var column = 1L
    private var line = 1L
    private var offset = 0L

    private var currentChar: Char = 0.toChar()

    @Suppress
    override fun peekNextChar(): Char {
        return try {
            val char = source.peek().readUtf8CodePoint().toChar()
            char
        } catch (e: EOFException) {
            EOF
        }
    }

    @Throws(EOFException::class)
    override fun getNextChar(): Char {
        currentChar = source.readUtf8CodePoint().toChar()
        if (currentChar == '\n') {
            line++
            column = 1
        } else {
            column++
        }
        offset++
        return currentChar
    }

    override fun peekNextChars(count: Int) = try {
            source.peek().readUtf8(count.toLong())
        } catch (e: EOFException) {
            ""
        }

    override fun getPosition() = CodePosition(
        column = column,
        line = line,
        offset = offset,
    )

    override fun getCurrentCharacter() = currentChar

    companion object {
        fun fromSource(source: BufferedSource): InputSource {
            return InputSourceImpl(source)
        }

        fun fromString(source: String): InputSource {
            source.byteInputStream().source().use {
                return fromSource(it.buffer())
            }
        }
    }
}
