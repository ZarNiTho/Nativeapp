package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RepairEntity
import com.example.ui.theme.BrandPrimary

@Composable
fun VoucherDialog(
    repair: RepairEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val receiptText = """
        ==============================
               Mobile ANSWER
            PHONE SERVICE & STORE
        ==============================
        ရက်စွဲ: ${repair.date}
        ဘောက်ချာ No: VR-${repair.vrno}
        ------------------------------
        အမည်: ${repair.name}
        မော်ဒယ်: ${repair.model}
        ပြစ်ချက်: ${repair.issue}
        ------------------------------
        စုစုပေါင်း: ${repair.income.toInt()} Ks
        ==============================
        ယုံကြည်စိတ်ချစွာ အပ်နှံခြင်းအတွက်
              ကျေးဇူးတင်ပါသည်!
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🧾 Digital Voucher Receipt",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BrandPrimary, RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mobile ANSWER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F1A2E)
                    )
                    Text(
                        text = "PHONE SERVICE & STORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4338CA)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    VoucherRow("ရက်စွဲ:", repair.date)
                    VoucherRow("ဘောက်ချာ No:", "VR-${repair.vrno}")
                    VoucherRow("အမည်:", repair.name)
                    VoucherRow("မော်ဒယ်:", repair.model)
                    VoucherRow("ပြစ်ချက်:", repair.issue)

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("စုစုပေါင်း:", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4338CA))
                        Text("${repair.income.toInt()} Ks", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE11D48))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "ယုံကြည်စိတ်ချစွာ အပ်နှံခြင်းအတွက် ကျေးဇူးတင်ပါသည်။",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, receiptText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Voucher Receipt")
                    context.startActivity(shareIntent)
                }
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Voucher မျှဝေမည်", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ပိတ်မည်")
            }
        }
    )
}

@Composable
fun VoucherRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 12.sp, color = Color(0xFF0F1A2E), fontWeight = FontWeight.Bold)
    }
}
