package org.example.interpreter.data

import org.example.interpreter.StandardLibrary
import org.example.parser.data.Parameter
import org.example.parser.data.ProgramFunction
import org.example.parser.data.RuntimeValue
import java.util.Stack

class Environment(
    private val globalScope: GlobalScope,
    private val standardLibrary: StandardLibrary
) {
    private val scopes = Stack<Scope>()

    init {
        scopes.push(globalScope)
    }

    var recursionLevel: Int = 0
        private set

    fun setVariable(name: String, value: RuntimeValue?) {
        scopes.peek().trySetVariable(name, value)
    }

    fun getVariable(name: String?): Variable? {
        if (name == null) {
            return null
        }
        return scopes.peek().tryFindVariable(name)
    }

    fun addVariable(variable: Variable) {
        scopes.peek().addVariable(variable)
    }

    fun getFunction(name: String): ProgramFunction? {
        return standardLibrary.tryGetFunction(name) ?: globalScope.tryFindFunction(name)
    }

    fun newLocalScope() {
        scopes.push(Scope(scopes.peek()))
    }

    fun newFunctionScope(
        args: Map<Parameter, RuntimeValue?>
    ) {
        scopes.push(
            FunctionCallContext(
                args,
                globalScope
            )
        )
        recursionLevel++
    }

    fun exitFunctionScope() {
        scopes.pop()
        recursionLevel--
    }

    fun exitLocalScope() {
        scopes.pop()
    }

    fun addFunction(function: ProgramFunction): Boolean {
        if (standardLibrary.tryGetFunction(function.name) != null) {
            return false
        }
        globalScope.addFunction(function)
        return true
    }

}
