package com.example

import com.example.campusdirectory.data.entity.ContactEntity
import com.example.campusdirectory.data.entity.NoteEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testContactEntityCreation() {
    val contact = ContactEntity(
      id = 1L,
      name = "Alex Rivera",
      phone = "555-0142",
      email = "arivera@campus.edu",
      course = "CS-301 Mobile Dev"
    )
    assertEquals("Alex Rivera", contact.name)
    assertEquals("CS-301 Mobile Dev", contact.course)
  }

  @Test
  fun testNoteEntityCreation() {
    val note = NoteEntity(
      id = 1L,
      title = "ContentProvider Rules",
      body = "Exposes data via content:// URIs with permissions.",
      courseTag = "CS-301"
    )
    assertEquals("CS-301", note.courseTag)
  }
}
