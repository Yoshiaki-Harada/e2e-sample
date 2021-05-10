import com.example.common.*
import io.mockk.*
import org.amshove.kluent.`should be equal to`
import org.dbunit.dataset.ITable
import org.dbunit.dataset.ITableMetaData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class ITableBaseTest : ResourceBase, ITableBase {

    @AfterEach
    fun after() {
        unmockkAll()
    }

    @Test
    fun MapのDiffをカウントする() {
        val same = listOf<Any>("1") to mapOf<String, Any>(
            "user" to "taro",
            "age" to 10
        )
        val diff1 = listOf<Any>("2") to mapOf<String, Any>(
            "user" to "yuji",
            "age" to 11
        )
        val diff2 = listOf<Any>("2") to mapOf<String, Any>(
            "user" to "yuji",
            "age" to 15
        )

        val m1 = mapOf(
            same,
            diff1,
        )

        val m2 = mapOf(
            same,
            diff2,
        )
        diffValueCount(m1, m2) `should be equal to` 1
    }

    @Test
    fun テーブルのDiffをカウントする() {
        mockkStatic("ITableExtensionsKt")

        val m1 = mockk<Map<List<Any>, Map<String, Any>>>()
        val m2 = mockk<Map<List<Any>, Map<String, Any>>>()
        val t1 = mockk<ITable>()
        val t2 = mockk<ITable>()

        val meta = spyk<ITableMetaData>()
        every { t1.tableMetaData } returns meta
        every { t2.tableMetaData } returns meta
        every { t1.createMap() } returns m1
        every { t2.createMap() } returns m2
        every { diffValueCount(m1, m2) } returns 1
        class TestITableBase : ITableBase

        val target = spyk<TestITableBase>()
        every { target.diffValueCount(m1, m2) } returns 1

        target.diffRecordValueCount(t1, t2) `should be equal to` 1
        verify { t1.tableMetaData }
        verify { t2.tableMetaData }
        verify { t1.createMap() }
        verify { t2.createMap() }
        verify { target.diffValueCount(m1, m2) }
    }
}