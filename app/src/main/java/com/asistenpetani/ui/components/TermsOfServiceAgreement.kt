package com.asistenpetani.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.asistenpetani.ui.theme.AspekGreen
import com.asistenpetani.ui.theme.MidnightBlue
import com.asistenpetani.ui.theme.OrangeRed

@Composable
fun TermsOfServiceAgreement(
    isAccepted: Boolean,
    onAcceptanceChanged: (Boolean) -> Unit,
    onReadPolicyClick: () -> Unit,
    hasError: Boolean = false,
    modifier: Modifier = Modifier
) {
    val errorColor = OrangeRed
    val primaryColor = AspekGreen

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tos_agreement_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasError) Color(0xFFFEF2F2) else Color(0xFFF8FAFC)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (hasError) errorColor.copy(alpha = 0.6f) else Color(0xFFE2E8F0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = isAccepted,
                    onCheckedChange = { onAcceptanceChanged(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MidnightBlue,
                        uncheckedColor = if (hasError) errorColor else Color(0xFF64748B),
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier
                        .testTag("tos_checkbox")
                        .padding(end = 4.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp)
                ) {
                    val annotatedText = buildAnnotatedString {
                        append("Saya telah membaca, memahami, dan menyetujui ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = MidnightBlue,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Ketentuan Layanan (Terms of Service)")
                        }
                        append(" serta ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Kebijakan Privasi")
                        }
                        append(" Asisten Petani AI.")
                    }

                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = if (hasError) Color(0xFF991B1B) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .testTag("tos_text_clickable")
                            .clickable { onReadPolicyClick() }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onReadPolicyClick() }
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Baca Kebijakan Privasi",
                            tint = primaryColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Baca Kebijakan & Lisensi Lengkap",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            }

            if (hasError) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Error",
                        tint = errorColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Wajib menyetujui Ketentuan Layanan & Kebijakan Privasi untuk melanjutkan.",
                        color = errorColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
