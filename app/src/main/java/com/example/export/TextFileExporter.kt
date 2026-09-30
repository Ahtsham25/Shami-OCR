package com.example.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.model.ExamPaper
import com.example.model.QuestionType
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TextFileExporter {

    fun formatExamPaperAsText(paper: ExamPaper): String {
        val sb = StringBuilder()
        val isUrdu = paper.language.equals("Urdu", ignoreCase = true) ||
                paper.title.any { it in '\u0600'..'\u06FF' }

        val border = "=".repeat(60)
        val separator = "-".repeat(60)

        // Header
        sb.appendLine(border)
        sb.appendLine("           ${paper.academyName.ifBlank { "شامی اکیڈمی (SHAMI ACADEMY)" }}")
        sb.appendLine("                 ${paper.title}")
        sb.appendLine(border)

        val metaList = mutableListOf<String>()
        if (paper.subject.isNotBlank()) metaList.add("مضمون / Subject: ${paper.subject}")
        if (paper.gradeClass.isNotBlank()) metaList.add("جماعت / Class: ${paper.gradeClass}")
        if (paper.totalMarks.isNotBlank()) metaList.add("کل نمبر / Marks: ${paper.totalMarks}")
        if (paper.timeAllowed.isNotBlank()) metaList.add("وقت / Time: ${paper.timeAllowed}")

        if (metaList.isNotEmpty()) {
            sb.appendLine(metaList.joinToString("  |  "))
            sb.appendLine(separator)
        }

        // Section A: MCQs
        val mcqs = paper.questions.filter { it.type == QuestionType.MCQ }
        if (mcqs.isNotEmpty()) {
            sb.appendLine()
            if (isUrdu) {
                sb.appendLine("■ حصہ معروضی: کثیر الانتخابی سوالات (MCQs) [تعداد: ${mcqs.size}]")
            } else {
                sb.appendLine("■ SECTION A: MULTIPLE CHOICE QUESTIONS (MCQs) [Total: ${mcqs.size}]")
            }
            sb.appendLine(separator)

            mcqs.forEachIndexed { idx, q ->
                sb.appendLine("${idx + 1}. ${q.statement}")
                if (q.options.isNotEmpty()) {
                    val opts = q.options.joinToString("   ") { opt -> "(${opt.key}) ${opt.text}" }
                    sb.appendLine("   $opts")
                }
                if (!q.correctAnswer.isNullOrBlank()) {
                    val ansLabel = if (isUrdu) "درست جواب" else "Answer"
                    sb.appendLine("   [$ansLabel: (${q.correctAnswer})]")
                }
                sb.appendLine()
            }
        }

        // Section B: Short Questions
        val shortQs = paper.questions.filter { it.type == QuestionType.SHORT_QUESTION }
        if (shortQs.isNotEmpty()) {
            sb.appendLine()
            if (isUrdu) {
                sb.appendLine("■ حصہ اول: مختصر سوالات (Short Questions) [تعداد: ${shortQs.size}]")
            } else {
                sb.appendLine("■ SECTION B: SHORT QUESTIONS [Total: ${shortQs.size}]")
            }
            sb.appendLine(separator)

            shortQs.forEachIndexed { idx, q ->
                val markStr = if (q.marks != null) " [${q.marks} نمبر]" else ""
                sb.appendLine("س-${idx + 1}: ${q.statement}$markStr")
            }
            sb.appendLine()
        }

        // Section C: Long Questions
        val longQs = paper.questions.filter { it.type == QuestionType.LONG_QUESTION }
        if (longQs.isNotEmpty()) {
            sb.appendLine()
            if (isUrdu) {
                sb.appendLine("■ حصہ دوم: تفصیلی / انشائیہ سوالات (Long Questions) [تعداد: ${longQs.size}]")
            } else {
                sb.appendLine("■ SECTION C: DETAILED / LONG QUESTIONS [Total: ${longQs.size}]")
            }
            sb.appendLine(separator)

            longQs.forEachIndexed { idx, q ->
                val markStr = if (q.marks != null) " [${q.marks} نمبر]" else ""
                sb.appendLine("سوال-${idx + 1}: ${q.statement}$markStr")
            }
            sb.appendLine()
        }

        // Footer
        sb.appendLine(border)
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        sb.appendLine("تیار کردہ بذریعہ: شامی اکیڈمی او سی آر ایپ (Shami Academy OCR)")
        sb.appendLine("تاریخ برآمد / Export Date: $dateStr")
        sb.appendLine(border)

        return sb.toString()
    }

    fun getSanitizedFileName(paper: ExamPaper, extension: String = ".txt"): String {
        val sanitized = paper.title
            .replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_\\- ]"), "")
            .trim()
            .replace(" ", "_")
            .ifBlank { "Shami_Academy_Paper" }
        return "${sanitized}_${System.currentTimeMillis()}$extension"
    }

    /**
     * Saves the formatted text to the device's public Documents/ShamiAcademy directory
     * which is directly accessible by Termux under ~/storage/shared/Documents/ShamiAcademy.
     */
    fun saveTextToLocalStorage(context: Context, paper: ExamPaper): Result<String> {
        return try {
            val fileName = getSanitizedFileName(paper, ".txt")
            val content = formatExamPaperAsText(paper)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // MediaStore approach for Android 10+
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/ShamiAcademy")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    ?: return Result.failure(Exception("Unable to create file via MediaStore"))

                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                }

                val friendlyPath = "Documents/ShamiAcademy/$fileName"
                Result.success(friendlyPath)
            } else {
                // Direct file system access for older Android
                val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val targetDir = File(docsDir, "ShamiAcademy").apply { mkdirs() }
                val targetFile = File(targetDir, fileName)

                FileOutputStream(targetFile).use { out ->
                    out.write(content.toByteArray(Charsets.UTF_8))
                }

                Result.success(targetFile.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Writes text directly into a user-selected Uri from ActivityResultContracts.CreateDocument.
     */
    fun writeTextToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun saveCustomTextToLocalStorage(context: Context, paperTitle: String, content: String): Result<String> {
        return try {
            val sanitized = paperTitle
                .replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_\\- ]"), "")
                .trim()
                .replace(" ", "_")
                .ifBlank { "Shami_Academy_Paper" }
            val fileName = "${sanitized}_${System.currentTimeMillis()}.txt"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}/ShamiAcademy")
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    ?: return Result.failure(Exception("Unable to create file via MediaStore"))

                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(content.toByteArray(Charsets.UTF_8))
                }

                val friendlyPath = "Documents/ShamiAcademy/$fileName"
                Result.success(friendlyPath)
            } else {
                val docsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val targetDir = File(docsDir, "ShamiAcademy").apply { mkdirs() }
                val targetFile = File(targetDir, fileName)

                FileOutputStream(targetFile).use { out ->
                    out.write(content.toByteArray(Charsets.UTF_8))
                }

                Result.success(targetFile.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareCustomTextFile(context: Context, paperTitle: String, content: String) {
        try {
            val sanitized = paperTitle
                .replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF_\\- ]"), "")
                .trim()
                .replace(" ", "_")
                .ifBlank { "Shami_Academy_Paper" }
            val fileName = "${sanitized}_${System.currentTimeMillis()}.txt"
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val file = File(exportDir, fileName)

            FileOutputStream(file).use { out ->
                out.write(content.toByteArray(Charsets.UTF_8))
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, paperTitle)
                putExtra(Intent.EXTRA_TEXT, "$paperTitle - Shami Academy Paper Text File")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Paper Text File (.txt)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun saveTextToCacheFile(context: Context, paper: ExamPaper): File {
        val fileName = getSanitizedFileName(paper, ".txt")
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(exportDir, fileName)

        val formattedText = formatExamPaperAsText(paper)
        FileOutputStream(file).use { out ->
            out.write(formattedText.toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun shareTextFile(context: Context, paper: ExamPaper) {
        try {
            val file = saveTextToCacheFile(context, paper)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, paper.title)
                putExtra(Intent.EXTRA_TEXT, "${paper.title} - Shami Academy Paper Text File")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Paper Text File (.txt)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error sharing file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Exam Paper Text") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Text copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Generates a complete bash sync script for Termux to automatically push all
     * Shami Academy papers to GitHub.
     */
    fun getTermuxGitSyncScript(): String {
        return """
            #!/data/data/com.termux/files/usr/bin/bash
            # =================================================================
            # Shami Academy (شامی اکیڈمی) - Termux GitHub Auto-Sync Script
            # =================================================================
            
            echo "🚀 Shami Academy Paper Sync Starting..."
            
            # Setup Termux storage access if not already done
            if [ ! -d "${'$'}HOME/storage/shared" ]; then
                echo "🔑 Requesting storage access in Termux..."
                termux-setup-storage
            fi
            
            # Target directory where the Android app saves all paper .txt files
            SHAMI_PAPERS_DIR="${'$'}HOME/storage/shared/Documents/ShamiAcademy"
            
            mkdir -p "${'$'}SHAMI_PAPERS_DIR"
            cd "${'$'}SHAMI_PAPERS_DIR" || exit 1
            
            echo "📂 Working directory: ${'$'}(pwd)"
            echo "📄 Found papers:"
            ls -1 *.txt 2>/dev/null || echo "   (No .txt papers found yet)"
            
            # Check if Git is initialized
            if [ ! -d ".git" ]; then
                echo ""
                echo "⚠️ Git repository not found in this folder."
                echo "To connect this folder to your GitHub repository, run:"
                echo "  git init"
                echo "  git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git"
                echo "  git branch -M main"
                echo "  git add ."
                echo "  git commit -m 'Initial Shami Academy papers'"
                echo "  git push -u origin main"
                exit 0
            fi
            
            echo ""
            echo "🔄 Syncing with GitHub..."
            git add .
            
            # Check if there are changes to commit
            if git diff-index --quiet HEAD --; then
                echo "✅ No new changes to push. All papers up to date!"
            else
                TIMESTAMP=${'$'}(date '+%Y-%m-%d %H:%M:%S')
                git commit -m "Auto-synced Shami Academy papers - ${'$'}TIMESTAMP"
                git push
                echo "🎉 Successfully pushed to GitHub!"
            fi
        """.trimIndent()
    }
}
