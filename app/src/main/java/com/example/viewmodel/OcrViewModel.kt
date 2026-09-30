package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AcademyDatabase
import com.example.data.local.PaperConverters
import com.example.data.local.PaperEntity
import com.example.data.local.toEntity
import com.example.export.TextFileExporter
import com.example.model.ExamPaper
import com.example.model.QuestionItem
import com.example.model.QuestionType
import com.example.ocr.DocumentProcessor
import com.example.ocr.GeminiOcrEngine
import com.example.ocr.SamplePapers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class OcrUiState(
    val selectedDoc: DocumentProcessor.ProcessedDocument? = null,
    val currentPaper: ExamPaper? = null,
    val isProcessing: Boolean = false,
    val statusMessage: String = "",
    val error: String? = null,
    val selectedCategoryFilter: QuestionType? = null, // null = All
    val activeTab: Int = 0, // 0 = Extractor, 1 = History, 2 = Settings
    val customApiKey: String = "",
    val previewingRawText: Boolean = false,
    val showEditDialog: Boolean = false,
    val showTermuxDialog: Boolean = false,
    val showTextEditor: Boolean = false,
    val editingQuestion: QuestionItem? = null,
    val savedFilePath: String? = null,
    val progressPercent: Float = 0f,
    val elapsedSeconds: Int = 0,
    val currentStepIndex: Int = 0,
    val customEditedText: String? = null
)

class OcrViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AcademyDatabase.getDatabase(application)
    private val paperDao = db.paperDao()
    private val ocrEngine = GeminiOcrEngine()

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    val savedPapers: StateFlow<List<PaperEntity>> = paperDao.getAllPapers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load default sample initially so screen is never blank or confusing
        loadSamplePaper(SamplePapers.urduBiologyPaper)
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun setCategoryFilter(type: QuestionType?) {
        _uiState.value = _uiState.value.copy(selectedCategoryFilter = type)
    }

    fun setCustomApiKey(key: String) {
        _uiState.value = _uiState.value.copy(customApiKey = key)
        ocrEngine.setCustomApiKey(key)
    }

    fun toggleRawTextPreview(show: Boolean) {
        _uiState.value = _uiState.value.copy(previewingRawText = show)
    }

    fun loadSamplePaper(paper: ExamPaper) {
        _uiState.value = _uiState.value.copy(
            selectedDoc = null,
            currentPaper = paper,
            customEditedText = null,
            isProcessing = false,
            statusMessage = "Sample paper loaded: ${paper.title}",
            error = null
        )
    }

    fun handlePdfPicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                statusMessage = "Rendering PDF pages...",
                error = null
            )
            try {
                val doc = DocumentProcessor.processPdfUri(getApplication(), uri)
                _uiState.value = _uiState.value.copy(
                    selectedDoc = doc,
                    isProcessing = false,
                    statusMessage = "PDF loaded successfully (${doc.pageCount} pages)"
                )
                startOcrExtraction()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "PDF processing error: ${e.message}"
                )
            }
        }
    }

    fun handleImagePicked(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                statusMessage = "Loading image...",
                error = null
            )
            try {
                val doc = DocumentProcessor.processImageUri(getApplication(), uri)
                _uiState.value = _uiState.value.copy(
                    selectedDoc = doc,
                    isProcessing = false,
                    statusMessage = "Image loaded successfully"
                )
                startOcrExtraction()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Error loading image: ${e.message}"
                )
            }
        }
    }

    private var extractionJob: Job? = null
    private var tickerJob: Job? = null

    fun cancelExtraction() {
        extractionJob?.cancel()
        tickerJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isProcessing = false,
            statusMessage = "Extraction cancelled",
            progressPercent = 0f,
            elapsedSeconds = 0
        )
    }

    fun startOcrExtraction() {
        val doc = _uiState.value.selectedDoc ?: return
        if (doc.pageBitmaps.isEmpty()) return

        extractionJob?.cancel()
        tickerJob?.cancel()

        _uiState.value = _uiState.value.copy(
            isProcessing = true,
            statusMessage = "Preparing document pages...",
            progressPercent = 0.15f,
            elapsedSeconds = 0,
            currentStepIndex = 0,
            error = null
        )

        tickerJob = viewModelScope.launch {
            var seconds = 0
            while (isActive) {
                delay(1000)
                seconds++
                _uiState.value = _uiState.value.copy(elapsedSeconds = seconds)
            }
        }

        extractionJob = viewModelScope.launch {
            val result = ocrEngine.extractQuestionsFromImages(
                bitmaps = doc.pageBitmaps,
                fileName = doc.fileName,
                onProgress = { stepDesc, progress ->
                    val stepIdx = when {
                        progress < 0.35f -> 0
                        progress < 0.65f -> 1
                        progress < 0.85f -> 2
                        else -> 3
                    }
                    _uiState.value = _uiState.value.copy(
                        statusMessage = stepDesc,
                        progressPercent = progress,
                        currentStepIndex = stepIdx
                    )
                }
            )

            tickerJob?.cancel()

            result.onSuccess { extractedPaper ->
                _uiState.value = _uiState.value.copy(
                    currentPaper = extractedPaper,
                    customEditedText = null,
                    isProcessing = false,
                    progressPercent = 1.0f,
                    currentStepIndex = 3,
                    statusMessage = "Success! Extracted ${extractedPaper.questions.size} questions."
                )
                savePaperToDb(extractedPaper)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    progressPercent = 0f,
                    error = err.message ?: "OCR extraction failed"
                )
            }
        }
    }

    fun saveCurrentPaper() {
        val paper = _uiState.value.currentPaper ?: return
        viewModelScope.launch {
            savePaperToDb(paper)
            Toast.makeText(getApplication(), "Exam paper saved to history!", Toast.LENGTH_SHORT).show()
        }
    }

    private suspend fun savePaperToDb(paper: ExamPaper) {
        val questionsJson = PaperConverters.serializeQuestions(paper.questions)
        val entity = paper.toEntity(questionsJson)
        paperDao.insertPaper(entity)
    }

    fun loadPaperFromEntity(entity: PaperEntity) {
        val questions = PaperConverters.deserializeQuestions(entity.questionsJson)
        val paper = ExamPaper(
            id = entity.id,
            title = entity.title,
            academyName = entity.academyName,
            subject = entity.subject,
            gradeClass = entity.gradeClass,
            totalMarks = entity.totalMarks,
            timeAllowed = entity.timeAllowed,
            language = entity.language,
            rawExtractedText = entity.rawExtractedText,
            questions = questions,
            sourceFileName = entity.sourceFileName,
            timestamp = entity.timestamp
        )
        _uiState.value = _uiState.value.copy(
            currentPaper = paper,
            customEditedText = null,
            activeTab = 0,
            statusMessage = "Loaded: ${paper.title}"
        )
    }

    fun deletePaper(id: String) {
        viewModelScope.launch {
            paperDao.deletePaperById(id)
            Toast.makeText(getApplication(), "Exam paper deleted", Toast.LENGTH_SHORT).show()
        }
    }

    fun startEditQuestion(question: QuestionItem) {
        _uiState.value = _uiState.value.copy(
            editingQuestion = question,
            showEditDialog = true
        )
    }

    fun dismissEditDialog() {
        _uiState.value = _uiState.value.copy(
            editingQuestion = null,
            showEditDialog = false
        )
    }

    fun saveEditedQuestion(updated: QuestionItem) {
        val current = _uiState.value.currentPaper ?: return
        val updatedQuestions = current.questions.map {
            if (it.id == updated.id) updated else it
        }
        val newPaper = current.copy(questions = updatedQuestions)
        _uiState.value = _uiState.value.copy(
            currentPaper = newPaper,
            customEditedText = null,
            showEditDialog = false,
            editingQuestion = null
        )
        viewModelScope.launch {
            savePaperToDb(newPaper)
        }
    }

    fun deleteQuestion(questionId: String) {
        val current = _uiState.value.currentPaper ?: return
        val updatedQuestions = current.questions.filter { it.id != questionId }
        val newPaper = current.copy(questions = updatedQuestions)
        _uiState.value = _uiState.value.copy(
            currentPaper = newPaper,
            customEditedText = null
        )
        viewModelScope.launch {
            savePaperToDb(newPaper)
        }
    }

    fun openTextEditor() {
        val text = getActivePaperText()
        _uiState.value = _uiState.value.copy(
            showTextEditor = true,
            customEditedText = text
        )
    }

    fun closeTextEditor() {
        _uiState.value = _uiState.value.copy(showTextEditor = false)
    }

    fun updateCustomEditedText(newText: String) {
        val current = _uiState.value.currentPaper
        _uiState.value = _uiState.value.copy(
            customEditedText = newText,
            currentPaper = current?.copy(rawExtractedText = newText)
        )
        Toast.makeText(getApplication(), "Changes applied to exam paper!", Toast.LENGTH_SHORT).show()
    }

    fun getActivePaperText(): String {
        val state = _uiState.value
        if (!state.customEditedText.isNullOrBlank()) return state.customEditedText
        val paper = state.currentPaper ?: return ""
        return TextFileExporter.formatExamPaperAsText(paper)
    }

    fun saveCustomTextToStorage(text: String) {
        val paper = _uiState.value.currentPaper ?: return
        val result = TextFileExporter.saveCustomTextToLocalStorage(getApplication(), paper.title, text)
        result.onSuccess { path ->
            _uiState.value = _uiState.value.copy(
                savedFilePath = path,
                customEditedText = text,
                statusMessage = "File saved: $path"
            )
            Toast.makeText(getApplication(), "File saved to $path\nAccessible by Termux!", Toast.LENGTH_LONG).show()
        }.onFailure { err ->
            _uiState.value = _uiState.value.copy(
                error = "Error saving file: ${err.message}"
            )
        }
    }

    fun savePaperToLocalStorage() {
        val paper = _uiState.value.currentPaper ?: return
        val text = getActivePaperText()
        saveCustomTextToStorage(text)
    }

    fun savePaperToUri(uri: Uri, content: String? = null) {
        val textToSave = content ?: getActivePaperText()
        val success = TextFileExporter.writeTextToUri(getApplication(), uri, textToSave)
        if (success) {
            Toast.makeText(getApplication(), "File saved to selected location!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getApplication(), "Failed to save file", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleTermuxDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTermuxDialog = show)
    }

    fun clearSavedFilePath() {
        _uiState.value = _uiState.value.copy(savedFilePath = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
