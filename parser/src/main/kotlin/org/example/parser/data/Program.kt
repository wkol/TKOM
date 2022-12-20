package org.example.parser.data

data class Program(
    val statements: List<Statement>,
    val functions: Map<String, Function>
)