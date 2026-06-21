package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.example.ui.theme.MyApplicationTheme

class PrivacyPolicyActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = Color.White) },
                            navigationIcon = {
                                IconButton(
                                    onClick = { finish() },
                                    modifier = Modifier.testTag("privacy_back_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(innerPadding)
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    text = "DinaMarket Pakistan Privacy Protection",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                SectionTitle("1. Scope and Information Collected")
                                SectionBody(
                                    "We collect your Email/Password credentials through secured Firebase Authentication for account validation " +
                                            "and platform profiling. Merchants registering a business store also publish their contact emails, physical " +
                                            "address, and official WhatsApp numbers to allow seamless buyer discovery."
                                )

                                SectionTitle("2. Decentralized Transaction Disclosures")
                                SectionBody(
                                    "Because DinaMarket Pakistan utilizes third-party regional mobile wallets (EasyPaisa / JazzCash), " +
                                            "buyers transmit their 12-digit transaction ID (TID) to the specific merchant to verify payments. " +
                                            "We do not store or process card numbers, bank PINs, or secondary security passwords. Your financial " +
                                            "privacy remains fully secured by the payment providers."
                                )

                                SectionTitle("3. PECA Data Sharing Compliances")
                                SectionBody(
                                    "We comply strictly with data privacy provisions of Pakistan’s Prevention of Electronic Crimes Act (PECA 2016). " +
                                            "We will never sell or exploit merchant contact information, buyer locations, or chat messages. Data will only " +
                                            "be shared with law enforcement departments if legally subpoenaed by appropriate Pakistani judicial courts."
                                )

                                SectionTitle("4. Storage Location & Security protocol")
                                SectionBody(
                                    "Your profile logs, order records, and transaction indicators are stored in our secured Realtime " +
                                            "Database on highly encrypted Firebase cloud instances, minimizing any vectors for external penetration."
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { finish() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("privacy_accept_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("Accept & Close", fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun SectionTitle(text: String) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(6.dp))
    }

    @Composable
    private fun SectionBody(text: String) {
        Text(
            text = text,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = Color(0xFF2D3748)
        )
    }
}
