/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * UI: HomeScreen
 * Primary hub containing the "Contacts" and "Notes" tabs alongside
 * immediate access to the Chapter 5 Teaching Lab (ShareDemoScreen).
 */
package com.example.campusdirectory.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.R
import com.example.campusdirectory.ui.screens.contacts.ContactsScreen
import com.example.campusdirectory.ui.screens.contacts.ContactsViewModel
import com.example.campusdirectory.ui.screens.notes.NotesScreen
import com.example.campusdirectory.ui.screens.notes.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contactsViewModel: ContactsViewModel,
    notesViewModel: NotesViewModel,
    onContactClick: (Long) -> Unit,
    onAddContactClick: () -> Unit,
    onNoteClick: (Long) -> Unit,
    onAddNoteClick: () -> Unit,
    onShareDemoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf(
        stringResource(R.string.tab_contacts),
        stringResource(R.string.tab_notes)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = onShareDemoClick,
                        modifier = Modifier.testTag("action_share_demo")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = stringResource(R.string.nav_share_demo)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = tabTitles[0],
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.testTag("tab_contacts")
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            text = tabTitles[1],
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.testTag("tab_notes")
                )
            }

            when (selectedTabIndex) {
                0 -> ContactsScreen(
                    viewModel = contactsViewModel,
                    onContactClick = onContactClick,
                    onAddContactClick = onAddContactClick
                )
                1 -> NotesScreen(
                    viewModel = notesViewModel,
                    onNoteClick = onNoteClick,
                    onAddNoteClick = onAddNoteClick
                )
            }
        }
    }
}
