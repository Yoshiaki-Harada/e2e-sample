package com.harada.usecase

import com.harada.domain.model.message.TweetId
import com.harada.domain.model.message.UpdateTweet

interface ITweetUpdateUseCase {
    fun execute(tweetId: TweetId, tweet: UpdateTweet)
}