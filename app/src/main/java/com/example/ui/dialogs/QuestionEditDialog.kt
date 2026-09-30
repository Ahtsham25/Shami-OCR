package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.McqOption
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.example.ui.theme.AcademyBluePrimary

@Composable
fun QuestionEditDialog(
    question: QuestionItem,
    onDismiss: () -> Unit,
    onSave: (QuestionItem) -> Unit
) {
    var statement by remember { mutableStateOf(question.statement) }
    var selectedType by remember { mutableStateOf(question.type) }
    var marksStr by remember { mutableStateOf(question.marks?.toString() ?: "") }
    var optionA by remember { mutableStateOf(question.options.getOrNull(0)?.text ?: "") }
    var optionB by remember { mutableStateOf(question.options.getOrNull(1)?.text ?: "") }
    var optionC by remember { mutableStateOf(question.options.getOrNull(2)?.text ?: "") }
    var optionD by remember { mutableStateOf(question.options.getOrNull(3)?.text ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Edit Question #${question.number}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == QuestionType.MCQ,
                        onClick = { selectedType = QuestionType.MCQ },
                        label = { Text("MCQ") }
                    )
                    FilterChip(
                        selected = selectedType == QuestionType.SHORT_QUESTION,
                        onClick = { selectedType = QuestionType.SHORT_QUESTION },
                        label = { Text("Short Question") }
                    )
                    FilterChip(
                        selected = selectedType == QuestionType.LONG_QUESTION,
                        onClick = { selectedType = QuestionType.LONG_QUESTION },
                        label = { Text("Long Question") }
                    )
                }

                OutlinedTextField(
                    value = statement,
                    onValueChange = { statement = it },
                    label = { Text("Question Statement") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_q_statement_input"),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = marksStr,
                    onValueChange = { marksStr = it },
                    label = { Text("Marks") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (selectedType == QuestionType.MCQ) {
                    Text(
                        text = "Multiple Choice Options:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = optionA,
                        onValueChange = { optionA = it },
                        label = { Text("Option A") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = optionB,
                        onValueChange = { optionB = it },
                        label = { Text("Option B") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = optionC,
                        onValueChange = { optionC = it },
                        label = { Text("Option C") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = optionD,
                        onValueChange = { optionD = it },
                        label = { Text("Option D") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val opts = if (selectedType == QuestionType.MCQ) {
                        listOf(
                            McqOption("A", optionA),
                            McqOption("B", optionB),
                            McqOption("C", optionC),
                            McqOption("D", optionD)
                        ).filter { it.text.isNotBlank() }
                    } else emptyList()

                    val marksInt = marksStr.toIntOrNull()
                    val updated = question.copy(
                        statement = statement,
                        type = selectedType,
                        marks = marksInt,
                        options = opts
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AcademyBluePrimary),
                modifier = Modifier.testTag("save_edit_q_btn")
            ) {
                Text("Save Question")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
