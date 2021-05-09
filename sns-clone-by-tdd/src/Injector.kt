package com.harada

import com.harada.driver.dao.*
import com.harada.gateway.TweetQueryPostgresDB
import com.harada.gateway.TweetWritePostgresDB
import com.harada.gateway.UserQueryPostgresDB
import com.harada.gateway.UserWritePostgresDB
import com.harada.port.TweetQueryService
import com.harada.port.TweetWriteStore
import com.harada.port.UserQueryService
import com.harada.port.UserWriteStore
import com.harada.usecase.*
import org.jetbrains.exposed.sql.Database
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import org.postgresql.ds.PGSimpleDataSource

object Injector {
    val useCaseModule = Kodein.Module("useCase") {
        bind<IUserCreateUseCase>() with singleton { UserCreateUseCase(instance(),instance()) }
        bind<IUserUpdateUseCase>() with singleton { UserUpdateUseCase(instance(), instance()) }
        bind<ITweetCreateUseCase>() with singleton { TweetCreateUseCase(instance(), instance(), instance()) }
        bind<ITweetUpdateUseCase>() with singleton { TweetUpdateUseCase(instance()) }
    }

    val gatewayModule = Kodein.Module("gateway") {
        bind<UserWriteStore>() with singleton { UserWritePostgresDB(instance(), instance()) }
        bind<TweetWriteStore>() with singleton {
            TweetWritePostgresDB(
                instance(),
                instance(),
                instance(),
                instance(),
                instance(),
                instance()
            )
        }
        bind<UserQueryService>() with singleton { UserQueryPostgresDB(instance(), instance()) }
        bind<TweetQueryService>() with singleton { TweetQueryPostgresDB(instance(), instance(), instance(),instance(),instance()) }
    }

    val driverModule = Kodein.Module("driver") {
        bind<UserDao.Companion>() with singleton { UserDao }
        bind<TweetDao.Companion>() with singleton { TweetDao }
        bind<CommentDao.Companion>() with singleton { CommentDao }
        bind<TagDao.Companion>() with singleton { TagDao }
        bind<TagTweetMapDao.Companion>() with singleton { TagTweetMapDao }
        bind<TagCommentMapDao.Companion>() with singleton { TagCommentMapDao }
    }

    val dbModule = Kodein.Module("dataSource") {
        bind<Database>() with singleton {
            val dataSource = PGSimpleDataSource()
            dataSource.user = "developer"
            dataSource.password = "developer"
            dataSource.setURL("jdbc:postgresql://localhost:5432/sns_db")
            dataSource.loggerLevel = "DEBUG"
            Database.connect(dataSource)
        }
    }
    val kodein = Kodein {
        importAll(useCaseModule, gatewayModule, driverModule, dbModule)
    }
}