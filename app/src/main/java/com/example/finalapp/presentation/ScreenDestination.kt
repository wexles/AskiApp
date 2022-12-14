package com.example.finalapp.presentation

sealed class ScreenDestination(val route: String) {
    object Signup : ScreenDestination("signup")
    object Login : ScreenDestination("login")
    object Feed : ScreenDestination("feed")
    object Search : ScreenDestination("search")
    object MyPosts : ScreenDestination("myposts")
    object Profile : ScreenDestination("profile")
    object NewPost : ScreenDestination("newpost/{imageUri}") {
        fun createRoute(uri: String) = "newpost/$uri"
    }

    object SinglePost : ScreenDestination("singlepost")
    object Comments : ScreenDestination("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
}