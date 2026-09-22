package com.fieldvet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.fieldvet.model.InferenceEngine
import com.fieldvet.model.KnowledgeBase
import com.fieldvet.model.ModelDownloadManager
import com.fieldvet.model.RetrievalEngine
import com.fieldvet.model.TriageUseCase
import com.fieldvet.view.FieldVetTheme
import com.fieldvet.view.ModelDownloadScreen
import com.fieldvet.view.ResponseScreen
import com.fieldvet.view.SymptomInputScreen
import com.fieldvet.viewmodel.ModelDownloadUiState
import com.fieldvet.viewmodel.ModelDownloadViewModel
import com.fieldvet.viewmodel.TriageUiState
import com.fieldvet.viewmodel.TriageViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FieldVetTheme {
                val downloadFactory = viewModelFactory {
                    initializer { ModelDownloadViewModel(ModelDownloadManager(applicationContext)) }
                }
                val downloadViewModel: ModelDownloadViewModel = viewModel(factory = downloadFactory)
                val downloadState by downloadViewModel.uiState.collectAsState()

                if (downloadState !is ModelDownloadUiState.Ready) {
                    ModelDownloadScreen(uiState = downloadState, onRetry = downloadViewModel::retry)
                } else {
                    val triageFactory = viewModelFactory {
                        initializer {
                            val knowledgeBase = KnowledgeBase(applicationContext).apply { seedIfEmpty() }
                            TriageViewModel(
                                TriageUseCase(
                                    RetrievalEngine(knowledgeBase),
                                    InferenceEngine(applicationContext),
                                )
                            )
                        }
                    }
                    val viewModel: TriageViewModel = viewModel(factory = triageFactory)
                    val uiState by viewModel.uiState.collectAsState()

                    when (uiState) {
                        is TriageUiState.Idle, is TriageUiState.Loading ->
                            SymptomInputScreen(uiState = uiState, onSubmit = viewModel::onSymptomSubmitted)
                        is TriageUiState.Success, is TriageUiState.Error ->
                            ResponseScreen(uiState = uiState, onNewTriage = viewModel::reset)
                    }
                }
            }
        }
    }
}
