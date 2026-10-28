package com.example.direktoripenggunasaya

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val email: String
)

data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String
)

data class PostBaru(
    val title: String,
    val body: String,
    val userId: Int
)