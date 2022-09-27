package com.example.ktor_klient.api

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

object ApiFactory {
    private val client = HttpClient(OkHttp) {
        install(Resources)
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            host = "localhost"
            port = 8000
            url { protocol = URLProtocol.HTTP }
        }
    }
    fun getClient() = client
}
