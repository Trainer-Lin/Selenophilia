package com.example.tmusic.personal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Headset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.tmusic.personal.data.MostPlayedSong
import com.example.tmusic.personal.mvi.PersonalState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tmusic.R
import com.example.tmusic.localMusicList.data.room.MusicEntity
import com.example.tmusic.personal.mvi.PersonalViewModel

@Composable
fun PersonalScreen(
    viewModel: PersonalViewModel,
    onBackClick: () -> Unit,
    onPlaySong: (List<MusicEntity>, Int) -> Unit
) {
    val state by viewModel.viewState.collectAsState()
    PersonalContent(state = state, onBackClick = onBackClick, onPlaySong = onPlaySong)
}

@Composable
fun PersonalContent(
    state: PersonalState,
    onBackClick: () -> Unit,
    onPlaySong: (List<MusicEntity>, Int) -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF8D8E1),
            Color(0xFFD1A8E3),
            Color(0xFFA9B1F1)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
    ) {
        // Background large "PERSONAL" text
        Text(
            text = "PERSONAL",
            color = Color.White.copy(alpha = 0.15f),
            fontSize = 58.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1).sp,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .offset(y = 20.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1D182E)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Top section: Days used
            Text(
                text = "月亮记得\n已与你相遇 ${state.daysUsed} 天",
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1D182E),
                lineHeight = 40.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    // Most Listened
                    SectionTitle("❤ 您最爱听")
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassmorphicCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = state.mostPlayedSong?.coverPath.takeIf { !it.isNullOrEmpty() } ?: R.drawable.bg_moon_new,
                                contentDescription = "Album Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = state.mostPlayedSong?.title ?: "暂无数据",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D182E)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Played ${state.mostPlayedSong?.playCount ?: 0} times",
                                    fontSize = 14.sp,
                                    color = Color(0xFF4C444D)
                                )
                            }
                        }
                    }
                }

                item {
                    // Total Listening
                    SectionTitle("🎵 总听歌时长")
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassmorphicCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headset,
                                contentDescription = "Music",
                                tint = Color(0xFF1D182E),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = state.totalPlayHours.toString(),
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D182E)
                                )
                                Text(
                                    text = "h",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4C444D),
                                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    // Study Tree Hole
                    SectionTitle("🦉 学习树洞")
                    Spacer(modifier = Modifier.height(12.dp))
                    GlassmorphicCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Cumulative Focus Count
                            Column {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_good),
                                        contentDescription = "Count",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF4C444D)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "您总共专注了",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4C444D)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = state.focusCount.toString(),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D182E)
                                    )
                                    Text(
                                        text = "次",
                                        fontSize = 16.sp,
                                        color = Color(0xFF1D182E),
                                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                    )
                                }
                            }

                            // Total Focus Duration
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color.White.copy(alpha = 0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_clock),
                                        contentDescription = "Duration",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color(0xFF4C444D)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "累计专注时长",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF4C444D),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = state.focusDurationHours.toString(),
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D182E)
                                    )
                                    Text(
                                        text = "小时",
                                        fontSize = 16.sp,
                                        color = Color(0xFF1D182E),
                                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    SectionTitle("⏰ 历史播放")
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (state.playHistory.isEmpty()) {
                    item {
                        Text(
                            text = "暂无历史播放记录",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold ,
                            color = Color(0xFF4C444D),
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    }
                } else {
                    itemsIndexed(state.playHistory) { index, music ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                                .clickable {
                                    onPlaySong(state.playHistory, index)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = music.albumArt.takeIf { !it.isNullOrEmpty() } ?: R.drawable.bg_moon_new,
                                contentDescription = "Album Cover",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = music.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1D182E),
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = music.artist,
                                    fontSize = 12.sp,
                                    color = Color(0xFF4C444D),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PersonalScreenPreview() {
    val mockState = PersonalState(
        daysUsed = 1234,
        focusCount = 156,
        focusDurationHours = 428,
        totalPlayHours = 856,
        mostPlayedSong = MostPlayedSong(
            title = "Midnight City",
            coverPath = "",
            playCount = 256
        ),
        playHistory = listOf(
            MusicEntity(id = 1, title = "Song A", artist = "Artist A", duration = 180000, uri = ""),
            MusicEntity(id = 2, title = "Song B", artist = "Artist B", duration = 240000, uri = "")
        )
    )
    MaterialTheme {
        PersonalContent(
            state = mockState,
            onBackClick = {},
            onPlaySong = { _, _ -> }
        )
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF745185),
        letterSpacing = 0.6.sp
    )
}

@Composable
fun GlassmorphicCard(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.4f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}





