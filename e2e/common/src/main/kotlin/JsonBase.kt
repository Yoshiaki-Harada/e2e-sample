package com.example.common

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath

interface JsonBase {
    fun getJsonDocumentContext(json: String): DocumentContext = JsonPath.parse(json)
}