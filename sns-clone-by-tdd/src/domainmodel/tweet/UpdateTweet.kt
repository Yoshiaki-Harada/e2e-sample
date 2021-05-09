package com.harada.domain.model.message

import com.harada.domainmodel.tag.Tags

data class UpdateTweet(val text: Text?, val tags: Tags?) {
    fun isOverLimitTweetLength() = text?.let { !it.isInCharacterLimit() } ?: false
    fun length() = text?.length() ?: 0
}
