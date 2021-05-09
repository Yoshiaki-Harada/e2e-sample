import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.ResponseException
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.client.statement.readText
import io.ktor.http.*

enum class RootPropertyType {
    Map, Array
}

interface HttpClient {
    suspend fun httpGet(uri: String, rootPropertyType: RootPropertyType, headers: Map<String, String> = emptyMap()) =
        try {
            httpGet(uri, headers).let {
                val body: Any = when (rootPropertyType) {
                    RootPropertyType.Map -> it.receive<Map<String, Any>>()
                    RootPropertyType.Array -> it.receive<List<Map<String, Any>>>()
                }
                Triple(it.status, it.headers, body)
            }
        } catch (e: ResponseException) {
            Triple(e.response.status, e.response.headers, emptyMap<String, Any>())
        }

    suspend fun httpGetBinary(uri: String) =
        httpClient.get<HttpResponse>(uri)
            .let { Triple(it.status, it.headers, it.readBytes()) }

    suspend fun httpGetText(uri: String, headers: Map<String, String> = emptyMap()) =
        httpGet(uri, headers).let { Triple(it.status, it.headers, it.readText()) }

    suspend fun httpGet(uri: String, headers: Map<String, String>) =
        httpClient.get<HttpResponse>(uri) { headers { headers.forEach { (name, value) -> append(name, value) } } }

    suspend fun httpPost(uri: String, params: Map<String, Any>, headers: Map<String, String> = emptyMap()) =
        try {
            httpClient.post<HttpResponse>(uri) {
                body = params
                headers { headers.forEach { name, value -> append(name, value) } }
            }
                .let { response ->
                    Triple(
                        response.status,
                        response.headers,
                        if (response.contentLength()
                                ?.let { it > 0L } == true
                        ) response.receive() else emptyMap()
                    )
                }
        } catch (e: ResponseException) {
            Triple(e.response.status, e.response.headers, emptyMap<String, Any>())
        }

    suspend fun httpPut(uri: String, body: Map<String, Any>, headers: Map<String, String> = emptyMap()) =
        try {
            httpClient.put<HttpResponse>(uri) {
                this.body = body
                headers { headers.forEach { (name, value) -> append(name, value) } }
            }
                .let { response ->
                    Triple(
                        response.status,
                        response.headers,
                        if (response.contentLength()
                                ?.let { it > 0L } == true
                        ) response.receive<Map<String, Any>>() else emptyMap()
                    )
                }
        } catch (e: ResponseException) {
            Triple(e.response.status, e.response.headers, emptyMap<String, Any>())
        }

    suspend fun httpDelete(uri: String, body: Map<String, Any>, headers: Map<String, String> = emptyMap()) =
        try {
            httpClient.delete<HttpResponse>(uri) {
                if (body.isNotEmpty()) {
                    this.body = body
                }
                headers { headers.forEach { (name, value) -> append(name, value) } }
            }
                .let { response ->
                    Triple(
                        response.status,
                        response.headers,
                        if (response.contentLength()
                                ?.let { it > 0L } == true
                        ) response.receive<Map<String, Any>>() else emptyMap()
                    )
                }
        } catch (e: ResponseException) {
            Triple(e.response.status, e.response.headers, emptyMap<String, Any>())
        }

    companion object {
        val httpClient =
            HttpClient() {
                install(JsonFeature) { serializer = JacksonSerializer() }
            }
    }
}
