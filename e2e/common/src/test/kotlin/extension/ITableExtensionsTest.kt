package extension

import createMap
import createPrimaryKeyValues
import createRecordMap
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.dbunit.dataset.Column
import org.dbunit.dataset.ITable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class ITableExtensionsTest {
    @AfterEach
    fun after() {
        unmockkAll()
    }

    @Test
    fun PrimaryKeyの値のリストを作成する() {
        val primaryKey1 = mockk<Column>() {
            every { columnName } returns "id"
        }
        val primaryKey2 = mockk<Column>() {
            every { columnName } returns "test_id"
        }
        val table = spyk<ITable>() {
            every { tableMetaData } returns mockk {
                every { primaryKeys } returns arrayOf(primaryKey1, primaryKey2)
            }
            every { rowCount } returns 2
            every { getValue(0, "id") } returns "id1"
            every { getValue(0, "test_id") } returns "test_id1"
        }
        table.createPrimaryKeyValues(0) shouldBeEqualTo listOf("id1", "test_id1")
    }

    @Test
    fun レコードをマップ構造に変換する() {
        val primaryKey1 = mockk<Column>() {
            every { columnName } returns "id"
        }
        val primaryKey2 = mockk<Column>() {
            every { columnName } returns "test_id"
        }
        val test = mockk<Column>() {
            every { columnName } returns "test"
        }

        val created = mockk<Column>() {
            every { columnName } returns "created"
        }
        val mockk = mockk<Timestamp>()

        val table = spyk<ITable>() {
            every { tableMetaData } returns mockk {
                every { primaryKeys } returns arrayOf(primaryKey1, primaryKey2)
                every { columns } returns arrayOf(primaryKey1, primaryKey2, test, created)
            }
            every { rowCount } returns 2
            every { getValue(0, "id") } returns "id1"
            every { getValue(0, "test_id") } returns "test_id1"
            every { getValue(0, "test") } returns "test1"
            every { getValue(0, "created") } returns mockk
        }
        val expected = mapOf(
            "id" to "id1",
            "test_id" to "test_id1",
            "test" to "test1",
            "created" to mockk
        )
        table.createRecordMap(0) shouldBeEqualTo expected
    }

    @Test
    fun TableをMap構造に変換する() {
        val primaryKey1 = mockk<Column>() {
            every { columnName } returns "id"
        }
        val primaryKey2 = mockk<Column>() {
            every { columnName } returns "test_id"
        }
        val test = mockk<Column>() {
            every { columnName } returns "test"
        }

        val created = mockk<Column>() {
            every { columnName } returns "created"
        }
        val mockk = mockk<Timestamp>()

        val table = spyk<ITable>() {
            every { tableMetaData } returns mockk {
                every { primaryKeys } returns arrayOf(primaryKey1, primaryKey2)
                every { columns } returns arrayOf(primaryKey1, primaryKey2, test, created)
            }
            every { rowCount } returns 2
            every { getValue(0, "id") } returns "id1"
            every { getValue(0, "test_id") } returns "test_id1"
            every { getValue(0, "test") } returns "test1"
            every { getValue(0, "created") } returns mockk
            every { getValue(1, "id") } returns "id2"
            every { getValue(1, "test_id") } returns "test_id2"
            every { getValue(1, "test") } returns "test2"
            every { getValue(1, "created") } returns mockk
        }

        val expected = mapOf<List<Any>, Map<String, Any>>(
            listOf("id1", "test_id1") to mapOf(
                "id" to "id1",
                "test_id" to "test_id1",
                "test" to "test1",
                "created" to mockk
            ),
            listOf("id2", "test_id2") to mapOf(
                "id" to "id2",
                "test_id" to "test_id2",
                "test" to "test2",
                "created" to mockk
            )
        )

        table.createMap() `should be equal to` expected
    }
}