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

import com.fasterxml.jackson.core.*
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.Serializers
import java.util.Stack

class JsValueSerializer : JsonSerializer<JsValue>() {
    override fun serialize(value: JsValue?, json: JsonGenerator?, provider: SerializerProvider?) {
        json!!

        when (value) {
            is JsNumber -> json.writeNumber(value.asBigDecimal())
            is JsString -> json.writeString(value.asString())
            is JsBoolean -> json.writeBoolean(value.asBoolean()!!)
            is JsArray -> {
                json.writeStartArray()
                for (e in value) {
                    serialize(e, json, provider)
                }
                json.writeEndArray()
            }
            is JsObject -> {
                json.writeStartObject()
                for ((name, fieldValue) in value.fields) {
                    json.writeFieldName(name)
                    serialize(fieldValue, json, provider)
                }
                json.writeEndObject()
            }
            is JsNull -> json.writeNull()
            is JsUndefined -> json.writeNull()
        }
    }
}

trait DeserializerContext {
    fun addValue(value: JsValue, jsonParser: JsonParser): DeserializerContext
}

class ArrayContext(val content: MutableList<JsValue> = arrayListOf()) : DeserializerContext {
    override fun addValue(value: JsValue, jsonParser: JsonParser): DeserializerContext {
        content.add(value)
        return this
    }
}

class ObjectKeyContext(val content: MutableMap<String, JsValue>, val fieldName: String) : DeserializerContext {
    override fun addValue(value: JsValue, jsonParser: JsonParser): DeserializerContext {
        content.put(fieldName, value)
        return ObjectContext(content)
    }
}

// Context for reading one item of an Object (we already read fieldName)
class ObjectContext(val content: MutableMap<String, JsValue> = linkedMapOf()) : DeserializerContext {
    fun setField(fieldName: String) = ObjectKeyContext(content, fieldName)
    override fun addValue(value: JsValue, jsonParser: JsonParser): DeserializerContext {
        throw JsonParseException("Expected object key before value", jsonParser.location)
    }
}

val JsonParser.location: JsonLocation?
    get() = this.getCurrentLocation()

class JsValueDeserializer(val klass: Class<*>) : JsonDeserializer<Any>() {
    override fun isCachable() = true

    override fun deserialize(jp: JsonParser?, ctxt: DeserializationContext?): Any? {
        val value = doDeserialize(jp!!, ctxt!!, Stack<DeserializerContext>())

        if (!klass.isAssignableFrom(value.javaClass)) {
            throw ctxt.mappingException(klass)!!
        }

        return value
    }

    tailRecursive fun doDeserialize(jp: JsonParser, context: DeserializationContext, stack: Stack<DeserializerContext>): JsValue {
        // Note: it appears that most of the error conditions within are unreachable as the JsonParser has already
        // validated the sequence of tokens before calling us
        if (jp.getCurrentToken() == null) {
            jp.nextToken()
        }

        val value: JsValue? = when (jp.getCurrentToken()) {

            JsonToken.VALUE_NUMBER_INT, JsonToken.VALUE_NUMBER_FLOAT -> JsNumber(jp.getDecimalValue())

            JsonToken.VALUE_STRING -> JsString(jp.getText())

            JsonToken.VALUE_TRUE -> JsBoolean(true)

            JsonToken.VALUE_FALSE -> JsBoolean(false)

            JsonToken.VALUE_NULL -> JsNull

            JsonToken.START_ARRAY -> {
                stack.push(ArrayContext())
                null
            }

            JsonToken.END_ARRAY -> {
                val arrayContext = stack.pop()
                if (arrayContext is ArrayContext) JsArray(arrayContext.content) else {
                    throw JsonParseException("Array context expected", jp.location)
                }
            }

            JsonToken.START_OBJECT -> {
                stack.push(ObjectContext())
                null
            }

            JsonToken.FIELD_NAME -> {
                val objectContext = stack.pop()
                if (objectContext is ObjectContext) {
                    stack.push(objectContext.setField(jp.getCurrentName()!!))
                    null
                } else throw JsonParseException("Object context expected", jp.location)
            }

            JsonToken.END_OBJECT -> {
                val objectContext = stack.pop()
                if (objectContext is ObjectContext) JsObject(objectContext.content) else {
                    throw JsonParseException("Object context expected", jp.location)
                }
            }

            JsonToken.NOT_AVAILABLE -> throw JsonParseException("Non-blocking parser not supported", jp.location)
            else -> throw JsonParseException("Unexpected token: ${jp.getCurrentToken()?.name()}", jp.location)
        }

        jp.nextToken() // Read ahead

        return if (value != null && stack.isEmpty() && jp.getCurrentToken() == null) {
            value
        } else if (value != null && stack.isEmpty()) {
            throw JsonParseException("Unexpected value", jp.location)
        } else {
            val toPass = if (value == null) stack else {
                val previous = stack.pop()!!
                stack.push(previous.addValue(value, jp))
                stack
            }

            doDeserialize(jp, context, toPass)
        }
    }

    override fun getNullValue(): Any? = JsNull
}

class KsonDeserializers() : Deserializers.Base() {
    override fun findBeanDeserializer(javaType: JavaType?, config: DeserializationConfig?, beanDesc: BeanDescription?): JsonDeserializer<out Any?>? {
        val klass = javaType?.getRawClass()!!
        return if (javaClass<JsValue>().isAssignableFrom(klass) || klass == javaClass<JsNull>()) {
            JsValueDeserializer(klass)
        } else null
    }
}

class KsonSerializers : Serializers.Base() {
    override fun findSerializer(config: SerializationConfig?, javaType: JavaType?, beanDesc: BeanDescription?): JsonSerializer<out Any?>? {
        return if (javaClass<JsValue>().isAssignableFrom(beanDesc!!.getBeanClass()!!)) {
            JsValueSerializer()
        } else null
    }
}

public class KsonModule() : SimpleModule("kson", Version.unknownVersion()) {
    override fun setupModule(context: Module.SetupContext?) {
        context!!.addDeserializers(KsonDeserializers())
        context.addSerializers(KsonSerializers())
    }
}