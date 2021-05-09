package com.harada.usecase

import com.harada.domain.model.message.LIMIT_TWEET_LENGTH
import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId
import com.harada.port.TweetNotFoundException
import com.harada.port.UserNotFoundException
import com.harada.port.TweetQueryService
import com.harada.port.TweetWriteStore
import com.harada.port.UserQueryService

data class OverTweetLengthException(val length: Int) :
    Throwable("Tweet Length must be in $LIMIT_TWEET_LENGTH. but actually $length")

class TweetCreateUseCase(
    private val store: TweetWriteStore,
    private val userQuery: UserQueryService,
    private val tweetQuery: TweetQueryService
) :
    ITweetCreateUseCase {
    override fun execute(tweet: Tweet): TweetId {
        if (userQuery.isNotFound(tweet.userId)) throw UserNotFoundException(tweet.userId.value)
        if (tweet.text.isOverCharacterLimit()) throw OverTweetLengthException(tweet.length())
        tweet.replyTo?.let {
            if (tweetQuery.isNotFound(it)) throw TweetNotFoundException(it.value)
        }
        return store.save(tweet)
    }
}