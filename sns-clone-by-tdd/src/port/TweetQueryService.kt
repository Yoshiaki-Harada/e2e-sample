package com.harada.port

import com.harada.domain.model.message.TweetId
import com.harada.domainmodel.tweet.TweetFilter
import com.harada.viewmodel.TweetInfo
import com.harada.viewmodel.TimeLine

interface TweetQueryService {
    fun getTweet(id: TweetId): TweetInfo
    fun getTimeLine(filter: TweetFilter): TimeLine
    fun isNotFound(tweetId: TweetId): Boolean
}