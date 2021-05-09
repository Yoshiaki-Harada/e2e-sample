package com.harada.port

import java.util.*

data class TweetNotFoundException(val tweetId: UUID) : Throwable("Tweet id: $tweetId not found.")
