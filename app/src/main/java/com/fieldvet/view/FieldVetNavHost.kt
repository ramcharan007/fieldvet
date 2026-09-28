package com.fieldvet.view

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fieldvet.model.InferenceEngine
import com.fieldvet.model.KnowledgeBase
import com.fieldvet.model.ModelDownloadManager
import com.fieldvet.model.RetrievalEngine
import com.fieldvet.model.TriageUseCase
import com.fieldvet.viewmodel.ModelDownloadViewModel
import com.fieldvet.viewmodel.SplashDestination
import com.fieldvet.viewmodel.SplashViewModel
import com.fieldvet.viewmodel.TriageUiState
import com.fieldvet.viewmodel.TriageViewModel

private const val ROUTE_SPLASH = "splash"
private const val ROUTE_MODEL_SETUP = "model_setup"
private const val ROUTE_SPECIES_SELECT = "species_select"
private const val ROUTE_TRIAGE_FLOW = "triage_flow"
private const val ARG_SPECIES = "species"

private fun symptomInputRoute(species: String) = "symptom_input/$species"
private fun responseRoute(species: String) = "response/$species"
private fun noMatchRoute(species: String) = "no_match/$species"

private fun triageViewModelFactory(context: Context) = viewModelFactory {
    initializer {
        val knowledgeBase = KnowledgeBase(context).apply { seedIfEmpty() }
        TriageViewModel(TriageUseCase(RetrievalEngine(knowledgeBase), InferenceEngine(context)))
    }
}

@Composable
fun FieldVetNavHost() {
    val context = LocalContext.current.applicationContext
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = ROUTE_SPLASH) {
        composable(ROUTE_SPLASH) {
            val splashViewModel: SplashViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { SplashViewModel(ModelDownloadManager(context)) }
                },
            )
            SplashScreen(
                viewModel = splashViewModel,
                onNavigate = { destination ->
                    val target = when (destination) {
                        SplashDestination.ModelSetup -> ROUTE_MODEL_SETUP
                        SplashDestination.SpeciesSelect -> ROUTE_SPECIES_SELECT
                    }
                    navController.navigate(target) {
                        popUpTo(ROUTE_SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(ROUTE_MODEL_SETUP) {
            val downloadViewModel: ModelDownloadViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ModelDownloadViewModel(ModelDownloadManager(context)) }
                },
            )
            val uiState by downloadViewModel.uiState.collectAsState()
            ModelSetupScreen(
                uiState = uiState,
                onRetry = downloadViewModel::retry,
                onContinue = {
                    navController.navigate(ROUTE_SPECIES_SELECT) {
                        popUpTo(ROUTE_MODEL_SETUP) { inclusive = true }
                    }
                },
            )
        }

        composable(ROUTE_SPECIES_SELECT) {
            SpeciesSelectScreen(
                onSpeciesSelected = { species ->
                    navController.navigate(symptomInputRoute(species))
                },
            )
        }

        navigation(
            route = ROUTE_TRIAGE_FLOW,
            startDestination = symptomInputRoute("{$ARG_SPECIES}"),
        ) {
            composable(
                route = symptomInputRoute("{$ARG_SPECIES}"),
                arguments = listOf(navArgument(ARG_SPECIES) { type = NavType.StringType }),
            ) { entry ->
                val species = entry.arguments?.getString(ARG_SPECIES) ?: "Cattle"
                val parentEntry = remember(entry) { navController.getBackStackEntry(ROUTE_TRIAGE_FLOW) }
                val triageViewModel: TriageViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = triageViewModelFactory(context),
                )
                val uiState by triageViewModel.uiState.collectAsState()

                LaunchedEffect(uiState) {
                    when (uiState) {
                        is TriageUiState.Success ->
                            navController.navigate(responseRoute(species)) { launchSingleTop = true }
                        is TriageUiState.NoMatch ->
                            navController.navigate(noMatchRoute(species)) { launchSingleTop = true }
                        else -> Unit
                    }
                }

                SymptomInputScreen(
                    species = species,
                    uiState = uiState,
                    onBack = { navController.popBackStack() },
                    onSubmit = { query -> triageViewModel.onSymptomSubmitted(species, query) },
                )
            }

            composable(
                route = responseRoute("{$ARG_SPECIES}"),
                arguments = listOf(navArgument(ARG_SPECIES) { type = NavType.StringType }),
            ) { entry ->
                val species = entry.arguments?.getString(ARG_SPECIES) ?: "Cattle"
                val parentEntry = remember(entry) { navController.getBackStackEntry(ROUTE_TRIAGE_FLOW) }
                val triageViewModel: TriageViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = triageViewModelFactory(context),
                )
                val uiState by triageViewModel.uiState.collectAsState()

                LaunchedEffect(uiState) {
                    // Only reachable via process-death restoration (normal navigation always
                    // arrives here from a non-Idle state) - bounce back rather than show blank.
                    if (uiState is TriageUiState.Idle) {
                        navController.navigate(symptomInputRoute(species)) {
                            popUpTo(ROUTE_SPECIES_SELECT)
                        }
                    }
                }

                val successState = uiState as? TriageUiState.Success
                if (successState != null) {
                    val symptomText by triageViewModel.submittedSymptomText.collectAsState()
                    val streamState by triageViewModel.streamState.collectAsState()
                    TriageResponseScreen(
                        species = species,
                        symptomText = symptomText,
                        result = successState.result,
                        guidanceText = streamState.text,
                        isStreaming = streamState.isStreaming,
                        streamError = streamState.error,
                        onRetryStreaming = triageViewModel::retryStreaming,
                        onNewSymptomCheck = {
                            triageViewModel.reset()
                            navController.navigate(symptomInputRoute(species)) {
                                popUpTo(ROUTE_SPECIES_SELECT)
                            }
                        },
                    )
                }
            }

            composable(
                route = noMatchRoute("{$ARG_SPECIES}"),
                arguments = listOf(navArgument(ARG_SPECIES) { type = NavType.StringType }),
            ) { entry ->
                val species = entry.arguments?.getString(ARG_SPECIES) ?: "Cattle"
                val parentEntry = remember(entry) { navController.getBackStackEntry(ROUTE_TRIAGE_FLOW) }
                val triageViewModel: TriageViewModel = viewModel(
                    viewModelStoreOwner = parentEntry,
                    factory = triageViewModelFactory(context),
                )
                val uiState by triageViewModel.uiState.collectAsState()

                LaunchedEffect(uiState) {
                    if (uiState is TriageUiState.Idle) {
                        navController.navigate(symptomInputRoute(species)) {
                            popUpTo(ROUTE_SPECIES_SELECT)
                        }
                    }
                }

                NoMatchScreen(
                    onNewSymptomCheck = {
                        triageViewModel.reset()
                        navController.navigate(symptomInputRoute(species)) {
                            popUpTo(ROUTE_SPECIES_SELECT)
                        }
                    },
                )
            }
        }
    }
}
