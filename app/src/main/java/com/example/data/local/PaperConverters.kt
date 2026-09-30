package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.McqOption
import com.example.model.QuestionItem
import com.example.model.QuestionType
import org.json.JSONArray
import org.json.JSONObject

class PaperConverters {

    @TypeConverter
    fun fromQuestionsList(questions: List<QuestionItem>): String {
        return serializeQuestions(questions)
    }

    @TypeConverter
    fun toQuestionsList(json: String): List<QuestionItem> {
        return deserializeQuestions(json)
    }

    companion object {
        fun serializeQuestions(questions: List<QuestionItem>): String {
            val jsonArray = JSONArray()
            for (q in questions) {
                val obj = JSONObject()
                obj.put("id", q.id)
                obj.put("number", q.number)
                obj.put("type", q.type.name)
                obj.put("statement", q.statement)
                if (q.correctAnswer != null) obj.put("correctAnswer", q.correctAnswer)
                if (q.marks != null) obj.put("marks", q.marks)

                val optArray = JSONArray()
                for (opt in q.options) {
                    val optObj = JSONObject()
                    optObj.put("key", opt.key)
                    optObj.put("text", opt.text)
                    optArray.put(optObj)
                }
                obj.put("options", optArray)
                jsonArray.put(obj)
            }
            return jsonArray.toString()
        }

        fun deserializeQuestions(json: String): List<QuestionItem> {
            if (json.isBlank()) return emptyList()
            val list = mutableListOf<QuestionItem>()
            try {
                val array = JSONArray(json)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val id = obj.optString("id")
                    val number = obj.optInt("number", i + 1)
                    val typeStr = obj.optString("type", QuestionType.SHORT_QUESTION.name)
                    val type = try {
                        QuestionType.valueOf(typeStr)
                    } catch (e: Exception) {
                        QuestionType.SHORT_QUESTION
                    }
                    val statement = obj.optString("statement", "")
                    val correctAnswer = if (obj.has("correctAnswer")) obj.getString("correctAnswer") else null
                    val marks = if (obj.has("marks")) obj.getInt("marks") else null

                    val options = mutableListOf<McqOption>()
                    val optArray = obj.optJSONArray("options")
                    if (optArray != null) {
                        for (j in 0 until optArray.length()) {
                            val optObj = optArray.getJSONObject(j)
                            options.add(
                                McqOption(
                                    key = optObj.optString("key", ""),
                                    text = optObj.optString("text", "")
                                )
                            )
                        }
                    }

                    list.add(
                        QuestionItem(
                            id = id,
                            number = number,
                            type = type,
                            statement = statement,
                            options = options,
                            correctAnswer = correctAnswer,
                            marks = marks
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return list
        }
    }
}
