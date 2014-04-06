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

import java.util.NoSuchElementException
import java.math.BigDecimal

public data open class JsValue : Iterable<JsValue> {
    open fun get(name: String): JsValue = JsUndefined()
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
    open fun asLong(): Long? = null
    open fun asInt(): Int? = null
    open fun asDouble(): Double? = null
    open fun asBigDecimal(): BigDecimal? = null
}

public data class JsObject(vararg fields: Pair<String, JsValue>) : JsValue() {
    val fields: Map<String, JsValue>;
    {
        this.fields = linkedMapOf(*fields)
    }

    override fun get(name: String) = fields.getOrElse(name) { super.get(name) }
    override fun iterator() = fields.values().iterator()

    override fun toString(): String {
        return "JsObject($fields)"
    }
}

public data class JsString(val value: String?) : JsValue() {
    override fun asString(): String? = value
}

public data class JsNumber(val value: BigDecimal?) : JsValue() {
    override fun asLong() = if (value == null) null else value.longValue()
    override fun asInt() = if (value == null) null else value.intValue()
    override fun asDouble() = if (value == null) null else value.doubleValue()
    override fun asBigDecimal() = value
}

public data class JsBoolean(val value: Boolean?) : JsValue() {
    override fun asBoolean() = value
}

public data class JsArray(val values: List<JsValue>) : JsValue() {
    override fun iterator() = values.iterator()
}

public data class JsUndefined() : JsValue()
public data class JsNull() : JsValue()