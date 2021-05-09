package com.harada.usecase

import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.Tweet
import com.harada.domain.model.message.UpdateTweet

interface ITweetCreateUseCase {
    fun execute(tweet: Tweet): TweetId
}