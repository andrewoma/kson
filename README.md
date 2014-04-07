#### Kson Overview

Kson is a library for producing and consuming JSON in Kotlin.

It is a thin wrapper over Jackson. It is heavily inspired by (in fact, it's largely a port from)
[Play's JSON support](http://www.playframework.com/documentation/2.0/ScalaJson).

##### Status

This is just an experiment - not for production use. However, it does appear to successfully
serialise to and from JSON, support building in a DSL fashion and allow extraction of attributes.

See the [tests](/kson/src/test/java/com/github/andrewoma/kson) for examples.

Build status: [![Build Status](https://travis-ci.org/andrewoma/kson.svg?branch=master)](https://travis-ci.org/andrewoma/kson)