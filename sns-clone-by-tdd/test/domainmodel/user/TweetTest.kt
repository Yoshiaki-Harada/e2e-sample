package domainmodel.user

import com.harada.domain.model.message.LIMIT_TWEET_LENGTH
import createTweet
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TweetTest {
    @Test
    public fun `Tweetの文字数が制限以内であることを確認できる`() {
        assertTrue(createTweet(text = "1".repeat(LIMIT_TWEET_LENGTH)).text.isInCharacterLimit())
        assertFalse(createTweet(text = "1".repeat(LIMIT_TWEET_LENGTH+1)).text.isInCharacterLimit())
    }
}