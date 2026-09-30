package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.export.TextFileExporter
import com.example.ui.dialogs.TermuxGitDialog
import com.example.ui.dialogs.TextEditorDialog
import com.example.ui.screens.ExtractorScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.OcrViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: OcrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: OcrViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingSaveAsContent by remember { mutableStateOf<String?>(null) }

    // File Pickers
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.handlePdfPicked(it) }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.handleImagePicked(it) }
    }

    val createDocLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            viewModel.savePaperToUri(it, pendingSaveAsContent)
            pendingSaveAsContent = null
        }
    }

    // Handle back button when not on main extractor tab
    BackHandler(enabled = uiState.activeTab != 0) {
        viewModel.setActiveTab(0)
    }

    // Termux & Git sync dialog
    if (uiState.showTermuxDialog) {
        TermuxGitDialog(
            onDismiss = { viewModel.toggleTermuxDialog(false) }
        )
    }

    // Text Editor Dialog
    if (uiState.showTextEditor && uiState.currentPaper != null) {
        TextEditorDialog(
            paper = uiState.currentPaper!!,
            initialText = viewModel.getActivePaperText(),
            onDismiss = { viewModel.closeTextEditor() },
            onSaveChanges = { newText ->
                viewModel.updateCustomEditedText(newText)
            },
            onSaveToStorage = { newText ->
                viewModel.saveCustomTextToStorage(newText)
            },
            onSaveAs = { title, content ->
                pendingSaveAsContent = content
                val fileName = TextFileExporter.getSanitizedFileName(uiState.currentPaper!!, ".txt")
                createDocLauncher.launch(fileName)
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == 0) Icons.Filled.DocumentScanner else Icons.Outlined.DocumentScanner,
                            contentDescription = "Extractor"
                        )
                    },
                    label = { Text("Extractor") },
                    modifier = Modifier.testTag("nav_extractor_tab")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == 1) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History") },
                    modifier = Modifier.testTag("nav_history_tab")
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.setActiveTab(2) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.activeTab == 2) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_settings_tab")
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.activeTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition",
            modifier = Modifier.padding(innerPadding)
        ) { tabIndex ->
            when (tabIndex) {
                0 -> ExtractorScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onPickPdf = {
                        pdfPickerLauncher.launch(arrayOf("application/pdf"))
                    },
                    onPickImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onOpenTextEditor = {
                        viewModel.openTextEditor()
                    },
                    onSaveAsLocation = { defaultName ->
                        pendingSaveAsContent = viewModel.getActivePaperText()
                        createDocLauncher.launch(defaultName)
                    },
                    onOpenTermuxDialog = {
                        viewModel.toggleTermuxDialog(true)
                    },
                    onOpenSettings = {
                        viewModel.setActiveTab(2)
                    }
                )

                1 -> HistoryScreen(viewModel = viewModel)

                2 -> SettingsScreen(
                    uiState = uiState,
                    viewModel = viewModel,
                    onOpenTermuxDialog = {
                        viewModel.toggleTermuxDialog(true)
                    }
                )
            }
        }
    }
}
