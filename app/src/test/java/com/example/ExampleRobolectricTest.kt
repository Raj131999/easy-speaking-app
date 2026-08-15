package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.InitialData
import com.example.ui.EnglishViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Easy Speaking", appName)
  }

  @Test
  fun `grammar lessons have questions and options`() {
    val lessons = InitialData.grammarLessons
    assertTrue("Lessons should not be empty", lessons.isNotEmpty())
    for (lesson in lessons) {
      assertTrue("Lesson ${lesson.id} should have a non-empty question", lesson.quizQuestion.isNotBlank())
      assertTrue("Lesson ${lesson.id} should have options", lesson.optionsString.isNotBlank())
      assertTrue("Lesson ${lesson.id} should have a correct option", lesson.correctOption.isNotBlank())
    }
  }

  @Test
  fun `extractSpokenExample removes topics correctly`() {
    val input = "• gonna (going to): I'm gonna grab some coffee at the café."
    val extracted = EnglishViewModel.extractSpokenExample(input)
    assertEquals("I'm gonna grab some coffee at the café.", extracted)
  }
}
