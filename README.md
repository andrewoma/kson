#### Kson Overview

Kson is a library for producing and consuming JSON in Kotlin.

It is a thin wrapper over Jackson. It is heavily inspired by (in fact, it's largely a port from)
[Play's JSON support](http://www.playframework.com/documentation/2.0/ScalaJson).

##### Status

This is just an experiment - not for production use. However, it does appear to successfully
serialise to and from JSON, support building in a DSL fashion and allow extraction of attributes.

##### An example of manually building JSON:
```kotlin
val person = JsObject(
        "firstName" to "Andrew".js,
        "lastName" to "O'Malley".js,
        "age" to 21.js,
        "adult" to true.js,
        "address" to JsObject(
                "number" to "88".js,
                "street" to "Chapel Street".js,
                "suburb" to "Windsor".js,
                "state" to "VIC".js,
                "postCode" to "3181".js
        ),
        "pets" to JsArray(
                JsObject(
                        "kind" to "dog".js,
                        "name" to "Rover".js
                ),
                JsObject(
                        "kind" to "cat".js,
                        "name" to "Kitty".js
                )
        )
)
```

##### An example of extracting data from JSON:
```kotlin
val street = person["address"]["street"] // Chapel Street
val petNames = person["pets"].map { it["name"].asString() } // List("Rover", "Kitty")
```

See the [tests](/kson/src/test/java/com/github/andrewoma/kson) for more examples.

[![Build Status](https://travis-ci.org/andrewoma/kson.svg?branch=master)](https://travis-ci.org/andrewoma/kson)
