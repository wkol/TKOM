package org.example.lexer

data class LexerConfig(
    val maxIdentifierLength: Int = DEFAULT_MAX_IDENTIFIER_LENGTH,
    val maxIntegerValue: Long = DEFAULT_MAX_INTEGER_VALUE,
    val maxDoublePrecision: Short = DEFAULT_MAX_DOUBLE_PRECISION,
    val maxStringLength: Int = DEFAULT_MAX_STRING_LENGTH,
    val maxCommentLength: Int = DEFAULT_MAX_COMMENT_LENGTH
) {
    companion object {
        private const val DEFAULT_MAX_IDENTIFIER_LENGTH = 255
        private const val DEFAULT_MAX_INTEGER_VALUE = Int.MAX_VALUE.toLong()
        private const val DEFAULT_MAX_DOUBLE_PRECISION = 6.toShort()
        private const val DEFAULT_MAX_STRING_LENGTH = 1000
        private const val DEFAULT_MAX_COMMENT_LENGTH = 1000
    }
}
