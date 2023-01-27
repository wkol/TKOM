package org.example.interpreter.data

import org.example.parser.data.RuntimeValue
import org.example.parser.data.Type

data class Variable(
    val type: Type,
    val isImmutable: Boolean,
    val name: String,
    val initialValue: RuntimeValue? = null,
) {
    var value: RuntimeValue? = null

    init {
        value = initialValue
    }

    fun trySetVariable(newValue: RuntimeValue?) {
        value = newValue
    }
}
