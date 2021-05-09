package assert

import com.example.common.JsonBase
import com.example.common.assert.JsonOrderAssert
import org.amshove.kluent.AnyException
import org.amshove.kluent.`should not throw`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class JsonOrderAssertTest : JsonOrderAssert, JsonBase {
    @Test
    fun Jsonの配列が降順になっていない場合に例外を発生する() {
        val jsonString = """
            {
                "users": [
                    {"id": 1, "age": 21},
                    {"id": 3, "age": 22},
                    {"id": 2, "age": 17}
                ]
            }
            """".trimIndent()
        val json = getJsonDocumentContext(jsonString)

        invoking {
            assertOrderDesc(
                json,
                "users",
                "age",
            ) { it: Int -> it }
        } `should throw` AnyException
    }

    @Test
    fun Jsonの配列が降順になっていない場合は例外を発生しない() {
        val jsonString = """
            {
                "users": [
                    {"id": 1, "age": 23},
                    {"id": 3, "age": 22},
                    {"id": 2, "age": 17}
                ]
            }
            """".trimIndent()
        val json = getJsonDocumentContext(jsonString)

        invoking {
            assertOrderDesc(json, "users", "age") { it: Int -> it }
        } `should not throw` AnyException
    }

    @Test
    fun Jsonの配列が昇順になっていない場合は例外を発生しない() {
        val jsonString = """
            {
                "users": [
                    {"id": 1, "age": 17},
                    {"id": 3, "age": 22},
                    {"id": 2, "age": 25}
                ]
            }
            """".trimIndent()
        val json = getJsonDocumentContext(jsonString)

        invoking {
            assertOrderAsc(json, "users", "age") { it: Int -> it }
        } `should not throw` AnyException
    }

    @Test
    fun パーサーによって変換された後でソートしアサートすることができる() {
        val jsonString = """
            {
                "users": [
                    {"id": 1, "date": "2020-05-15T11:11:05Z"},
                    {"id": 3, "date": "2020-05-12T11:11:05Z"},
                    {"id": 2, "date": "2020-05-14T11:11:05Z"}
                ]
            }
            """".trimIndent()
        val json = getJsonDocumentContext(jsonString)

        invoking {
            assertOrderAsc(json, "users", "date") { it: String -> ZonedDateTime.parse(it) }
        } `should throw` AssertionError::class
    }
}