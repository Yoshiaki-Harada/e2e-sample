package com.harada.usecase

import com.harada.domainmodel.user.Mail

data class InvalidMailException(private val mail: Mail) : Throwable("$mail is invalid format")