package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.export.TextFileExporter
import com.example.ocr.SamplePapers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Shami Academy OCR", appName)
  }

  @Test
  fun `verify paper formatting contains sections`() {
    val paper = SamplePapers.urduBiologyPaper
    val formatted = TextFileExporter.formatExamPaperAsText(paper)
    assertTrue(formatted.contains("شامی اکیڈمی"))
    assertTrue(formatted.contains("کثیر الانتخابی سوالات"))
    assertTrue(formatted.contains("مختصر سوالات"))
    assertTrue(formatted.contains("تفصیلی / انشائیہ سوالات"))
  }

  @Test
  fun `verify saveTextToLocalStorage creates file`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val paper = SamplePapers.englishPhysicsPaper
    val result = TextFileExporter.saveTextToLocalStorage(context, paper)
    assertTrue(result.isSuccess)
    val path = result.getOrNull()
    assertTrue(path != null && path.contains("ShamiAcademy"))
  }

  @Test
  fun `verify termux git sync script content`() {
    val script = TextFileExporter.getTermuxGitSyncScript()
    assertTrue(script.contains("termux-setup-storage"))
    assertTrue(script.contains("Documents/ShamiAcademy"))
    assertTrue(script.contains("git push"))
  }

  @Test
  fun `verify saveCustomTextToLocalStorage creates edited file`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val editedContent = "Custom Exam Questions\nQ1: Sample Question"
    val result = TextFileExporter.saveCustomTextToLocalStorage(context, "CustomPaper", editedContent)
    assertTrue(result.isSuccess)
    val path = result.getOrNull()
    assertTrue(path != null && path.contains("CustomPaper"))
  }
}
