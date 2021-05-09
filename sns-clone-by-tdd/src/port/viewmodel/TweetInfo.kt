package com.harada.viewmodel

data class TweetInfo(
    val id: String,
    val text: String,
    val userName: String,
    val createdAt: String,
    val updatedAt: String,
    val tags: List<String>,
    val replies: List<ReplyInfo>
)

data class ReplyInfo(
    val id: String,
    val text: String,
    val userName: String,
    val createdAt: String,
    val updatedAt: String,
    val tags: List<String>
)
data class TimeLine(val tweets: List<TweetInfo>)
