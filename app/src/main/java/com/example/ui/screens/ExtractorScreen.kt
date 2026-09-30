package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.export.TextFileExporter
import com.example.model.ExamPaper
import com.example.model.QuestionType
import com.example.ocr.SamplePapers
import com.example.ui.components.AcademyHeader
import com.example.ui.components.ExtractionProgressCard
import com.example.ui.components.QuestionCard
import com.example.ui.dialogs.QuestionEditDialog
import com.example.ui.dialogs.RawTextDialog
import com.example.ui.theme.AcademyBluePrimary
import com.example.ui.theme.AcademyGold
import com.example.ui.theme.AcademyTeal
import com.example.viewmodel.OcrUiState
import com.example.viewmodel.OcrViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtractorScreen(
    uiState: OcrUiState,
    viewModel: OcrViewModel,
    onPickPdf: () -> Unit,
    onPickImage: () -> Unit,
    onOpenTextEditor: () -> Unit = {},
    onSaveAsLocation: (String) -> Unit = {},
    onOpenTermuxDialog: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSampleMenu by remember { mutableStateOf(false) }

    // Dialogs
    if (uiState.previewingRawText && uiState.currentPaper != null) {
        RawTextDialog(
            paper = uiState.currentPaper,
            customText = viewModel.getActivePaperText(),
            onDismiss = { viewModel.toggleRawTextPreview(false) }
        )
    }

    if (uiState.showEditDialog && uiState.editingQuestion != null) {
        QuestionEditDialog(
            question = uiState.editingQuestion,
            onDismiss = { viewModel.dismissEditDialog() },
            onSave = { updated -> viewModel.saveEditedQuestion(updated) }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("extractor_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Academy Header Card
        item {
            AcademyHeader(onOpenSettings = onOpenSettings)
        }

        // Hero Banner & Document Picker Card
        item {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Document selector buttons
                    Text(
                        text = "Select Document for Extraction:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPickPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyBluePrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pick_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF File", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onPickImage,
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pick_image_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paper Image", fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sample paper selector
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showSampleMenu = true },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("sample_paper_selector_btn")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = AcademyGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Try Sample Papers")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = showSampleMenu,
                            onDismissRequest = { showSampleMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("9th Biology Paper (Urdu Medium)") },
                                onClick = {
                                    showSampleMenu = false
                                    viewModel.loadSamplePaper(SamplePapers.urduBiologyPaper)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("10th Physics Exam (English Medium)") },
                                onClick = {
                                    showSampleMenu = false
                                    viewModel.loadSamplePaper(SamplePapers.englishPhysicsPaper)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("10th Pakistan Studies (Urdu Medium)") },
                                onClick = {
                                    showSampleMenu = false
                                    viewModel.loadSamplePaper(SamplePapers.urduPakStudiesPaper)
                                }
                            )
                        }
                    }

                    // Rendered Preview thumbnail if available
                    if (uiState.selectedDoc?.previewBitmap != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                bitmap = uiState.selectedDoc.previewBitmap.asImageBitmap(),
                                contentDescription = "PDF Page Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.selectedDoc.fileName,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1
                                )
                                Text(
                                    text = "Pages: ${uiState.selectedDoc.pageCount} | Ready for Analysis",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = { viewModel.startOcrExtraction() },
                                colors = ButtonDefaults.buttonColors(containerColor = AcademyGold),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !uiState.isProcessing,
                                modifier = Modifier.testTag("extract_now_btn")
                            ) {
                                Text("Scan Document", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                    }
                }
            }
        }

        // Live Visual AI Extraction Progress Indicator
        if (uiState.isProcessing) {
            item {
                ExtractionProgressCard(
                    statusMessage = uiState.statusMessage,
                    progressPercent = uiState.progressPercent,
                    elapsedSeconds = uiState.elapsedSeconds,
                    currentStepIndex = uiState.currentStepIndex,
                    onCancel = { viewModel.cancelExtraction() }
                )
            }
        }

        // Error Banner
        if (uiState.error != null) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Notice:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = uiState.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }
        }

        // Paper Metadata Summary Card
        val paper = uiState.currentPaper
        if (paper != null) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paper_summary_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = paper.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.customEditedText != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = AcademyGold.copy(alpha = 0.2f),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Text(
                                            text = "Edited",
                                            color = Color(0xFFB45309),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = onOpenTextEditor,
                                    modifier = Modifier.testTag("open_text_editor_header_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EditNote,
                                        contentDescription = "Review & Edit Text",
                                        tint = AcademyTeal
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.saveCurrentPaper() },
                                    modifier = Modifier.testTag("save_to_history_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = "Save to History",
                                        tint = AcademyBluePrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Badges for Subject, Class, Marks, Questions
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (paper.subject.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AcademyBluePrimary.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Subject: ${paper.subject}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = AcademyBluePrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (paper.gradeClass.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AcademyTeal.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "Class: ${paper.gradeClass}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = AcademyTeal,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            if (paper.totalMarks.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AcademyGold.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "Marks: ${paper.totalMarks}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = "Questions: ${paper.totalQuestionCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("All Questions (${paper.totalQuestionCount})") },
                            leadingIcon = if (uiState.selectedCategoryFilter == null) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.testTag("filter_all_chip")
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == QuestionType.MCQ,
                            onClick = { viewModel.setCategoryFilter(QuestionType.MCQ) },
                            label = { Text("MCQs (${paper.mcqCount})") },
                            leadingIcon = if (uiState.selectedCategoryFilter == QuestionType.MCQ) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.testTag("filter_mcq_chip")
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == QuestionType.SHORT_QUESTION,
                            onClick = { viewModel.setCategoryFilter(QuestionType.SHORT_QUESTION) },
                            label = { Text("Short Questions (${paper.shortQuestionCount})") },
                            leadingIcon = if (uiState.selectedCategoryFilter == QuestionType.SHORT_QUESTION) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.testTag("filter_short_chip")
                        )
                    }

                    item {
                        FilterChip(
                            selected = uiState.selectedCategoryFilter == QuestionType.LONG_QUESTION,
                            onClick = { viewModel.setCategoryFilter(QuestionType.LONG_QUESTION) },
                            label = { Text("Long Questions (${paper.longQuestionCount})") },
                            leadingIcon = if (uiState.selectedCategoryFilter == QuestionType.LONG_QUESTION) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            modifier = Modifier.testTag("filter_long_chip")
                        )
                    }
                }
            }

            // Export & Storage Actions Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AcademyBluePrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Export & Storage Options:",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )

                        // Confirmation banner if file was just saved
                        if (uiState.savedFilePath != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AcademyTeal.copy(alpha = 0.25f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "File saved successfully!",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = uiState.savedFilePath ?: "",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.clearSavedFilePath() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White)
                                    }
                                }
                            }
                        }

                        // Prominent Action 1: Review & Edit Extracted Text
                        Button(
                            onClick = onOpenTextEditor,
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("open_text_editor_btn")
                        ) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Review & Edit Extracted Text", fontWeight = FontWeight.Bold)
                        }

                        // Prominent Action 2: Save to Local Storage
                        Button(
                            onClick = { viewModel.savePaperToLocalStorage() },
                            colors = ButtonDefaults.buttonColors(containerColor = AcademyGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("save_to_local_storage_btn")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save to Storage (/Documents/ShamiAcademy)",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Secondary Row: Save As (Custom Folder) & Share File
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSaveAsLocation(TextFileExporter.getSanitizedFileName(paper, ".txt"))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_as_location_btn")
                            ) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save As...", color = Color.White, fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val text = viewModel.getActivePaperText()
                                    if (uiState.customEditedText != null) {
                                        TextFileExporter.shareCustomTextFile(context, paper.title, text)
                                    } else {
                                        TextFileExporter.shareTextFile(context, paper)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.22f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_txt_file_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share File", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                            }
                        }

                        // Third Row: Copy Text & Preview
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val formatted = viewModel.getActivePaperText()
                                    TextFileExporter.copyToClipboard(context, formatted, "Shami Academy Paper")
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("copy_all_text_btn")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Text", fontSize = 12.sp)
                            }

                            IconButton(
                                onClick = { viewModel.toggleRawTextPreview(true) },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .testTag("preview_raw_btn")
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = "Preview Text", tint = Color.White)
                            }
                        }

                        // Termux & GitHub connection banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.8f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenTermuxDialog() }
                                .testTag("open_termux_dialog_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = "Termux",
                                        tint = Color(0xFFA3BE8C),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Auto-Push to GitHub via Termux",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Termux & GitHub Sync Guide & Commands",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Text(
                                    text = "View →",
                                    color = Color(0xFF38BDF8),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Question Cards
            val displayedQuestions = if (uiState.selectedCategoryFilter == null) {
                paper.questions
            } else {
                paper.questions.filter { it.type == uiState.selectedCategoryFilter }
            }

            if (displayedQuestions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No questions found in this category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(displayedQuestions, key = { it.id }) { question ->
                    QuestionCard(
                        question = question,
                        onEdit = { viewModel.startEditQuestion(question) },
                        onDelete = { viewModel.deleteQuestion(question.id) }
                    )
                }
            }
        }
    }
}
