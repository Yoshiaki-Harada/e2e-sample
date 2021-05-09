package com.harada.port

import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.UpdateTweet

interface TweetWriteStore {
    fun save(tweet: Tweet): TweetId
    fun update(tweetId: TweetId, tweet: UpdateTweet)
}