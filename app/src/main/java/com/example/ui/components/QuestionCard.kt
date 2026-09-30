package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.export.TextFileExporter
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.example.ui.theme.AcademyBluePrimary
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyTeal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionCard(
    question: QuestionItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isUrdu = question.statement.any { it in '\u0600'..'\u06FF' }

    val (badgeBg, badgeFg, typeLabel) = when (question.type) {
        QuestionType.MCQ -> Triple(
            AcademyTeal.copy(alpha = 0.12f),
            AcademyTeal,
            "MCQ"
        )
        QuestionType.SHORT_QUESTION -> Triple(
            AcademyBluePrimary.copy(alpha = 0.12f),
            AcademyBluePrimary,
            "Short Question"
        )
        QuestionType.LONG_QUESTION -> Triple(
            AcademyGold.copy(alpha = 0.15f),
            Color(0xFFB45309),
            "Long Question"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("question_card_${question.number}_${question.type}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Number + Badge + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(badgeFg),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${question.number}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = typeLabel,
                            color = badgeFg,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (question.marks != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "${question.marks} Marks",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = {
                            val copyText = buildString {
                                appendLine("${question.number}. ${question.statement}")
                                question.options.forEach { opt ->
                                    appendLine("   (${opt.key}) ${opt.text}")
                                }
                            }
                            TextFileExporter.copyToClipboard(context, copyText, "Question")
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("copy_q_btn_${question.number}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Question",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("edit_q_btn_${question.number}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Question",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_q_btn_${question.number}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Question",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Statement text
            Text(
                text = question.statement,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = if (isUrdu) TextAlign.Right else TextAlign.Left,
                modifier = Modifier.fillMaxWidth()
            )

            // MCQs Options grid
            if (question.type == QuestionType.MCQ && question.options.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    question.options.forEach { opt ->
                        val isCorrect = question.correctAnswer != null &&
                                (question.correctAnswer.equals(opt.key, ignoreCase = true) ||
                                        opt.text.contains(question.correctAnswer, ignoreCase = true))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isCorrect) AcademyTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isCorrect) androidx.compose.foundation.BorderStroke(1.dp, AcademyTeal) else null,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(if (isCorrect) AcademyTeal else MaterialTheme.colorScheme.outlineVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = opt.key,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isCorrect) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = opt.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isCorrect) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isCorrect) AcademyTeal else MaterialTheme.colorScheme.onSurface
                                )

                                if (isCorrect) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Correct Answer",
                                        tint = AcademyTeal,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
