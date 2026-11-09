package com.example

import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.campusdirectory.provider.CampusContract

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testCampusContract_constants() {
    assertEquals("com.example.campusdirectory", CampusContract.AUTHORITY)
    assertEquals("_id", CampusContract.Contacts.ID)
    assertEquals("name", CampusContract.Contacts.NAME)
    assertEquals("phone", CampusContract.Contacts.PHONE)
    assertEquals("email", CampusContract.Contacts.EMAIL)
    assertEquals("course", CampusContract.Contacts.COURSE)
  }
}
