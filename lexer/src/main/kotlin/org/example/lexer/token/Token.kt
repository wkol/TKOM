@file:Suppress("unused")
package org.example.lexer.token

import org.example.inputsource.CodePosition

sealed class Token(
    val type: TokenType,
    open val value: Any?,
    open val position: CodePosition
) {
    data class EOF(
        override val position: CodePosition
    ) : Token(TokenType.EOF, "EOF", position)


    data class Literal(
        override val value: Any?,
        override val position: CodePosition,
        val literalType: Type
    ) : Token(TokenType.LITERAL, value, position) {
        enum class Type {
            INTEGER,
            DOUBLE,
            STRING,
            BOOLEAN,
            NULL,
        }
    }

    data class Identifier(
        override val value: String,
        override val position: CodePosition
    ) : Token(TokenType.IDENTIFIER, value, position)

    data class AdditiveOperator(
        override val position: CodePosition
    ) : Token(TokenType.ADDITIVE_OPERATOR, "+", position)

    data class MultiplicativeOperator(
        override val value: Type,
        override val position: CodePosition
    ) : Token(TokenType.MULTIPLICATIVE_OPERATOR, value, position) {
        enum class Type {
            MULTIPLY,
            DIVIDE,
            MODULO
        }
    }

    data class ComparisonOperator(
        override val value: Type,
        override val position: CodePosition
    ) : Token(TokenType.COMPARISON_OPERATOR, value, position) {
        enum class Type {
            EQUAL,
            NOT_EQUAL,
            LESS,
            LESS_OR_EQUAL,
            GREATER,
            GREATER_OR_EQUAL
        }
    }

    data class NullSafetyOperator(
        override val position: CodePosition
    ) : Token(TokenType.NULL_SAFETY_OPERATOR, "?:", position)

    data class UnaryOperator(
        override val value: Type,
        override val position: CodePosition
    ) : Token(TokenType.UNARY_OPERATOR, value, position) {
        enum class Type {
            MINUS,
            NOT
        }
    }

    data class Special(
        override val value: Type,
        override val position: CodePosition,
    ) : Token(TokenType.SPECIAL, value, position) {
        enum class Type {
            LPAREN,
            RPAREN,
            LBRACE,
            RBRACE,
            COMMA,
            ASSIGN,
            QUESTION_MARK,
        }
    }

    data class Keyword(
        override val value: Type,
        override val position: CodePosition
    ) : Token(TokenType.KEYWORD, value, position) {
        enum class Type {
            RETURN,
            FUN,
            VAR,
            CONST,
            IF,
            ELSE,
            WHILE,
            BOOL,
            INT,
            DOUBLE,
            STRING,
            AS,
        }
    }

    data class Conjunction(
        override val position: CodePosition
    ) : Token(TokenType.CONJUNCTION, "&&", position)

    data class Disjunction(
        override val position: CodePosition
    ) : Token(TokenType.DISJUNCTION, "||", position)

    data class FunReturnTypeArrow(
        override val position: CodePosition
    ) : Token(TokenType.FUN_RETURN_ARROW, "->", position)

    data class Comment(
        override val value: String,
        override val position: CodePosition
    ) : Token(TokenType.COMMENT, value, position)
}


