package com.example.ocr

import android.graphics.Bitmap
import android.util.Log
import com.example.BuildConfig
import com.example.model.ExamPaper
import com.example.model.McqOption
import com.example.model.QuestionItem
import com.example.model.QuestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiOcrEngine(
    private var customApiKey: String? = null
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun setCustomApiKey(key: String?) {
        customApiKey = key?.trim()
    }

    fun getEffectiveApiKey(): String {
        val custom = customApiKey
        if (!custom.isNullOrBlank()) return custom
        return BuildConfig.GEMINI_API_KEY
    }

    suspend fun extractQuestionsFromImages(
        bitmaps: List<Bitmap>,
        fileName: String,
        onProgress: ((stepDescription: String, progress: Float) -> Unit)? = null
    ): Result<ExamPaper> = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please enter your API key in Settings or AI Studio Secrets.")
            )
        }

        try {
            val systemInstruction = """
                You are an expert OCR & examination paper analyzer for 'Shami Academy (شامی اکیڈمی)'.
                Your task is to analyze examination papers, worksheets, or test question sheets in Urdu, English, or bilingual format.
                Carefully read the whole document, transcribe all text accurately, and categorize all questions into exactly three categories:
                1. 'MCQ' (کثیر الانتخابی سوالات) with statement, options (key 'A','B','C','D' or 'الف','ب','ج','د'), correct option if marked.
                2. 'SHORT_QUESTION' (مختصر سوالات) - e.g. Section B / حصہ اول مختصر سوالات.
                3. 'LONG_QUESTION' (تفصیلی / انشائیہ سوالات) - e.g. Section C / حصہ دوم انشائیہ سوالات.

                Return a clean JSON object without Markdown backticks:
                {
                  "title": "Title of the test paper",
                  "academyName": "شامی اکیڈمی (Shami Academy)",
                  "subject": "Subject name (e.g. Physics, بائیولوجی, Mathematics)",
                  "gradeClass": "Class/Grade (e.g. 9th, 10th, 1st Year)",
                  "totalMarks": "Total Marks if mentioned",
                  "timeAllowed": "Time allowed if mentioned",
                  "language": "Urdu or English or Bilingual",
                  "rawExtractedText": "Full plain transcription of the document text",
                  "questions": [
                    {
                      "number": 1,
                      "type": "MCQ",
                      "statement": "Question statement here",
                      "options": [
                        {"key": "A", "text": "Option 1"},
                        {"key": "B", "text": "Option 2"},
                        {"key": "C", "text": "Option 3"},
                        {"key": "D", "text": "Option 4"}
                      ],
                      "correctAnswer": "A",
                      "marks": 1
                    },
                    {
                      "number": 1,
                      "type": "SHORT_QUESTION",
                      "statement": "Short question statement",
                      "marks": 2
                    },
                    {
                      "number": 1,
                      "type": "LONG_QUESTION",
                      "statement": "Long / descriptive question statement",
                      "marks": 8
                    }
                  ]
                }
            """.trimIndent()

            onProgress?.invoke("صفحات کا ڈیجیٹل ڈیٹا تیار کیا جا رہا ہے…", 0.25f)
            val partsArray = JSONArray()

            // Text instruction part
            val textPart = JSONObject()
            textPart.put(
                "text",
                "Extract and categorize all MCQs, short questions, and long questions from this Shami Academy examination paper."
            )
            partsArray.put(textPart)

            // Add all rendered page bitmaps as inline data
            for (bitmap in bitmaps) {
                val imageBase64 = DocumentProcessor.bitmapToBase64(bitmap)
                val imagePart = JSONObject()
                val inlineData = JSONObject()
                inlineData.put("mimeType", "image/jpeg")
                inlineData.put("data", imageBase64)
                imagePart.put("inlineData", inlineData)
                partsArray.put(imagePart)
            }

            val contentObject = JSONObject()
            contentObject.put("parts", partsArray)

            val contentsArray = JSONArray()
            contentsArray.put(contentObject)

            val rootRequest = JSONObject()
            rootRequest.put("contents", contentsArray)

            val systemInstructionObj = JSONObject()
            val sysParts = JSONArray()
            val sysText = JSONObject()
            sysText.put("text", systemInstruction)
            sysParts.put(sysText)
            systemInstructionObj.put("parts", sysParts)
            rootRequest.put("systemInstruction", systemInstructionObj)

            val generationConfig = JSONObject()
            generationConfig.put("responseMimeType", "application/json")
            generationConfig.put("temperature", 0.1)
            rootRequest.put("generationConfig", generationConfig)

            onProgress?.invoke("Gemini 3.5 Flash اے آئی او سی آر سے رابطہ قائم ہو رہا ہے…", 0.50f)
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootRequest.toString().toRequestBody(mediaType)

            val httpRequest = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .build()

            onProgress?.invoke("اے آئی ماڈل اردو نستعلیق اور انگلش متن کا گہرائی سے تجزیہ کر رہا ہے…", 0.70f)
            val response = client.newCall(httpRequest).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e("GeminiOcrEngine", "Error response: $responseBody")
                return@withContext Result.failure(
                    Exception("Gemini API error (HTTP ${response.code}): $responseBody")
                )
            }

            onProgress?.invoke("ایم سی کیوز، مختصر اور لانگ سوالات کی درجہ بندی ہو رہی ہے…", 0.90f)
            val parsedPaper = parseGeminiResponse(responseBody, fileName)
            onProgress?.invoke("کامیابی! سوالات نکال لیے گئے۔", 1.0f)
            Result.success(parsedPaper)
        } catch (e: Exception) {
            Log.e("GeminiOcrEngine", "Extraction exception", e)
            Result.failure(e)
        }
    }

    private fun parseGeminiResponse(rawResponse: String, sourceFileName: String): ExamPaper {
        val root = JSONObject(rawResponse)
        val candidates = root.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val firstPart = parts?.optJSONObject(0)
        var text = firstPart?.optString("text", "") ?: ""

        // Strip possible markdown ticks
        text = text.trim()
        if (text.startsWith("```json")) {
            text = text.removePrefix("```json")
        } else if (text.startsWith("```")) {
            text = text.removePrefix("```")
        }
        if (text.endsWith("```")) {
            text = text.removeSuffix("```")
        }
        text = text.trim()

        val json = JSONObject(text)
        val title = json.optString("title", "امتحانی پرچہ - شامی اکیڈمی")
        val academyName = json.optString("academyName", "شامی اکیڈمی (Shami Academy)")
        val subject = json.optString("subject", "General")
        val gradeClass = json.optString("gradeClass", "")
        val totalMarks = json.optString("totalMarks", "")
        val timeAllowed = json.optString("timeAllowed", "")
        val language = json.optString("language", "Urdu")
        val rawExtractedText = json.optString("rawExtractedText", "")

        val questionsList = mutableListOf<QuestionItem>()
        val questionsArray = json.optJSONArray("questions")
        if (questionsArray != null) {
            for (i in 0 until questionsArray.length()) {
                val qObj = questionsArray.getJSONObject(i)
                val number = qObj.optInt("number", i + 1)
                val typeStr = qObj.optString("type", "SHORT_QUESTION")
                val type = when (typeStr.uppercase()) {
                    "MCQ" -> QuestionType.MCQ
                    "LONG_QUESTION", "LONG" -> QuestionType.LONG_QUESTION
                    else -> QuestionType.SHORT_QUESTION
                }
                val statement = qObj.optString("statement", "")
                val marks = if (qObj.has("marks")) qObj.getInt("marks") else null
                val correctAnswer = if (qObj.has("correctAnswer")) qObj.getString("correctAnswer") else null

                val options = mutableListOf<McqOption>()
                val optArr = qObj.optJSONArray("options")
                if (optArr != null) {
                    for (j in 0 until optArr.length()) {
                        val optObj = optArr.getJSONObject(j)
                        options.add(
                            McqOption(
                                key = optObj.optString("key", ('A' + j).toString()),
                                text = optObj.optString("text", "")
                            )
                        )
                    }
                }

                questionsList.add(
                    QuestionItem(
                        number = number,
                        type = type,
                        statement = statement,
                        options = options,
                        correctAnswer = correctAnswer,
                        marks = marks
                    )
                )
            }
        }

        return ExamPaper(
            title = title,
            academyName = academyName,
            subject = subject,
            gradeClass = gradeClass,
            totalMarks = totalMarks,
            timeAllowed = timeAllowed,
            language = language,
            rawExtractedText = rawExtractedText,
            questions = questionsList,
            sourceFileName = sourceFileName
        )
    }
}
