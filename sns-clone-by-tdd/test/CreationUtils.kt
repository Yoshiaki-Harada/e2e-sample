import com.harada.domain.model.message.Text
import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.UpdateTweet
import com.harada.domainmodel.tag.Tag
import com.harada.domainmodel.tag.Tags
import com.harada.domainmodel.user.*
import com.harada.formatter
import com.harada.rest.RequestTweet
import com.harada.rest.RequestUpdateTweet
import com.harada.rest.RequestUpdateUser
import com.harada.rest.RequestUser
import com.harada.viewmodel.*
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

val sdFormat = SimpleDateFormat("yyyy-MM-dd")
const val TEST_USER = "Tanaka Taro"
const val TEST_MAIL = "test@gmail.com"
const val TEST_BIRTHDAY = "1990-01-01"
fun createUser(
    name: String = TEST_USER,
    mail: String = TEST_MAIL,
    birthday: String = TEST_BIRTHDAY
) = User(
    UserName(name),
    Mail(mail),
    sdFormat.parse(birthday)
)

fun createUpdateUser(
    name: String? = null,
    mail: String? = null,
    birthday: String? = null
) = UpdateUser(
    name?.let { UserName(it) },
    mail?.let { Mail(mail) },
    birthday?.let { sdFormat.parse(birthday) }
)

fun createUserId(
    id: UUID = UUID.fromString("A0EEBC99-9C0B-4EF8-BB6D-6BB9BD380A11")
) = UserId(id)

fun createRequestUser(
    name: String = TEST_USER,
    mail: String = TEST_MAIL,
    birthday: String = TEST_BIRTHDAY
) = RequestUser(
    name, mail, birthday
)

fun createRequestUpdateUser(
    name: String? = null,
    mail: String? = null,
    birthday: String? = null
) = RequestUpdateUser(
    name, mail, birthday
)

fun createUserInfo(
    id: UUID = createUserId().value,
    name: String = createUser().name.value,
    mail: String = createUser().mail.value,
    birthday: Date = createUser().birthday,
    createdAt: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC")),
    updatedAt: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC"))
) = UserInfo(
    id = id.toString(),
    name = name,
    mail = mail,
    birthday = sdFormat.format(birthday),
    createdAt = formatter.format(createdAt),
    updatedAt = formatter.format(updatedAt)
)

fun createUsersInfo(
    userInfoList: List<UserInfo> = listOf(
        createUserInfo(),
        createUserInfo(
            id = UUID.fromString("ebda0d03-71e9-43bc-934b-b335f5708c7e"),
            name = "吉田 次郎",
            mail = "yoshida@gmail.com",
            birthday = sdFormat.parse("1995-01-01")
        )
    )
) = UsersInfo(userInfoList)

const val TEST_TWEET = "test tweet"
const val TEST_UPDATE_TWEET = "update test tweet"

fun createTweetId(
    id: UUID = UUID.fromString("d3c68568-ef91-412f-bee6-46dac9474566")
) = TweetId(id)

fun createRequestTweet(
    userId: UUID = createUserId().value,
    text: String = TEST_TWEET,
    replyTo: String? = null,
    tags: List<String> = listOf("tag")
) = RequestTweet(
    userId = userId.toString(),
    text = text,
    replyTo = replyTo,
    tags = tags
)

fun createTweet(
    userId: UUID = createUserId().value,
    text: String = TEST_TWEET,
    tags: List<String> = listOf("tag"),
    replyTo: UUID? = null
) = Tweet(
    UserId(userId),
    Text(text),
    Tags(tags.map { Tag(it) }),
    replyTo?.let { TweetId(it) })

fun createUpdateTweet(
    text: String? = TEST_UPDATE_TWEET,
    tags: List<String>? = listOf("tag")
) = UpdateTweet(
    text?.let { Text(it) },
    tags?.let {
        Tags(it.map {
            Tag(
                it
            )
        })
    }
)

fun createRequestUpdateTweet(
    text: String = TEST_UPDATE_TWEET,
    tags: List<String>? = listOf("tag")
) = RequestUpdateTweet(
    text = text,
    tags = tags
)

fun createTweetInfo(
    id: UUID = createTweetId().value,
    text: String = "",
    tags: List<String> = listOf("tag"),
    userName: String = "Tanaka Taro",
    replies: List<ReplyInfo> = emptyList(),
    createdAt: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC")),
    updatedAt: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC"))
) = TweetInfo(
    id = id.toString(),
    text = text,
    userName = userName,
    createdAt = formatter.format(createdAt),
    updatedAt = formatter.format(updatedAt),
    tags = tags,
    replies = replies
)

fun createReplyInfo(
    id: UUID = createTweetId().value,
    text: String = "",
    tags: List<String> = listOf("tag"),
    userName: String = "Tanaka Taro",
    createdAt: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC")),
    updatedAt: ZonedDateTime = ZonedDateTime.of(2020, 1, 1, 1, 0, 0, 0, ZoneId.of("UTC"))
) = ReplyInfo(
    id = id.toString(),
    text = text,
    userName = userName,
    createdAt = formatter.format(createdAt),
    updatedAt = formatter.format(updatedAt),
    tags = tags
)


fun createTimeLine() = TimeLine(
    listOf(
        createTweetInfo(),
        createTweetInfo(
            id = UUID.fromString("6207005e-d8ab-47ec-b483-189d7cbd726f"),
            text = "text 2",
            userName = "Tnaka Jiro",
            createdAt = ZonedDateTime.of(2020, 1, 1, 2, 0, 0, 0, ZoneId.of("UTC")),
            updatedAt = ZonedDateTime.of(2020, 1, 1, 2, 0, 0, 0, ZoneId.of("UTC"))
        )
    )
)
