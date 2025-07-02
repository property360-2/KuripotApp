package com.malikhain.kuripot_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.malikhain.kuripot_app.data.KuripotDatabase
import com.malikhain.kuripot_app.data.repository.*
import com.malikhain.kuripot_app.service.AudioService
import com.malikhain.kuripot_app.service.ImportExportService
import com.malikhain.kuripot_app.ui.KuripotNavGraph
import com.malikhain.kuripot_app.ui.theme.KuripotAppTheme
import com.malikhain.kuripot_app.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KuripotAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    KuripotAppRoot()
                }
            }
        }
    }
}

@Composable
fun KuripotAppRoot() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { KuripotDatabase.getDatabase(context) }

    // Repositories
    val noteRepository = remember { NoteRepository(db.noteDao(), db.archiveDao()) }
    val categoryRepository = remember { CategoryRepository(db.categoryDao()) }
            val budgetRepository = remember { BudgetRepository(db.budgetEntryDao(), db.archiveDao(), db.budgetLimitDao()) }
    val archiveRepository = remember { ArchiveRepository(db.archiveDao()) }
    val settingRepository = remember { SettingRepository(db.settingDao()) }

    // Services
    val audioService = remember { AudioService(context) }
    val importExportService = remember {
        ImportExportService(
            noteRepository,
            categoryRepository,
            budgetRepository,
            archiveRepository,
            settingRepository
        )
    }

    // ViewModels
    val passcodeViewModel = remember { PasscodeViewModel(db.settingDao()) }
    val notesViewModel = remember { NotesViewModel(noteRepository, categoryRepository, audioService) }
    val budgetViewModel = remember { BudgetViewModel(budgetRepository, noteRepository) }
    val settingsViewModel = remember { SettingsViewModel(settingRepository, archiveRepository, importExportService) }

    val navController = rememberNavController()

    KuripotNavGraph(
        navController = navController,
        passcodeViewModel = passcodeViewModel,
        notesViewModel = notesViewModel,
        budgetViewModel = budgetViewModel,
        settingsViewModel = settingsViewModel
    )
}