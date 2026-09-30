package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.ExamPaper
import com.example.model.QuestionItem

@Entity(tableName = "saved_papers")
data class PaperEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val academyName: String,
    val subject: String,
    val gradeClass: String,
    val totalMarks: String,
    val timeAllowed: String,
    val language: String,
    val rawExtractedText: String,
    val questionsJson: String,
    val sourceFileName: String,
    val timestamp: Long
)

fun ExamPaper.toEntity(questionsJson: String): PaperEntity {
    return PaperEntity(
        id = id,
        title = title,
        academyName = academyName,
        subject = subject,
        gradeClass = gradeClass,
        totalMarks = totalMarks,
        timeAllowed = timeAllowed,
        language = language,
        rawExtractedText = rawExtractedText,
        questionsJson = questionsJson,
        sourceFileName = sourceFileName,
        timestamp = timestamp
    )
}
