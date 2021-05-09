package com.harada.domain.model.message

import com.harada.domainmodel.tag.Tags
import com.harada.domainmodel.user.UserId

const val LIMIT_TWEET_LENGTH = 200

data class Tweet(
    val userId: UserId,
    val text: Text,
    val tags: Tags,
    val replyTo: TweetId? = null
) {
    fun length() = text.length()
}

data class Text(val value: String) {
    fun isInCharacterLimit() = value.length <= LIMIT_TWEET_LENGTH
    fun isOverCharacterLimit() = !isInCharacterLimit()
    fun length() = value.length
}