package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Html
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Html
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.repository.IctLabRepository
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AlgorithmFlowchartScreen
import com.example.ui.screens.CLabScreen
import com.example.ui.screens.ExamScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HtmlLabScreen
import com.example.ui.screens.HtmlPlaygroundScreen
import com.example.ui.screens.ProgressAndNotebookScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.theme.HscIctLabTheme

object AppRoutes {
    const val ROUTE_HOME = "home"
    const val ROUTE_HTML_LAB = "html_lab"
    const val ROUTE_C_LAB = "c_lab"
    const val ROUTE_PROGRESS = "progress"
    const val ROUTE_SUPPORT = "support"
    const val ROUTE_EXAM = "exam"
    const val ROUTE_ADMIN = "admin"
    const val ROUTE_SETTINGS = "settings"
    const val ROUTE_ALGO_FLOW = "algorithm_flowchart"
    const val ROUTE_CALCULATOR = "scientific_calculator"
    const val ROUTE_CALC_FX100 = "calc_fx100"
    const val ROUTE_CALC_FX991 = "calc_fx991"
    const val ROUTE_HTML_PLAYGROUND = "html_playground"
}

private data class NavTab(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val primaryTabs = listOf(
    NavTab(AppRoutes.ROUTE_HOME, R.string.nav_home, Icons.Rounded.Home, Icons.Outlined.Home),
    NavTab(AppRoutes.ROUTE_HTML_LAB, R.string.nav_html, Icons.Rounded.Html, Icons.Outlined.Html),
    NavTab(AppRoutes.ROUTE_C_LAB, R.string.nav_c, Icons.Rounded.Code, Icons.Outlined.Code),
    NavTab(AppRoutes.ROUTE_PROGRESS, R.string.nav_progress, Icons.Rounded.BarChart, Icons.Outlined.BarChart),
    NavTab(AppRoutes.ROUTE_SUPPORT, R.string.nav_support, Icons.Rounded.SupportAgent, Icons.Outlined.SupportAgent)
)

@Composable
fun HscIctLabApp() {
    val context = LocalContext.current.applicationContext
    val viewModel: IctLabViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                IctLabViewModel(IctLabRepository(context))
            }
        }
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val htmlProblemId by viewModel.selectedHtmlProblemId.collectAsStateWithLifecycle()
    val htmlCode by viewModel.htmlCodeValue.collectAsStateWithLifecycle()
    val htmlCheck by viewModel.htmlCheckResult.collectAsStateWithLifecycle()
    val autoClose by viewModel.autoCloseSuggestion.collectAsStateWithLifecycle()
    val playgroundHtmlCode by viewModel.playgroundHtmlCode.collectAsStateWithLifecycle()
    val playgroundAutoClose by viewModel.playgroundAutoClose.collectAsStateWithLifecycle()

    val cProblemId by viewModel.selectedCProblemId.collectAsStateWithLifecycle()
    val cCode by viewModel.cCodeValue.collectAsStateWithLifecycle()
    val cStdin by viewModel.cStdin.collectAsStateWithLifecycle()
    val cResult by viewModel.cResult.collectAsStateWithLifecycle()
    val isRunningC by viewModel.isRunningC.collectAsStateWithLifecycle()
    val preferCloud by viewModel.preferCloudCompiler.collectAsStateWithLifecycle()

    val supportPrefill by viewModel.supportPrefill.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMsg) {
        val msg = snackbarMsg
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = backStackEntry?.destination
    val isTopLevelRoute = primaryTabs.any { tab ->
        currentDest?.hierarchy?.any { it.route == tab.route } == true
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    HscIctLabTheme(darkTheme = state.settings.isDarkMode) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail && isTopLevelRoute) {
                        NavigationBar {
                            primaryTabs.forEach { tab ->
                                val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                                val label = stringResource(tab.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    },
                                    modifier = Modifier.testTag("nav_${tab.route}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail && isTopLevelRoute) {
                        NavigationRail {
                            primaryTabs.forEach { tab ->
                                val selected = currentDest?.hierarchy?.any { it.route == tab.route } == true
                                val label = stringResource(tab.labelRes)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                            maxLines = 1
                                        )
                                    }
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.ROUTE_HOME,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(AppRoutes.ROUTE_HOME) {
                            HomeScreen(
                                state = state,
                                isSyncing = isSyncing,
                                onManualSync = viewModel::manualSync,
                                onNavigateToHtml = { navigateToTab(AppRoutes.ROUTE_HTML_LAB) },
                                onNavigateToC = { navigateToTab(AppRoutes.ROUTE_C_LAB) },
                                onNavigateToExam = { navController.navigate(AppRoutes.ROUTE_EXAM) },
                                onNavigateToProgress = { navigateToTab(AppRoutes.ROUTE_PROGRESS) },
                                onNavigateToSettings = { navController.navigate(AppRoutes.ROUTE_SETTINGS) },
                                onNavigateToAdmin = { navController.navigate(AppRoutes.ROUTE_ADMIN) },
                                onUpdateInstructorPhoto = viewModel::updateInstructorPhoto,
                                onNavigateToAlgoFlow = { navController.navigate(AppRoutes.ROUTE_ALGO_FLOW) },
                                onNavigateToCalculator = { navController.navigate(AppRoutes.ROUTE_CALCULATOR) },
                                onNavigateToFx100 = { navController.navigate(AppRoutes.ROUTE_CALC_FX100) },
                                onNavigateToFx991 = { navController.navigate(AppRoutes.ROUTE_CALC_FX991) },
                                onNavigateToHtmlPlayground = { navController.navigate(AppRoutes.ROUTE_HTML_PLAYGROUND) }
                            )
                        }

                        composable(AppRoutes.ROUTE_HTML_LAB) {
                            HtmlLabScreen(
                                state = state,
                                selectedProblemId = htmlProblemId,
                                codeValue = htmlCode,
                                checkResult = htmlCheck,
                                autoCloseSuggestion = autoClose,
                                onSelectProblem = viewModel::selectHtmlProblem,
                                onCodeChange = viewModel::updateHtmlCode,
                                onInsertSymbol = viewModel::insertHtmlSymbol,
                                onApplyAutoClose = viewModel::applyAutoCloseTag,
                                onCheckSolution = viewModel::checkHtmlSolution,
                                onResetStarter = viewModel::resetHtmlStarter,
                                onLoadSolution = viewModel::loadHtmlSolution,
                                onBookmark = { prob, code ->
                                    viewModel.bookmarkCurrentCode(prob.titleBn, "HTML", code, prob.hintBn)
                                },
                                onAskDoubt = { code, err ->
                                    viewModel.prepareSupportWithCode("HTML", code, err)
                                    navigateToTab(AppRoutes.ROUTE_SUPPORT)
                                },
                                onOpenPlayground = { navController.navigate(AppRoutes.ROUTE_HTML_PLAYGROUND) }
                            )
                        }

                        composable(AppRoutes.ROUTE_C_LAB) {
                            CLabScreen(
                                state = state,
                                selectedProblemId = cProblemId,
                                codeValue = cCode,
                                stdinValue = cStdin,
                                execResult = cResult,
                                isRunning = isRunningC,
                                preferCloudCompiler = preferCloud,
                                onSelectProblem = viewModel::selectCProblem,
                                onCodeChange = viewModel::updateCCode,
                                onInsertSymbol = viewModel::insertCSymbol,
                                onStdinChange = viewModel::updateCStdin,
                                onToggleCloudCompiler = viewModel::toggleCloudCompiler,
                                onRunProgram = viewModel::runCProgram,
                                onClearOutput = viewModel::clearCResult,
                                onResetStarter = viewModel::resetCStarter,
                                onLoadSolution = viewModel::loadCSolution,
                                onBookmark = { prob, code ->
                                    viewModel.bookmarkCurrentCode(prob.titleBn, "C", code, prob.hintBn)
                                },
                                onAskDoubt = { code, err ->
                                    viewModel.prepareSupportWithCode("C Programming", code, err)
                                    navigateToTab(AppRoutes.ROUTE_SUPPORT)
                                },
                                onOpenAlgoFlow = { navController.navigate(AppRoutes.ROUTE_ALGO_FLOW) }
                            )
                        }

                        composable(AppRoutes.ROUTE_PROGRESS) {
                            ProgressAndNotebookScreen(
                                state = state,
                                onLaunchExam = { navController.navigate(AppRoutes.ROUTE_EXAM) },
                                onOpenInEditor = { lang, code ->
                                    viewModel.openInEditorFromSaved(lang, code)
                                    navigateToTab(if (lang == "HTML") AppRoutes.ROUTE_HTML_LAB else AppRoutes.ROUTE_C_LAB)
                                },
                                onToggleMistakeResolved = viewModel::toggleMistakeResolved,
                                onDeleteMistake = viewModel::deleteMistake,
                                onDeleteBookmark = viewModel::deleteBookmark
                            )
                        }

                        composable(AppRoutes.ROUTE_SUPPORT) {
                            SupportScreen(
                                state = state,
                                prefill = supportPrefill,
                                onSubmitTicket = viewModel::submitTicket,
                                onUpdateInstructorPhoto = viewModel::updateInstructorPhoto,
                                onShowMessage = viewModel::showMessage
                            )
                        }

                        composable(AppRoutes.ROUTE_EXAM) {
                            ExamScreen(
                                onSaveAttempt = viewModel::recordQuizAttempt,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AppRoutes.ROUTE_ADMIN) {
                            AdminPanelScreen(
                                state = state,
                                onSetupAdminPin = viewModel::setupAdminPin,
                                onUnlockAdmin = viewModel::unlockAdmin,
                                onSwitchToStudent = viewModel::switchToStudentMode,
                                onSaveProblem = viewModel::adminSaveProblem,
                                onDeleteProblem = viewModel::adminDeleteProblem,
                                onReplyTicket = viewModel::adminReplyTicket,
                                onPublishNotice = viewModel::adminPublishNotice,
                                onDeleteNotice = viewModel::adminDeleteNotice,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AppRoutes.ROUTE_SETTINGS) {
                            SettingsScreen(
                                state = state,
                                onSetDarkMode = viewModel::setDarkMode,
                                onSetFontSize = viewModel::setEditorFontSize,
                                onSetEditorTheme = viewModel::setEditorTheme,
                                onSetStudentName = viewModel::setStudentName,
                                onSetAutoSync = viewModel::setAutoSync,
                                onManualSync = viewModel::manualSync,
                                onExportBackupJson = viewModel::exportBackupJson,
                                onImportBackupJson = viewModel::importBackupJson,
                                onShowMessage = viewModel::showMessage,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AppRoutes.ROUTE_ALGO_FLOW) {
                            AlgorithmFlowchartScreen(
                                onOpenInCLab = { code, stdin ->
                                    viewModel.openAlgoCodeInCLab(code, stdin)
                                    navController.popBackStack()
                                    navigateToTab(AppRoutes.ROUTE_C_LAB)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AppRoutes.ROUTE_CALCULATOR) {
                            AlgorithmFlowchartScreen(
                                initialTab = 2,
                                initialCalcMode = 2,
                                onOpenInCLab = { code, stdin ->
                                    viewModel.openAlgoCodeInCLab(code, stdin)
                                    navController.popBackStack()
                                    navigateToTab(AppRoutes.ROUTE_C_LAB)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AppRoutes.ROUTE_CALC_FX100) {
                            AlgorithmFlowchartScreen(
                                initialTab = 2,
                                initialCalcMode = 0,
                                onOpenInCLab = { code, stdin ->
                                    viewModel.openAlgoCodeInCLab(code, stdin)
                                    navController.popBackStack()
                                    navigateToTab(AppRoutes.ROUTE_C_LAB)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AppRoutes.ROUTE_CALC_FX991) {
                            AlgorithmFlowchartScreen(
                                initialTab = 2,
                                initialCalcMode = 1,
                                onOpenInCLab = { code, stdin ->
                                    viewModel.openAlgoCodeInCLab(code, stdin)
                                    navController.popBackStack()
                                    navigateToTab(AppRoutes.ROUTE_C_LAB)
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(AppRoutes.ROUTE_HTML_PLAYGROUND) {
                            HtmlPlaygroundScreen(
                                codeValue = playgroundHtmlCode,
                                fontSizeSp = state.settings.editorFontSizeSp,
                                themeMode = state.settings.editorTheme,
                                autoCloseSuggestion = playgroundAutoClose,
                                onCodeChange = viewModel::updatePlaygroundHtmlCode,
                                onInsertSymbol = viewModel::insertPlaygroundHtmlSymbol,
                                onApplyAutoClose = viewModel::applyPlaygroundAutoCloseTag,
                                onLoadTemplate = viewModel::loadPlaygroundHtmlTemplate,
                                onClearCode = viewModel::clearPlaygroundHtml,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
