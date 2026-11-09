/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * MAIN ACTIVITY: MainActivity
 * Hosts the Jetpack Navigation graph, initializes ViewModels via the AppContainer,
 * and sets up edge-to-edge Material 3 theming.
 */
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campusdirectory.ui.navigation.AppDestinations
import com.example.campusdirectory.ui.screens.contacts.AddEditContactScreen
import com.example.campusdirectory.ui.screens.contacts.AddEditContactViewModel
import com.example.campusdirectory.ui.screens.contacts.ContactDetailScreen
import com.example.campusdirectory.ui.screens.contacts.ContactDetailViewModel
import com.example.campusdirectory.ui.screens.contacts.ContactsViewModel
import com.example.campusdirectory.ui.screens.home.HomeScreen
import com.example.campusdirectory.ui.screens.notes.AddEditNoteScreen
import com.example.campusdirectory.ui.screens.notes.AddEditNoteViewModel
import com.example.campusdirectory.ui.screens.notes.NotesViewModel
import com.example.campusdirectory.ui.screens.sharedemo.ShareDemoScreen
import com.example.campusdirectory.ui.screens.sharedemo.ShareDemoViewModel
import com.example.ui.theme.CampusDirectoryTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as CampusApp).container

        setContent {
            CampusDirectoryTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CampusAppNavHost(
                        appContainer = appContainer,
                        application = application
                    )
                }
            }
        }
    }
}

@Composable
fun CampusAppNavHost(
    appContainer: com.example.campusdirectory.di.AppContainer,
    application: android.app.Application,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.HOME_ROUTE,
        modifier = modifier
    ) {
        // HOME SCREEN (Tabs: Contacts & Notes)
        composable(AppDestinations.HOME_ROUTE) {
            val contactsViewModel: ContactsViewModel = viewModel(
                factory = ContactsViewModel.Factory(appContainer.contactRepository)
            )
            val notesViewModel: NotesViewModel = viewModel(
                factory = NotesViewModel.Factory(appContainer.noteRepository)
            )

            HomeScreen(
                contactsViewModel = contactsViewModel,
                notesViewModel = notesViewModel,
                onContactClick = { contactId ->
                    navController.navigate(AppDestinations.contactDetail(contactId))
                },
                onAddContactClick = {
                    navController.navigate(AppDestinations.addContact())
                },
                onNoteClick = { noteId ->
                    navController.navigate(AppDestinations.editNote(noteId))
                },
                onAddNoteClick = {
                    navController.navigate(AppDestinations.addNote())
                },
                onShareDemoClick = {
                    navController.navigate(AppDestinations.shareDemo())
                }
            )
        }

        // CONTACT DETAIL SCREEN
        composable(
            route = AppDestinations.CONTACT_DETAIL_ROUTE,
            arguments = listOf(
                navArgument("contactId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getLong("contactId") ?: 0L
            val detailViewModel: ContactDetailViewModel = viewModel(
                factory = ContactDetailViewModel.Factory(contactId, appContainer.contactRepository)
            )

            ContactDetailScreen(
                viewModel = detailViewModel,
                onNavigateBack = { navController.popBackStack() },
                onEditContact = { id ->
                    navController.navigate(AppDestinations.editContact(id))
                }
            )
        }

        // ADD / EDIT CONTACT SCREEN
        composable(
            route = AppDestinations.ADD_EDIT_CONTACT_ROUTE,
            arguments = listOf(
                navArgument("contactId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getLong("contactId") ?: 0L
            val addEditViewModel: AddEditContactViewModel = viewModel(
                factory = AddEditContactViewModel.Factory(contactId, appContainer.contactRepository)
            )

            AddEditContactScreen(
                viewModel = addEditViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ADD / EDIT NOTE SCREEN
        composable(
            route = AppDestinations.ADD_EDIT_NOTE_ROUTE,
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getLong("noteId") ?: 0L
            val addEditNoteViewModel: AddEditNoteViewModel = viewModel(
                factory = AddEditNoteViewModel.Factory(noteId, appContainer.noteRepository)
            )

            AddEditNoteScreen(
                viewModel = addEditNoteViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // CHAPTER 5 TEACHING LAB / SHARE DEMO SCREEN
        composable(AppDestinations.SHARE_DEMO_ROUTE) {
            val shareDemoViewModel: ShareDemoViewModel = viewModel(
                factory = ShareDemoViewModel.Factory(application)
            )

            ShareDemoScreen(
                viewModel = shareDemoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
