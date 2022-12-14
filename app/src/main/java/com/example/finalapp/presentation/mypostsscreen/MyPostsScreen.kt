package com.example.finalapp.presentation.mypostsscreen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.finalapp.R
import com.example.finalapp.data.remote.dto.PostDto
import com.example.finalapp.presentation.*
import com.example.finalapp.presentation.bottomnavigationmenu.BottomNavigationItem
import com.example.finalapp.presentation.bottomnavigationmenu.BottomNavigationMenu
import com.example.finalapp.ui.theme.Purple700
import com.example.finalapp.ui.theme.Teal200


data class PostRow(

    var post1: PostDto? = null,
    var post2: PostDto? = null,
    var post3: PostDto? = null,
) {
    fun isFull(): Boolean = post1 != null && post2 != null && post3 != null
    fun add(post: PostDto) {
        if (post1 == null) {
            post1 = post
        } else if (post2 == null) {
            post2 = post
        } else if (post3 == null) {
            post3 = post
        }
    }
}

@Composable
fun MyPostsScreen(navController: NavController, vm: AskiViewModel) {

    val newPostImageLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                val encoded = Uri.encode(it.toString())
                val route = ScreenDestination.NewPost.createRoute(encoded)
                navController.navigate(route)
            }
        }

    val userData = vm.userData.value
    val isLoading = vm.inProgress.value

    val postsLoading = vm.refreshPostsProgress.value
    val posts = vm.posts.value

    val numFollowers = vm.followers.value


    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                ProfileImage(imageUrl = userData?.imageUrl) {
                    newPostImageLauncher.launch("image/*")
                }
                Text(
                    text = "${posts.size}\npost${if (posts.size != 1) "s" else ""}",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$numFollowers\nfollower${if (numFollowers != 1) "s" else ""}",
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${userData?.following?.size ?: 0}\nfollowing", modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                val usernameDisplay =
                    if (userData?.username == null) "" else "@${userData.username}"
                Text(text = userData?.name ?: "", fontWeight = FontWeight.Bold)
                Text(text = usernameDisplay)
                Text(text = userData?.bio ?: "")
            }
            OutlinedButton(
                onClick = {
                    navigateTo(navController, ScreenDestination.Profile)
                }, modifier = Modifier
                    .background(Teal200)
                    .padding(8.dp)
                    .fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
                elevation = ButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp
                ),
                shape = RoundedCornerShape(10)
            ) {
                Text(text = "Edit Profile", color = Color.Black)
            }
            PostList(
                isContextLoading = isLoading,
                postsLoading = postsLoading,
                posts = posts,
                modifier = Modifier
                    .background(Teal200)
                    .weight(1f)
                    .padding(1.dp)
                    .fillMaxSize(),

                ) {
                navigateTo(
                    navController = navController,
                    dest = ScreenDestination.SinglePost,
                    NavParam("post", it)
                )
            }

        }
        BottomNavigationMenu(
            selectedItem = BottomNavigationItem.POSTS,
            navController = navController
        )
    }

    if (isLoading) {
        CommonProgressSpinner()
    }
}

@Composable
private fun ProfileImage(imageUrl: String?, onClick: () -> Unit) {
    Box(modifier = Modifier
        .padding(top = 16.dp)
        .clickable { onClick.invoke() }) {
        UserImageCard(
            userImage = imageUrl, modifier = Modifier
                .padding(8.dp)
                .size(80.dp)
        )

        Card(
            shape = CircleShape,
            border = BorderStroke(width = 2.dp, color = Color.White),
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .padding(bottom = 8.dp, end = 8.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Add",
                modifier = Modifier.background(Color.Blue),
                colorFilter = ColorFilter.tint(Color.White)
            )
        }
    }
}

@Composable
fun PostList(
    isContextLoading: Boolean,
    postsLoading: Boolean,
    posts: List<PostDto>,
    modifier: Modifier,
    onPostClick: (PostDto) -> Unit
) {
    if (postsLoading) {
        CommonProgressSpinner()
    } else if (posts.isEmpty()) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!isContextLoading) {
                Text(text = "No posts available")
            }
        }
    } else {
        LazyColumn(modifier = modifier) {
            val rows = arrayListOf<PostRow>()
            var currentRow = PostRow()
            rows.add(currentRow)

            for (post in posts) {
                if (currentRow.isFull()) {
                    currentRow = PostRow()
                    rows.add(currentRow)
                }
                currentRow.add(post = post)
            }
            items(items = rows) { row ->
                PostRowDisplay(item = row, onPostClick = onPostClick)
            }
        }
    }
}

@Composable
fun PostRowDisplay(item: PostRow, onPostClick: (PostDto) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Purple700)
            .height(120.dp)
    ) {
        PostImage(imageUrl = item.post1?.postImage, modifier = Modifier
            .weight(1f)
            .background(Purple700)
            .clickable {
                item.post1?.let { post ->
                    onPostClick(post)
                }
            }
        )
        PostImage(imageUrl = item.post2?.postImage, modifier = Modifier
            .weight(1f)
            .background(Purple700)
            .clickable {
                item.post2?.let { post ->
                    onPostClick(post)
                }
            }
        )
        PostImage(imageUrl = item.post3?.postImage, modifier = Modifier
            .weight(1f)
            .background(Purple700)
            .clickable {
                item.post3?.let { post ->
                    onPostClick(post)
                }
            }
        )
    }
}

@Composable
fun PostImage(imageUrl: String?, modifier: Modifier) {
    Box(modifier = modifier) {
        var imageModifier = Modifier
            .background(Purple700)
            .padding(1.dp)
            .fillMaxSize()
        if (imageUrl == null) {
            imageModifier = imageModifier.clickable(enabled = false) {}
        }

        CommonImage(data = imageUrl, modifier = imageModifier, contentScale = ContentScale.Crop)
    }
}