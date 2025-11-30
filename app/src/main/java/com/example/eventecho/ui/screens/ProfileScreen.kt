package com.example.eventecho.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.eventecho.ui.navigation.Routes
import com.example.eventecho.ui.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen( navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    ProfileHeader(
                        name = uiState.user.name,
                        location = uiState.user.location,
                        bio = uiState.user.bio,
                        onEditClick = { navController.navigate(Routes.EditProfile.route) }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    MemberSinceFooter(date = uiState.user.memberSince)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    StatsRow(
                        attended = uiState.user.eventsAttended,
                        created = uiState.user.eventsCreated,
                        favorites = uiState.user.favorites
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }


                item {
                    SectionTitle("Recent Events")
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Recent Events List
                items(uiState.recentEvents) { event ->
                    RecentEventCard(title = event.title, date = event.date)
                    Spacer(modifier = Modifier.height(12.dp))
                }

            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, location: String, bio: String, onEditClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // user avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD1DC))
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = "Avatar",
                tint = Color(0xFFD81B60),
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = location, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = bio, color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            OutlinedButton(
                onClick = onEditClick,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile", color = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun StatsRow(attended: Int, created: Int, favorites: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCard(icon = Icons.Default.CalendarToday, count = attended.toString(), label = "Events Attended")
        StatCard(icon = Icons.Default.Group, count = created.toString(), label = "Events Created")
        StatCard(icon = Icons.Default.FavoriteBorder, count = favorites.toString(), label = "Favorites")
    }
}

@Composable
fun StatCard(icon: ImageVector, count: String, label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(105.dp)
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = label, fontSize = 10.sp, color = Color.Gray, lineHeight = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
fun MemberSinceFooter(date: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Member since $date", color = Color.Gray)
    }
}

// TODO: backend
@Composable
fun RecentEventCard(title: String, date: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // TODO: Images 
            // Placeholder for Event Image
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Medium)
                Text(text = date, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.fillMaxWidth()
    )
}