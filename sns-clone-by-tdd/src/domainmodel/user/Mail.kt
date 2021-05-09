package com.harada.domainmodel.user

data class Mail(val value: String) {
    public fun isValid() = value.contains("@")
    public fun isInValid() = !isValid()
}
