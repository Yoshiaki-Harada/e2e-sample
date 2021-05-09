package com.harada.usecase

import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.UpdateTweet
import com.harada.port.TweetWriteStore

class TweetUpdateUseCase(private val store: TweetWriteStore) :
    ITweetUpdateUseCase {
    override fun execute(tweetId: TweetId, tweet: UpdateTweet) {
        tweet.text?.let {
            if (tweet.isOverLimitTweetLength()) throw OverTweetLengthException(tweet.length())
        }
        store.update(tweetId, tweet)
    }
}