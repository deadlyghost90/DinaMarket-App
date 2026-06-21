package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import com.example.ui.theme.MyApplicationTheme

class TermsAndConditionsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Terms & Conditions", fontWeight = FontWeight.Bold, color = Color.White) },
                            navigationIcon = {
                                IconButton(
                                    onClick = { finish() },
                                    modifier = Modifier.testTag("terms_back_btn")
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
                                    text = "DinaMarket Pakistan Legal Framework",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                SectionTitle("1. Decentralized Marketplace Model")
                                SectionBody(
                                    "DinaMarket Pakistan functions strictly as a decentralized peer-to-peer advertising and directory application. " +
                                            "We do not act as an escrow agent, intermediary, or contracting party in any sale. All sales remain 100% " +
                                            "exclusive contractual arrangements between the independent Merchant (seller) and the buyer."
                                )

                                SectionTitle("2. PECA Laws Compliance Code")
                                SectionBody(
                                    "DinaMarket Pakistan complies fully with Pakistan's Prevention of Electronic Crimes Act (PECA 2016). " +
                                            "Illegal acts, listing of banned contraband, stolen goods, weapons, unapproved pharmaceuticals, or obscene " +
                                            "media is strictly prohibited. Merchants assume complete civil and criminal liability under Pakistan law for " +
                                            "any material posted to our servers."
                                )

                                SectionTitle("3. Wallet Payment Verification via TID")
                                SectionBody(
                                    "Payment exchanges occur completely outside our platform directly from buyer to seller. Buyers must pay " +
                                            "the seller's declared EasyPaisa or JazzCash wallet address and submit the official, unique 12-digit " +
                                            "Transaction ID (TID) generated by the respective mobile services. The merchant must verify TID legality " +
                                            "independently before executing or dispatching the order."
                                )

                                SectionTitle("4. Surcharge and Commission Model")
                                SectionBody(
                                    "Store launching, setting up, and listing products on DinaMarket is 100% Free. However, to sustain platform operations " +
                                            "and direct cloud server infrastructure costs, merchants agree to pay a small Success-Based Service Surcharge " +
                                            "fraction after their established store successfully settles past its initial onboarding baseline of 5 to 10 free orders."
                                )

                                SectionTitle("5. Self-Pickup & Logistic Pathways")
                                SectionBody(
                                    "DinaMarket Pakistan does not operate, manage, or contract third-party delivery dispatch riders. All shipping routes, " +
                                            "timelines, shipping costs, self-pickups, or localized physical handovers belong exclusively to the bilateral " +
                                            "coordination of the Merchant and the Buyer."
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = { finish() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("terms_accept_btn"),
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
