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

package com.github.andrewoma.kson.ext

import com.github.andrewoma.kson.JsString
import com.github.andrewoma.kson.JsNull
import com.github.andrewoma.kson.JsValue
import com.github.andrewoma.kson.JsBoolean
import com.github.andrewoma.kson.JsNumber
import java.math.BigDecimal

public val String?.json: JsValue
    get() = if (this == null) JsNull() else JsString(this)

public val Boolean?.json: JsValue
    get() = if (this == null) JsNull() else JsBoolean(this)

public val Int?.json: JsValue
    get() = if (this == null) JsNull() else JsNumber(BigDecimal(this))

public val Long?.json: JsValue
    get() = if (this == null) JsNull() else JsNumber(BigDecimal(this))

public val Double?.json: JsValue
    get() = if (this == null) JsNull() else JsNumber(BigDecimal(this))
