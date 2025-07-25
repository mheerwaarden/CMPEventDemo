package com.github.mheerwaarden.eventdemo.data.model

import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.user
import kotlinx.serialization.Serializable

@Serializable
data class User(
    override val id: String = "",
    val email: String,
    val username: String = "",
    val name: String = "",
    val avatar: String = ""
) : ModelItem() {
    companion object {
        val typeNameResId = Res.string.user
    }

    override fun getTypeNameResId() = typeNameResId
    override fun getDisplayName() = name
}
