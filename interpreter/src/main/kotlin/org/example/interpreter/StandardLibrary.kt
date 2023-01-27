package org.example.interpreter

import org.example.parser.data.BuiltInFunction
import org.example.parser.data.Parameter
import org.example.parser.data.RuntimeValue
import org.example.parser.data.Type

interface StandardLibrary {

    val print: BuiltInFunction
    val readInput: BuiltInFunction
    val isNumber: BuiltInFunction
    val type: BuiltInFunction

    fun tryGetFunction(name: String): BuiltInFunction?
}

class StandardLibraryImpl : StandardLibrary {

    override val isNumber = BuiltInFunction(
        invokeFunction = { args ->
            RuntimeValue(
                value = args[0].getCastedValue<String>().toIntOrNull() != null,
                type = Type(
                    type = Type.TypePrimitive.BOOL,
                    isNullable = false
                )
            )
        },
        name = "isNumber",
        parameters = listOf(
            Parameter(
                name = "value",
                type = Type(
                    type = Type.TypePrimitive.STRING,
                    isNullable = false
                )
            )
        ),
        returnType = Type(
            type = Type.TypePrimitive.BOOL,
            isNullable = false
        )
    )

    override val print = BuiltInFunction(
        invokeFunction = { args ->
            println(args[0].getCastedValue<String>())
            RuntimeValue(
                value = null,
                type = Type(
                    type = Type.TypePrimitive.VOID,
                    isNullable = false
                )
            )
        },
        name = "print",
        parameters = listOf(
            Parameter(
                name = "value",
                type = Type(
                    type = Type.TypePrimitive.STRING,
                    isNullable = true
                )
            )
        ),
        returnType = Type(
            type = Type.TypePrimitive.VOID,
            isNullable = false
        )
    )

    override val readInput = BuiltInFunction(
        invokeFunction = { _ ->
            RuntimeValue(
                value = readlnOrNull(),
                type = Type(
                    type = Type.TypePrimitive.STRING,
                    isNullable = true
                )
            )
        },
        name = "readInput",
        parameters = emptyList(),
        returnType = Type(
            type = Type.TypePrimitive.STRING,
            isNullable = true
        )
    )

    override val type = BuiltInFunction(
        invokeFunction = { args ->
            RuntimeValue(
                value = args[0].type.type.name,
                type = Type(
                    type = Type.TypePrimitive.STRING,
                    isNullable = false
                )
            )
        },
        name = "type",
        parameters = listOf(
            TypeAnyParam("value")
        ),
        returnType = Type(
            type = Type.TypePrimitive.STRING,
            isNullable = false
        )
    )

    override fun tryGetFunction(name: String): BuiltInFunction? = when (name) {
        print.name -> print
        readInput.name -> readInput
        isNumber.name -> isNumber
        type.name -> type
        else -> null
    }
}

class TypeAnyParam(name: String) : Parameter(name, Type(Type.TypePrimitive.VOID, false))

