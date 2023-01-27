package org.example.interpreter.data

import org.example.parser.data.Parameter
import org.example.parser.data.RuntimeValue

class FunctionCallContext(
    args: Map<Parameter, RuntimeValue?>,
    globalScope: Scope
) : Scope(globalScope) {

    init {
        args.forEach { (parameter, expression) ->
            addVariable(
                Variable(
                    type = parameter.type,
                    isImmutable = false,
                    name = parameter.name,
                    initialValue = expression
                )
            )
        }
    }

}
