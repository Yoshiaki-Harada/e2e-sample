package com.harada.domainmodel.user


data class UserFilter(val name: NameFilter? = null, val old: OldFilter? = null)

data class NameFilter(val value: String)

data class OldFilter(val from: Int, val to: Int)

