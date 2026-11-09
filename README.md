# Campus Directory & Notes Hub

An instructional Android application designed for university mobile computing courses covering **Chapter 5: Content Providers and Query Providers**. The application demonstrates side-by-side how an Android app can maintain internal private data while selectively exposing structured relational data to external apps and system clients via standard `content://` URIs and Android security permissions.

---

## How to Run

1. Open the project in Android Studio (or run the app directly in the AI Studio streaming emulator).
2. Build and run on an Android 7.0+ (API 24+) emulator or physical device.
3. The Room database automatically seeds on first launch with 3 classmates and 2 course notes.
4. Tap the **Science Lab icon** in the top bar to open the **Chapter 5 Teaching Lab** screen.

---

## Teaching Guide

### Section A: How the ContentProvider Works

The application registers a custom `ContentProvider` named `CampusContactProvider` with the authority `com.example.campusdirectory`. It declares custom read and write permissions in `AndroidManifest.xml` (`com.example.campusdirectory.READ_CONTACTS` and `WRITE_CONTACTS`).

When external clients (other Android applications or terminal commands via ADB) make requests to `content://com.example.campusdirectory/contacts`, the Android OS:
1. Validates that the calling process holds the required `<uses-permission>` token.
2. Routes the request across process boundaries (IPC) to `CampusContactProvider`.
3. Maps the URI pattern using `UriMatcher` (`contacts` for directory or `contacts/#` for an individual item).
4. Translates external column projections (`_id`, `name`, `phone`, `email`, `course`) into internal Room SQLite queries.
5. Emits `notifyChange(uri, null)` on mutations so real-time observers refresh automatically.

#### Exact ADB Terminal Commands for Live Demonstration

You can query and mutate the app's database directly from your development machine's terminal:

**1. Query all contacts:**
```bash
adb shell content query --uri content://com.example.campusdirectory/contacts
```

*Expected output format:*
```text
Row: 0 _id=1, name=Alex Rivera, phone=555-0142, email=arivera@campus.edu, course=CS-301 Mobile Dev
Row: 1 _id=2, name=Maya Patel, phone=555-0188, email=mpatel@campus.edu, course=CS-305 Databases
Row: 2 _id=3, name=Jordan Chen, phone=555-0199, email=jchen@campus.edu, course=SE-410 Software Arch
```

**2. Insert a new student record via URI:**
```bash
adb shell content insert --uri content://com.example.campusdirectory/contacts \
  --bind name:s:Abebe --bind phone:s:0911223344 \
  --bind email:s:abebe@uni.edu --bind course:s:CS301
```

*Immediately observe the app screen: the newly inserted classmate appears without restarting the app!*

**3. Delete a contact by ID:**
```bash
adb shell content delete --uri content://com.example.campusdirectory/contacts \
  --where "_id=2"
```

---

### Section B: How the Query Provider Works

The **Query Provider** pattern refers to internal data access within the application's own process. Look at:
- `app/src/main/java/com/example/campusdirectory/data/db/ContactDao.kt`
- `app/src/main/java/com/example/campusdirectory/data/db/NoteDao.kt`
- `app/src/main/java/com/example/campusdirectory/data/repository/ContactRepository.kt`
- `app/src/main/java/com/example/campusdirectory/data/repository/NoteRepository.kt`

Key characteristics of the internal Query Provider:
- **No URIs**: Queries are strongly-typed Kotlin methods (`contactDao.observeAll()`, `contactDao.search(q)`).
- **No IPC overhead**: Execution occurs directly in-process via Room and Kotlin Coroutines.
- **No Android Permissions**: Internal classes don't need Android security tokens to read their own sandbox.
- **Private Entities**: Notice that `notes` (`NoteEntity`) are completely hidden from the ContentProvider. Only contacts are exposed, demonstrating principle of least privilege.

---

### Section C: Content Provider vs. Query Provider Comparison

| Feature / Dimension | Content Provider (`CampusContactProvider`) | Query Provider (`ContactDao` / `Repository`) |
| :--- | :--- | :--- |
| **Primary Purpose** | Sharing data with OTHER apps / system tools | Internal app data access & state management |
| **Addressing Method** | `Uri` (`content://com.example.campusdirectory/contacts`) | Direct Kotlin function calls on DAO / Repo |
| **Data Protocol** | Android `Cursor` / `ContentValues` / `ContentResolver` | Kotlin `List<T>`, `Flow<T>`, Coroutine `suspend` |
| **Process Boundary** | Cross-Process (IPC via Binder / Binder transaction) | In-Process (direct memory access / local SQLite) |
| **Access Control** | Android Permissions (`READ_CONTACTS`, `WRITE_CONTACTS`) | Standard JVM / Kotlin scope (`private`, `internal`) |
| **Primary Use Cases** | Contacts, Calendar, MediaStore, DocumentsProvider | UI ViewModels, offline cache, user draft notes |

---

### Proving Cross-App Sharing and SecurityException in the Classroom

1. **Cross-App Access**: Any second demo application installed on the device that includes `<uses-permission android:name="com.example.campusdirectory.READ_CONTACTS" />` in its manifest can call `contentResolver.query(Uri.parse("content://com.example.campusdirectory/contacts"), ...)` and read the full student list.
2. **SecurityException Demonstration**: In `AndroidManifest.xml`, comment out `<uses-permission android:name="com.example.campusdirectory.READ_CONTACTS" />` or try querying from an external app without declaring the permission. Attempting to query the provider will throw a `java.lang.SecurityException: Permission Denial`, which the `ShareDemoScreen` surfaces via Snackbar.
