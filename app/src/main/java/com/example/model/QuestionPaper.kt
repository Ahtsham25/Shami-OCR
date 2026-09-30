package com.example.model

import java.util.UUID

enum class QuestionType {
    MCQ,
    SHORT_QUESTION,
    LONG_QUESTION
}

data class McqOption(
    val key: String, // "A", "B", "C", "D" or "الف", "ب", "ج", "د"
    val text: String
)

data class QuestionItem(
    val id: String = UUID.randomUUID().toString(),
    val number: Int,
    val type: QuestionType,
    val statement: String,
    val options: List<McqOption> = emptyList(),
    val correctAnswer: String? = null,
    val marks: Int? = null
)

data class ExamPaper(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val academyName: String = "شامی اکیڈمی (Shami Academy)",
    val subject: String = "",
    val gradeClass: String = "",
    val totalMarks: String = "",
    val timeAllowed: String = "",
    val language: String = "Urdu",
    val rawExtractedText: String = "",
    val questions: List<QuestionItem> = emptyList(),
    val sourceFileName: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val mcqCount: Int get() = questions.count { it.type == QuestionType.MCQ }
    val shortQuestionCount: Int get() = questions.count { it.type == QuestionType.SHORT_QUESTION }
    val longQuestionCount: Int get() = questions.count { it.type == QuestionType.LONG_QUESTION }
    val totalQuestionCount: Int get() = questions.size
}
