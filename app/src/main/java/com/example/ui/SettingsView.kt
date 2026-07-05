package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.MainViewModel
import com.example.ui.theme.*

@Composable
fun SettingsView(
    viewModel: MainViewModel
) {
    val currentBaseUrl by viewModel.baseUrlState.collectAsState()
    val currentUsername by viewModel.usernameState.collectAsState()
    val currentCookies by viewModel.cookiesState.collectAsState()

    var tempBaseUrl by remember(currentBaseUrl) { mutableStateOf(currentBaseUrl) }
    var tempUsername by remember(currentUsername) { mutableStateOf(currentUsername) }
    var tempCookies by remember(currentCookies) { mutableStateOf(currentCookies) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Branded Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BentoPurpleAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = BentoPurpleText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = "Server Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Normal,
                    color = BentoTextPrimary
                )
                Text(
                    text = "Configure custom backend API and comments profile",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // API Endpoint Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorder),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Dns, contentDescription = "Dns", tint = BentoPurpleText, modifier = Modifier.size(20.dp))
                    Text(
                        text = "BACKEND API URL",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }

                Text(
                    text = "Specify the custom Python Flask backend server endpoint that processes yt-dlp metadata downloads.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted
                )

                OutlinedTextField(
                    value = tempBaseUrl,
                    onValueChange = { tempBaseUrl = it },
                    singleLine = true,
                    placeholder = { Text("https://yourserver.com") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPurpleText,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoPurpleText,
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary,
                        focusedContainerColor = BentoBg.copy(alpha = 0.5f),
                        unfocusedContainerColor = BentoBg.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("baseUrlInput")
                )

                Button(
                    onClick = {
                        if (tempBaseUrl.isNotBlank()) {
                            viewModel.updateBaseUrl(tempBaseUrl)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPurpleAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("saveBaseUrlBtn")
                ) {
                    Text(text = "APPLY URL", color = BentoPurpleText, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Comment Profile Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorder),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = "User", tint = BentoPurpleText, modifier = Modifier.size(20.dp))
                    Text(
                        text = "COMMENTER IDENTITY",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }

                Text(
                    text = "Choose the display name that appears when posting comments and replies under streaming streams.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted
                )

                OutlinedTextField(
                    value = tempUsername,
                    onValueChange = { tempUsername = it },
                    singleLine = true,
                    placeholder = { Text("Anonymous") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPurpleText,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoPurpleText,
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary,
                        focusedContainerColor = BentoBg.copy(alpha = 0.5f),
                        unfocusedContainerColor = BentoBg.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("usernameInput")
                )

                Button(
                    onClick = {
                        if (tempUsername.isNotBlank()) {
                            viewModel.updateUsername(tempUsername)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPurpleAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("saveUsernameBtn")
                ) {
                    Text(text = "APPLY PROFILE", color = BentoPurpleText, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Cookies Configuration Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, BentoBorder),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.VpnKey, contentDescription = "Cookies", tint = BentoPurpleText, modifier = Modifier.size(20.dp))
                    Text(
                        text = "COOKIES / AUTH",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextPrimary
                    )
                }

                Text(
                    text = "Paste your cookie string (e.g. from cookie editor or Netscape format) to fetch premium/restricted Hotstar content.",
                    style = MaterialTheme.typography.bodySmall,
                    color = BentoTextMuted
                )

                OutlinedTextField(
                    value = tempCookies,
                    onValueChange = { tempCookies = it },
                    minLines = 3,
                    maxLines = 6,
                    placeholder = { Text("paste cookies here...") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BentoPurpleText,
                        unfocusedBorderColor = BentoBorder,
                        focusedLabelColor = BentoPurpleText,
                        focusedTextColor = BentoTextPrimary,
                        unfocusedTextColor = BentoTextPrimary,
                        focusedContainerColor = BentoBg.copy(alpha = 0.5f),
                        unfocusedContainerColor = BentoBg.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cookiesInput")
                )

                Button(
                    onClick = {
                        viewModel.updateCookies(tempCookies)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPurpleAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.End)
                        .testTag("saveCookiesBtn")
                ) {
                    Text(text = "APPLY COOKIES", color = BentoPurpleText, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Credits / Info footer
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "OURIN MEDIA SUITE V1.0 // BENTO GRID THEME",
                style = MaterialTheme.typography.labelSmall,
                color = BentoTextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

