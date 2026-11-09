# Campus Directory & Notes Hub — Architecture & Implementation Plan

A university teaching demonstration Android application contrasting **Content Providers** (cross-process, URI-based data sharing with permissions) and **Query Providers** (internal Room DAO + Repository queries without URI exposure).

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural and design decisions below were confirmed in Phase 1 and guide the complete implementation:

- **Confirmed Decision 1 (Dependency Injection)**: **AppContainer Architecture** — A clean, robust `AppContainer` pattern providing manual constructor injection and ViewModel Factories. Avoids Gradle KSP/Hilt version flakiness while keeping architectural purity and testability.
- **Confirmed Decision 2 (Visual Aesthetic)**: **Modern Campus Indigo & Amber** — Collegiate indigo surfaces (`#1E293B`, `#312E81`, `#4338CA`), crisp slate cards, and vivid collegiate warm amber accents (`#F59E0B`), providing high contrast and academic elegance.
- **Confirmed Decision 3 (Interactive Teaching Controls)**: **Interactive ContentResolver Actions** — On `ShareDemoScreen`, users can perform live `query()`, `insert()`, and `delete()` operations directly through Android's `ContentResolver` to verify that external URI interactions mutate the same Room database shared by internal DAOs in real time.

---

## 1. Overview & Core Concept

- **What It Does**: Provides a functional student directory and personal course notebook while serving as a dual-concept teaching laboratory for Android Ch. 5.
  1. **Content Provider**: Exposes contacts to other applications and system clients via `content://com.example.campusdirectory/contacts`, protected by custom read/write permissions (`com.example.campusdirectory.READ_CONTACTS` and `WRITE_CONTACTS`).
  2. **Query Provider**: Encapsulates internal contact and note access using Room DAOs (`ContactDao`, `NoteDao`) and Clean Architecture Repositories (`ContactRepository`, `NoteRepository`) with reactive Kotlin Flows and search capability (`searchByName`).
  3. **ShareDemo Screen**: Acts as an internal client calling `context.contentResolver` on the app's own ContentProvider URI, displaying returned cursor rows and providing one-tap ADB commands for terminal testing.
- **Target Audience / Persona**: Computer Science university students and mobile engineering instructors learning Android IPC, content resolution, database security, and modern Jetpack Compose M3.
- **Key Value**: Directly proves that Room DAOs and Content Providers can share the same underlying SQLite storage, clarifying when to use lightweight internal DAOs versus system-wide URI providers.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Directory Browsing & Search (Query Provider Demo)**:
   - User opens the app on the "Contacts" tab.
   - Initial demo contacts (e.g., *Alex Rivera*, *Maya Patel*, *Jordan Chen*) are visible with phone, email, and course badges.
   - User types in the search bar: triggers `ContactDao.search(query)` via `ContactRepository`, updating the UI instantly via reactive Flow.
2. **Contact Management**:
   - Tap FAB $\rightarrow$ `AddEditContactScreen` with validation (Name, Phone, Email, Course).
   - Save updates the Room DB; automatically reflected across internal UI and ContentProvider queries.
   - Tap contact card $\rightarrow$ `ContactDetailScreen` to view details, initiate quick call/email intents, edit, or delete.
3. **Personal Notes (Pure Internal Storage)**:
   - User switches to "Notes" tab.
   - View course notes (e.g., *CS301 Lecture Notes*, *Lab 4 ContentProvider checklist*).
   - Tap FAB $\rightarrow$ `AddEditNoteScreen` to author or update notes with tags. Notes are strictly internal with no ContentProvider exposure.
4. **Teaching Lab / ShareDemoScreen**:
   - Accessible via the top bar science/school beaker action icon.
   - Displays Authority string (`com.example.campusdirectory`), URI patterns, and MIME types.
   - **ContentResolver Query Button**: Queries `CampusContract.Contacts.CONTENT_URI` using `contentResolver.query()`, parsing the cursor into readable items.
   - **ContentResolver Insert Button**: Inserts a sample student via `contentResolver.insert()`.
   - **ContentResolver Delete Button**: Deletes a test entry via `contentResolver.delete()`.
   - **Copy ADB Command**: One-tap clipboard copy of `adb shell content query ...` with preview of expected shell output.

### Visual Identity & Theme
- **Color Tokens**:
  - `Primary`: Deep Collegiate Indigo (`#4338CA`)
  - `OnPrimary`: Pure White (`#FFFFFF`)
  - `PrimaryContainer`: Soft Slate Indigo (`#E0E7FF`)
  - `Secondary`: Warm Collegiate Amber (`#F59E0B`)
  - `Background & Surface`: Light Crisp Slate (`#F8FAFC` light / `#0F172A` dark)
  - `SurfaceVariant`: Subtle Border Slate (`#F1F5F9` light / `#1E293B` dark)
- **Typography & Layout**:
  - Clear M3 hierarchy with bold category headers and monospace font styling for URIs, permissions, and ADB commands.
  - Safe-area edge-to-edge support with `Modifier.statusBarsPadding()` and `WindowInsets.navigationBars`.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: AppContainer vs. Heavy Dagger-Hilt Setup**:
  - *Chosen Approach*: `CampusApp` creates a singleton `AppContainer` holding `AppDatabase`, `ContactRepository`, and `NoteRepository`. ViewModels use `viewModelFactory` via simple constructor injection.
  - *Why*: Eliminates complex KSP annotation-processor synchronization issues while maintaining clean separation of concerns and 100% testability.
- **Decision 2: Shared Room SQLite Database for ContentProvider**:
  - *Chosen Approach*: `CampusContactProvider` gets the database instance from `AppDatabase.getInstance(context)`. The provider's `query()` returns a `Cursor` generated by Room or SQLite, ensuring atomicity and zero sync latency.
  - *Why*: A record created via DAO is immediately readable via `contentResolver.query()`, and records inserted via `contentResolver.insert()` immediately trigger Room `Flow` emissions in the UI via `ContentResolver.notifyChange()`.
- **Decision 3: In-App ContentResolver Consumer**:
  - *Chosen Approach*: `ShareDemoViewModel` calls `context.contentResolver` directly.
  - *Why*: Gives students immediate visual proof inside the app emulator without needing a second APK installed or terminal access open.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                       Campus Directory & Notes Hub                     │
└────────────────────────────────────────────────────────────────────────┘
                   │                                     │
                   ▼                                     ▼
 ┌───────────────────────────────────┐ ┌─────────────────────────────────┐
 │       INTERNAL APP CLIENT         │ │       EXTERNAL APIS / ADB       │
 │   (HomeScreen, Notes, Contacts)   │ │  (ShareDemoScreen / 3rd Party)  │
 └───────────────────────────────────┘ └─────────────────────────────────┘
                   │                                     │
     Query Provider: Internal Calls                      │ content:// URI Calls
     (No URIs, No Permissions)                           │ (Enforced by Permissions)
                   │                                     ▼
                   │                   ┌─────────────────────────────────┐
                   │                   │     CampusContactProvider       │
                   │                   │   (Android ContentProvider)     │
                   │                   └─────────────────────────────────┘
                   │                                     │
                   ▼                                     ▼
 ┌───────────────────────────────────┐                   │
 │   Contact & Note Repositories     │                   │
 └───────────────────────────────────┘                   │
                   │                                     │
                   ▼                                     ▼
 ┌───────────────────────────────────┐ ┌─────────────────────────────────┐
 │       Room DAOs (Flows)           │ │       Room Raw SQLite / DAO     │
 └───────────────────────────────────┘ └─────────────────────────────────┘
                   │                                     │
                   └──────────────────┬──────────────────┘
                                      ▼
                       ┌─────────────────────────────┐
                       │     AppDatabase (Room)      │
                       │   SQLite: contacts & notes  │
                       └─────────────────────────────┘
```

### Data Models & Entities
1. **`ContactEntity`**:
   - `id: Long` (Auto-generated primary key)
   - `name: String`
   - `phone: String`
   - `email: String`
   - `course: String`
2. **`NoteEntity`**:
   - `id: Long` (Auto-generated primary key)
   - `title: String`
   - `body: String`
   - `courseTag: String`

### Content Provider Contract (`CampusContract`)
- **Authority**: `com.example.campusdirectory`
- **Base URI**: `content://com.example.campusdirectory`
- **Contacts URI**: `content://com.example.campusdirectory/contacts`
- **Columns**: `_id`, `name`, `phone`, `email`, `course`
- **Permissions**: `com.example.campusdirectory.READ_CONTACTS`, `com.example.campusdirectory.WRITE_CONTACTS`

### Comprehensive File Implementation List
- `metadata.json` (Platform title & description)
- `res/values/strings.xml` (Localization, headers, labels, ADB descriptions)
- `res/values/colors.xml`
- `app/build.gradle.kts` (Room, Compose, Navigation, Material 3)
- `app/src/main/AndroidManifest.xml` (Provider declaration, authorities, custom permissions)
- `data/entity/ContactEntity.kt` & `data/entity/NoteEntity.kt`
- `data/db/ContactDao.kt` & `data/db/NoteDao.kt` (Query Provider)
- `data/db/AppDatabase.kt` (RoomDatabase with prepopulation callback)
- `data/repository/ContactRepository.kt` & `data/repository/NoteRepository.kt`
- `provider/CampusContract.kt` (Contract constants, URIs, columns, MIME types)
- `provider/CampusContactProvider.kt` (ContentProvider implementation: query, insert, update, delete, getType, UriMatcher)
- `di/AppContainer.kt` & `CampusApp.kt` (Application class and DI container)
- `ui/theme/Color.kt`, `ui/theme/Theme.kt`, `ui/theme/Type.kt`
- `ui/components/ContactCard.kt`, `NoteCard.kt`, `TeachingBanner.kt`
- `ui/navigation/AppNavGraph.kt`
- `ui/screens/home/HomeScreen.kt`
- `ui/screens/contacts/ContactsScreen.kt` & `ContactsViewModel.kt`
- `ui/screens/contacts/AddEditContactScreen.kt` & `AddEditContactViewModel.kt`
- `ui/screens/contacts/ContactDetailScreen.kt` & `ContactDetailViewModel.kt`
- `ui/screens/notes/NotesScreen.kt` & `NotesViewModel.kt`
- `ui/screens/notes/AddEditNoteScreen.kt` & `AddEditNoteViewModel.kt`
- `ui/screens/sharedemo/ShareDemoScreen.kt` & `ShareDemoViewModel.kt`
- `MainActivity.kt`
- `README.md` (Pedagogical guide, concept comparison table, and exact ADB terminal scripts)
