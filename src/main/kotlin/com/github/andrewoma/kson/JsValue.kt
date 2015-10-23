/*
 * Copyright (c) 2014 Andrew O'Malley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.andrewoma.kson

import java.math.BigDecimal
import java.util.NoSuchElementException

interface JsValue : Iterable<JsValue> {
    open operator fun get(name: String): JsValue = JsUndefined
    override fun iterator(): Iterator<JsValue> {
        return object : Iterator<JsValue> {
            var next = true
            override fun next() = if (hasNext()) {
                next = false
                this@JsValue
            } else {
                throw NoSuchElementException()
            }

            override fun hasNext() = next
        }
    }

    open fun asString(): String? = null
    open fun asBoolean(): Boolean? = null
    open fun asInt(): Int? = null
    open fun asLong(): Long? = null
    open fun asFloat(): Float? = null
    open fun asDouble(): Double? = null
    open fun asBigDecimal(): BigDecimal? = null
}

fun JsObject(vararg fields: Pair<String, JsValue>): JsObject = JsObject(linkedMapOf(*fields))

data class JsObject(val fields: Map<String, JsValue>) : JsValue {
    override fun get(name: String) = fields.getOrElse(name) { super.get(name) }
    override fun iterator() = fields.values.iterator()

    override fun toString(): String {
        return "JsObject($fields)"
    }
}

data class JsString(val value: String?) : JsValue {
    override fun asString(): String? = value
}

data class JsNumber(val value: BigDecimal?) : JsValue {
    override fun asInt() = value?.toInt()
    override fun asLong() = value?.toLong()
    override fun asFloat() = value?.toFloat()
    override fun asDouble() = value?.toDouble()
    override fun asBigDecimal() = value
}

data class JsBoolean(val value: Boolean?) : JsValue {
    override fun asBoolean() = value
}

fun JsArray(vararg values: JsValue): JsValue = JsArray(values.toList())

data class JsArray(val values: List<JsValue>) : JsValue {
    override fun iterator() = values.iterator()
}

object JsUndefined : JsValue

object JsNull : JsValue

fun toJsValue(value: Any?): JsValue {
    if (value == null) return JsNull

    return when (value) {
        is JsValue -> value
        is String -> JsString(value)
        is Boolean -> JsBoolean(value)
        is Int -> JsNumber(BigDecimal(value))
        is Long -> JsNumber(BigDecimal(value))
        is Float -> JsNumber(BigDecimal(value.toDouble()))
        is Double -> JsNumber(BigDecimal(value))
        is Number -> JsNumber(BigDecimal(value.toString()))
        else -> throw IllegalArgumentException("Cannot convert ${value.javaClass.name} to a JsValue")
    }
}

// TODO ... add fromJsValue to "unwrap" values?
