package org.example.interpreter.data

import org.example.parser.data.ProgramFunction
import org.example.parser.data.RuntimeValue

open class Scope(
    private val parentScope: Scope?
) {
    private val variables = mutableMapOf<String, Variable>()

    fun tryFindVariable(name: String): Variable? {
        return variables[name] ?: parentScope?.tryFindVariable(name)
    }

    fun trySetVariable(name: String, newValue: RuntimeValue?): Boolean =
        if (variables.containsKey(name)) {
            variables[name]?.trySetVariable(newValue)
            true
        } else {
            parentScope?.trySetVariable(name, newValue) == true
        }

    fun addVariable(variable: Variable): Boolean =
        if (variables.containsKey(variable.name)) {
            false
        } else {
            variables[variable.name] = variable
            true
        }
}

object GlobalScope : Scope(null) {
    private val userDefinedFunctions = mutableMapOf<String, ProgramFunction>()

    fun addFunction(function: ProgramFunction) {
        userDefinedFunctions[function.name] = function
    }

    fun tryFindFunction(name: String): ProgramFunction? {
        return userDefinedFunctions[name]
    }

    fun getGlobalScope(): GlobalScope {
        return this
    }
}

