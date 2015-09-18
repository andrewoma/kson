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

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.InputStreamReader
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class KsonModuleTest {
    val mapper = ObjectMapper()

    @Before fun setUp() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(KsonModule())
    }

    fun resource(name: String) = InputStreamReader(javaClass.classLoader!!.getResourceAsStream(name)!!, "UTF8")

    @Test fun testSample1() {
        assertRoundTrip("sample1.json")
    }

    fun assertRoundTrip(resource: String) {
        val jsValue = mapper.readValue(resource(resource), JsValue::class.java)
        val output = mapper.writeValueAsString(jsValue)

        // Now parse the output and input using Jackson's JsonNode to compare
        val expected = mapper.readValue(resource(resource), JsonNode::class.java)
        val actual = mapper.readValue(output, JsonNode::class.java)
        assertEquals(expected, actual)
    }

    @Test(expected = JsonParseException::class) fun malformedWithExtraEndArray() {
        mapper.readValue("[1, 2, 3]]", JsValue::class.java)
    }

    @Test(expected = JsonParseException::class) fun malformedWithExtraEndObject() {
        mapper.readValue("""{"k": 1}}""", JsValue::class.java)
    }

    @Test(expected = JsonParseException::class) fun malformedObjectWithoutKey() {
        mapper.readValue("{1}", JsValue::class.java)
    }
}
