package com.harada.driver.dao

import com.harada.driver.entity.TweetEntity
import com.harada.driver.entity.TweetUpdateEntity
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.`java-time`.datetime
import java.time.LocalDateTime
import java.util.*

object Tweets : UUIDTable() {
    val userId = reference("user_id", Users)
    val text = varchar("text", 500)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class TweetDao(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<TweetDao>(Tweets) {
        fun create(entity: TweetEntity) = TweetDao.new(entity.id) {
            userId = EntityID(entity.userId, Tweets)
            text = entity.text
            createdAt = entity.createdAt
            updatedAt = entity.updatedAt
        }.id.value

        fun update(entity: TweetUpdateEntity) {
            TweetDao.findById(entity.id)?.let { tweetDao ->
                entity.text?.let {
                    tweetDao.text = it
                }
                tweetDao.updatedAt = entity.updatedAt
            }
        }

        fun createTweetFilterCondition(textFilter: String?, createFilter: SqlDateTimeFilter?): Op<Boolean>? =
            when {
                textFilter != null && createFilter != null -> {
                    Op.build {
                        Tweets.text like textFilter and Tweets.createdAt.between(
                            createFilter.from,
                            createFilter.to
                        )
                    }
                }
                textFilter != null -> {
                    Op.build {
                        Tweets.text like "%${textFilter}%"
                    }
                }
                createFilter != null -> {
                    Op.build {
                        Tweets.createdAt.between(createFilter.from, createFilter.to)
                    }
                }
                else -> null
            }


        fun getTweetQuery(filter: SqlTweetFilter): Query {
            if (filter.tagFilter == null)
                return TweetDao.createTweetFilterCondition(filter.textFilter, filter.createFilter)
                    ?.let { Tweets.select { it } }
                    ?: kotlin.run { return@run Tweets.selectAll() }

            val tagTable = Tags.alias("t")
            val mapTable = TagTweetMap.alias("m")
            val query = Tweets
                .innerJoin(mapTable, { Tweets.id }, { mapTable[TagTweetMap.tweetId] })
                .innerJoin(tagTable, { tagTable[Tags.id] }, { mapTable[TagTweetMap.tagId] })
                .slice(Tweets.columns)
                .select { tagTable[Tags.name] inList filter.tagFilter.tags }.groupBy(Tweets.id)
                .having { Tweets.id.count() eq filter.tagFilter.tags.size.toLong() }

            return TweetDao.createTweetFilterCondition(filter.textFilter, filter.createFilter)
                ?.let { query.andWhere { it } }
                ?: kotlin.run { return@run query }
        }

        fun get(filter: SqlTweetFilter): List<TweetDao> {
            val cond = when {
                filter.textFilter != null && filter.createFilter != null -> {
                    Op.build {
                        Tweets.text like filter.textFilter and Tweets.createdAt.between(
                            filter.createFilter.from,
                            filter.createFilter.to
                        )
                    }
                }
                filter.textFilter != null -> {
                    Op.build {
                        Tweets.text like "%${filter.textFilter}%"
                    }
                }
                filter.createFilter != null -> {
                    Op.build {
                        Tweets.createdAt.between(filter.createFilter.from, filter.createFilter.to)
                    }
                }
                else -> null
            }
            return cond?.let { TweetDao.find(it).toList() } ?: TweetDao.all().toList()
        }
    }

    var userId by Tweets.userId
    var text by Tweets.text
    var createdAt by Tweets.createdAt
    var updatedAt by Tweets.updatedAt
}

data class SqlTweetFilter(
    val textFilter: String? = null,
    val createFilter: SqlDateTimeFilter? = null,
    val tagFilter: SqlTagFilter? = null
)

data class SqlTagFilter(val tags: List<String>)

data class SqlDateTimeFilter(val from: LocalDateTime, val to: LocalDateTime)