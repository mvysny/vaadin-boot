package com.example

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

fun wget(url: String): String {
    val client = HttpClient.newBuilder().build()
    val request = HttpRequest.newBuilder(URI.create(url)).build()
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    require(response.statusCode() == 200) {
        "$response failed: ${response.body()}"
    }
    val body = response.body()
    return body
}
