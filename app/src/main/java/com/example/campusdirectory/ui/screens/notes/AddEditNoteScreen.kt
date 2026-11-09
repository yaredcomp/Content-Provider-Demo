/**
 * Campus Directory & Notes Hub - University Teaching Demo
 * Chapter 5: Content Providers and Query Providers
 *
 * This is a QUERY PROVIDER — internal only. No URIs. No permissions. Used by the app's own UI.
 *
 * AddEditNoteScreen provides editing controls for student notes.
 */
package com.example.campusdirectory.ui.screens.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditNoteScreen(
    viewModel: AddEditNoteViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditNoteEvent.SaveSuccess -> onNavigateBack()
                is AddEditNoteEvent.DeleteSuccess -> onNavigateBack()
                is AddEditNoteEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (formState.noteId == 0L) {
                            stringResource(R.string.add_note)
                        } else {
                            stringResource(R.string.edit_note)
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("note_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (formState.noteId > 0L) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.testTag("note_delete_action")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Note"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    titleContentColor = MaterialTheme.colorScheme.onSecondary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSecondary,
                    actionIconContentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Title Field
            OutlinedTextField(
                value = formState.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text(stringResource(R.string.label_note_title)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Title,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                isError = formState.errorMessage != null,
                supportingText = {
                    formState.errorMessage?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_note_title")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Course Tag Field
            OutlinedTextField(
                value = formState.courseTag,
                onValueChange = viewModel::onCourseTagChange,
                label = { Text(stringResource(R.string.label_note_course)) },
                placeholder = { Text("e.g. CS-301 Lecture") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_note_course_tag")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body Field
            OutlinedTextField(
                value = formState.body,
                onValueChange = viewModel::onBodyChange,
                label = { Text(stringResource(R.string.label_note_body)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                },
                minLines = 6,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_note_body")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Save Button
            Button(
                onClick = viewModel::saveNote,
                enabled = !formState.isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_save_note")
            ) {
                if (formState.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(4.dp)
                    )
                } else {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.save_note),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_note)) },
                text = { Text("Are you sure you want to delete this note?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteNote()
                        },
                        modifier = Modifier.testTag("confirm_delete_note_button")
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
