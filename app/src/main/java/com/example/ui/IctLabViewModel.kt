package com.example.ui

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CurriculumSeed
import com.example.data.model.ActivityLogEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.EditorThemeMode
import com.example.data.model.MistakeEntryEntity
import com.example.data.model.NoticeEntity
import com.example.data.model.PracticeProblemEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.SyncStatus
import com.example.data.model.TicketStatus
import com.example.data.model.UserProgressEntity
import com.example.data.prefs.UserSettings
import com.example.data.repository.IctLabRepository
import com.example.engine.CExecutionResult
import com.example.engine.HtmlAnalyzer
import com.example.engine.HtmlCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SupportPrefill(
    val category: String = "C Programming",
    val codeSnippet: String = "",
    val errorMessage: String = ""
)

data class IctLabUiState(
    val problems: List<PracticeProblemEntity> = emptyList(),
    val progressMap: Map<String, UserProgressEntity> = emptyMap(),
    val activityLogs: List<ActivityLogEntity> = emptyList(),
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val mistakes: List<MistakeEntryEntity> = emptyList(),
    val tickets: List<SupportTicketEntity> = emptyList(),
    val notices: List<NoticeEntity> = emptyList(),
    val quizAttempts: List<QuizAttemptEntity> = emptyList(),
    val settings: UserSettings = UserSettings(),
    val isOnline: Boolean = false,
    val queuedSyncCount: Int = 0
)

class IctLabViewModel(private val repo: IctLabRepository) : ViewModel() {

    private val coreBundle = combine(
        repo.problemsFlow,
        repo.progressFlow,
        repo.activityLogsFlow,
        repo.bookmarksFlow,
        repo.mistakesFlow
    ) { problems, progress, logs, bookmarks, mistakes ->
        CoreData(problems, progress, logs, bookmarks, mistakes)
    }

    private val secondaryBundle = combine(
        repo.ticketsFlow,
        repo.noticesFlow,
        repo.quizAttemptsFlow,
        repo.settingsFlow,
        repo.isOnlineFlow
    ) { tickets, notices, quizzes, settings, isOnline ->
        SecondaryData(tickets, notices, quizzes, settings, isOnline)
    }

    val uiState: StateFlow<IctLabUiState> = combine(coreBundle, secondaryBundle) { core, sec ->
        val queuedCount = sec.tickets.count { it.syncStatus == SyncStatus.QUEUED_OFFLINE.name } +
            core.progress.count { !it.synced } +
            core.logs.count { !it.synced }

        IctLabUiState(
            problems = core.problems,
            progressMap = core.progress.associateBy { it.problemId },
            activityLogs = core.logs,
            bookmarks = core.bookmarks,
            mistakes = core.mistakes,
            tickets = sec.tickets,
            notices = sec.notices,
            quizAttempts = sec.quizzes,
            settings = sec.settings,
            isOnline = sec.isOnline,
            queuedSyncCount = queuedCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = IctLabUiState()
    )

    // HTML Lab Editor State
    private val _selectedHtmlProblemId = MutableStateFlow("html_basic_1")
    val selectedHtmlProblemId: StateFlow<String> = _selectedHtmlProblemId.asStateFlow()

    private val _htmlCodeValue = MutableStateFlow(
        TextFieldValue(CurriculumSeed.initialProblems.first { it.language == "HTML" }.starterCode)
    )
    val htmlCodeValue: StateFlow<TextFieldValue> = _htmlCodeValue.asStateFlow()

    private val _htmlCheckResult = MutableStateFlow<HtmlCheckResult?>(null)
    val htmlCheckResult: StateFlow<HtmlCheckResult?> = _htmlCheckResult.asStateFlow()

    private val _autoCloseSuggestion = MutableStateFlow<Pair<String, String>?>(null)
    val autoCloseSuggestion: StateFlow<Pair<String, String>?> = _autoCloseSuggestion.asStateFlow()

    // C Lab Editor State
    private val _selectedCProblemId = MutableStateFlow("c_io_1")
    val selectedCProblemId: StateFlow<String> = _selectedCProblemId.asStateFlow()

    private val _cCodeValue = MutableStateFlow(
        TextFieldValue(CurriculumSeed.initialProblems.first { it.language == "C" }.starterCode)
    )
    val cCodeValue: StateFlow<TextFieldValue> = _cCodeValue.asStateFlow()

    private val _cStdin = MutableStateFlow("")
    val cStdin: StateFlow<String> = _cStdin.asStateFlow()

    private val _cResult = MutableStateFlow<CExecutionResult?>(null)
    val cResult: StateFlow<CExecutionResult?> = _cResult.asStateFlow()

    private val _isRunningC = MutableStateFlow(false)
    val isRunningC: StateFlow<Boolean> = _isRunningC.asStateFlow()

    private val _preferCloudCompiler = MutableStateFlow(false)
    val preferCloudCompiler: StateFlow<Boolean> = _preferCloudCompiler.asStateFlow()

    // Support Prefill & Sync & Snackbars
    private val _supportPrefill = MutableStateFlow(SupportPrefill())
    val supportPrefill: StateFlow<SupportPrefill> = _supportPrefill.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
        }
        viewModelScope.launch {
            combine(repo.isOnlineFlow, repo.settingsFlow) { online, settings ->
                online && settings.autoSyncEnabled
            }.distinctUntilChanged().collect { shouldSync ->
                if (shouldSync) {
                    repo.syncPendingData()
                }
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    // ==================== HTML LAB ACTIONS ====================
    fun selectHtmlProblem(problem: PracticeProblemEntity) {
        _selectedHtmlProblemId.value = problem.id
        val saved = uiState.value.progressMap[problem.id]?.lastCode?.takeIf { it.isNotBlank() }
        val text = saved ?: problem.starterCode
        _htmlCodeValue.value = TextFieldValue(text, TextRange(text.length))
        _htmlCheckResult.value = null
        _autoCloseSuggestion.value = null
    }

    fun updateHtmlCode(value: TextFieldValue) {
        val oldText = _htmlCodeValue.value.text
        _htmlCodeValue.value = value
        if (value.text.length == oldText.length + 1 && value.selection.start > 0) {
            val typedChar = value.text.getOrNull(value.selection.start - 1)
            if (typedChar == '>') {
                val prefix = value.text.substring(0, value.selection.start)
                _autoCloseSuggestion.value = HtmlAnalyzer.autoCloseSuggestion(prefix)
            } else {
                _autoCloseSuggestion.value = null
            }
        } else {
            _autoCloseSuggestion.value = null
        }
    }

    fun applyAutoCloseTag() {
        val suggestion = _autoCloseSuggestion.value ?: return
        val cur = _htmlCodeValue.value
        val pos = cur.selection.start.coerceIn(0, cur.text.length)
        val newText = cur.text.substring(0, pos) + suggestion.second + cur.text.substring(pos)
        _htmlCodeValue.value = TextFieldValue(newText, TextRange(pos))
        _autoCloseSuggestion.value = null
    }

    fun insertHtmlSymbol(symbol: String) {
        _htmlCodeValue.update { insertAtCursor(it, symbol) }
    }

    fun resetHtmlStarter(problem: PracticeProblemEntity) {
        _htmlCodeValue.value = TextFieldValue(problem.starterCode, TextRange(problem.starterCode.length))
        _htmlCheckResult.value = null
    }

    fun loadHtmlSolution(problem: PracticeProblemEntity) {
        _htmlCodeValue.value = TextFieldValue(problem.solutionCode, TextRange(problem.solutionCode.length))
    }

    fun checkHtmlSolution(problem: PracticeProblemEntity) {
        val code = _htmlCodeValue.value.text
        val res = HtmlAnalyzer.analyze(code, problem.requiredTagsCsv)
        _htmlCheckResult.value = res
        viewModelScope.launch {
            repo.recordPracticeAttempt(problem, code, res.passed, res.accuracyPercent)
            if (!res.passed && res.warningsBn.isNotEmpty()) {
                repo.recordMistake(
                    language = "HTML",
                    title = problem.titleBn,
                    brokenCode = code,
                    errorTitleBn = "HTML ট্যাগ/কাঠামো ত্রুটি",
                    explanationBn = res.warningsBn.first(),
                    fixTipBn = problem.commonMistakeBn
                )
            }
        }
    }

    // ==================== C LAB ACTIONS ====================
    fun selectCProblem(problem: PracticeProblemEntity) {
        _selectedCProblemId.value = problem.id
        val saved = uiState.value.progressMap[problem.id]?.lastCode?.takeIf { it.isNotBlank() }
        val text = saved ?: problem.starterCode
        _cCodeValue.value = TextFieldValue(text, TextRange(text.length))
        _cStdin.value = problem.testInput
        _cResult.value = null
    }

    fun updateCCode(value: TextFieldValue) {
        _cCodeValue.value = value
    }

    fun insertCSymbol(symbol: String) {
        _cCodeValue.update { insertAtCursor(it, symbol) }
    }

    fun updateCStdin(input: String) {
        _cStdin.value = input
    }

    fun toggleCloudCompiler(enabled: Boolean) {
        _preferCloudCompiler.value = enabled
    }

    fun resetCStarter(problem: PracticeProblemEntity) {
        _cCodeValue.value = TextFieldValue(problem.starterCode, TextRange(problem.starterCode.length))
        _cStdin.value = problem.testInput
        _cResult.value = null
    }

    fun loadCSolution(problem: PracticeProblemEntity) {
        _cCodeValue.value = TextFieldValue(problem.solutionCode, TextRange(problem.solutionCode.length))
    }

    fun clearCResult() {
        _cResult.value = null
    }

    fun runCProgram(problem: PracticeProblemEntity) {
        val code = _cCodeValue.value.text
        val stdin = _cStdin.value
        val preferCloud = _preferCloudCompiler.value
        viewModelScope.launch {
            _isRunningC.value = true
            val result = repo.runCProgram(
                code = code,
                stdin = stdin,
                expectedOutput = problem.expectedOutput.takeIf { it.isNotBlank() },
                preferCloud = preferCloud
            )
            _cResult.value = result
            _isRunningC.value = false

            val passed = result.success && (result.passedTestCase != false)
            val accuracy = when {
                !result.success -> 25
                result.passedTestCase == true -> 100
                result.passedTestCase == false -> 60
                else -> 90
            }
            repo.recordPracticeAttempt(problem, code, passed, accuracy)

            if (!result.success && result.error != null) {
                repo.recordMistake(
                    language = "C",
                    title = problem.titleBn,
                    brokenCode = code,
                    errorTitleBn = result.error.titleBn,
                    explanationBn = result.error.explanationBn,
                    fixTipBn = result.error.fixTipBn
                )
            }
        }
    }

    // ==================== BOOKMARKS & MISTAKES ====================
    fun bookmarkCurrentCode(title: String, language: String, code: String, noteBn: String) {
        viewModelScope.launch {
            repo.addBookmark(title, language, code, noteBn)
            _snackbarMessage.value = "বুকমার্কে সংরক্ষণ করা হয়েছে!"
        }
    }

    fun deleteBookmark(id: String) {
        viewModelScope.launch { repo.deleteBookmark(id) }
    }

    fun toggleMistakeResolved(id: String, resolved: Boolean) {
        viewModelScope.launch { repo.toggleMistakeResolved(id, resolved) }
    }

    fun deleteMistake(id: String) {
        viewModelScope.launch { repo.deleteMistake(id) }
    }

    fun openInEditorFromSaved(language: String, code: String) {
        if (language == "HTML") {
            _htmlCodeValue.value = TextFieldValue(code, TextRange(code.length))
        } else {
            _cCodeValue.value = TextFieldValue(code, TextRange(code.length))
        }
    }

    // ==================== SUPPORT & EXAM ====================
    fun prepareSupportWithCode(category: String, code: String, error: String) {
        _supportPrefill.value = SupportPrefill(category, code, error)
    }

    fun submitTicket(
        subject: String,
        category: String,
        message: String,
        codeSnippet: String,
        errorMessage: String,
        attachmentUri: String?
    ) {
        if (subject.isBlank() || message.isBlank()) {
            _snackbarMessage.value = "বিষয় ও বিস্তারিত সমস্যা লিখুন।"
            return
        }
        viewModelScope.launch {
            repo.submitSupportTicket(
                studentName = uiState.value.settings.studentName,
                subject = subject.trim(),
                category = category,
                message = message.trim(),
                codeSnippet = codeSnippet,
                errorMessage = errorMessage,
                attachmentUri = attachmentUri
            )
            _supportPrefill.value = SupportPrefill()
            _snackbarMessage.value = if (uiState.value.isOnline) {
                "সাপোর্ট টিকিট পাঠানো হয়েছে (Synced)!"
            } else {
                "অফলাইন কিউতে জমা হয়েছে—ইন্টারনেট পেলেই স্বয়ংক্রিয়ভাবে সিঙ্ক হবে!"
            }
        }
    }

    fun recordQuizAttempt(category: String, score: Int, total: Int, durationSec: Int) {
        viewModelScope.launch {
            repo.saveQuizAttempt(category, score, total, durationSec)
        }
    }

    // ==================== SYNC, SETTINGS, BACKUP & ADMIN ====================
    fun manualSync() {
        viewModelScope.launch {
            _isSyncing.value = true
            val ok = repo.syncPendingData()
            _isSyncing.value = false
            _snackbarMessage.value = if (ok) {
                "সব ডাটা ও টিকিট সফলভাবে সিঙ্ক হয়েছে!"
            } else {
                "এখন অফলাইন মোড চলছে—ইন্টারনেট সংযোগ পেলে সিঙ্ক হবে।"
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repo.setDarkMode(enabled) }
    }

    fun setEditorFontSize(sizeSp: Int) {
        viewModelScope.launch { repo.setEditorFontSize(sizeSp) }
    }

    fun setEditorTheme(theme: EditorThemeMode) {
        viewModelScope.launch { repo.setEditorTheme(theme) }
    }

    fun setStudentName(name: String) {
        viewModelScope.launch {
            repo.setStudentName(name)
            _snackbarMessage.value = "নাম আপডেট হয়েছে!"
        }
    }

    fun setAutoSync(enabled: Boolean) {
        viewModelScope.launch { repo.setAutoSync(enabled) }
    }

    fun updateInstructorPhoto(uri: android.net.Uri) {
        viewModelScope.launch {
            val saved = repo.prefs.saveInstructorPhotoFromUri(uri)
            _snackbarMessage.value = if (saved) {
                "আপনার ছবি সফলভাবে আপডেট করা হয়েছে!"
            } else {
                "ছবিটি লোড করা যায়নি, আবার চেষ্টা করুন।"
            }
        }
    }

    fun setupAdminPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repo.prefs.setupAndUnlockAdmin(pin)
            if (ok) _snackbarMessage.value = "অ্যাডমিন পিন সেট ও প্যানেল আনলক হয়েছে!"
            onResult(ok)
        }
    }

    fun unlockAdmin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = repo.prefs.verifyAndUnlockAdmin(pin)
            if (ok) _snackbarMessage.value = "অ্যাডমিন মোড চালু হয়েছে!"
            onResult(ok)
        }
    }

    fun switchToStudentMode() {
        viewModelScope.launch {
            repo.prefs.switchToStudent()
            _snackbarMessage.value = "স্টুডেন্ট মোডে ফিরে আসা হয়েছে।"
        }
    }

    fun adminSaveProblem(problem: PracticeProblemEntity) {
        viewModelScope.launch {
            repo.adminSaveProblem(problem)
            _snackbarMessage.value = "সমস্যা সংরক্ষণ করা হয়েছে!"
        }
    }

    fun adminDeleteProblem(id: String) {
        viewModelScope.launch {
            repo.adminDeleteProblem(id)
            _snackbarMessage.value = "সমস্যা মুছে ফেলা হয়েছে।"
        }
    }

    fun adminReplyTicket(ticketId: String, reply: String, status: TicketStatus) {
        viewModelScope.launch {
            repo.adminReplyToTicket(ticketId, reply.trim(), status)
            _snackbarMessage.value = "শিক্ষার্থীর প্রশ্নের উত্তর পাঠানো হয়েছে!"
        }
    }

    fun adminPublishNotice(title: String, body: String, tag: String, pinned: Boolean) {
        viewModelScope.launch {
            repo.adminPublishNotice(title.trim(), body.trim(), tag, pinned)
            _snackbarMessage.value = "নতুন নোটিশ প্রকাশিত হয়েছে!"
        }
    }

    fun adminDeleteNotice(id: String) {
        viewModelScope.launch { repo.adminDeleteNotice(id) }
    }

    suspend fun exportBackupJson(): String = repo.exportBackupJson()

    fun importBackupJson(json: String) {
        viewModelScope.launch {
            repo.importBackupJson(json)
                .onSuccess { count ->
                    _snackbarMessage.value = "সফলভাবে $count টি রেকর্ড রিস্টোর করা হয়েছে!"
                }
                .onFailure {
                    _snackbarMessage.value = "ব্যাকআপ ফাইলটি সঠিক ফরম্যাটে নেই।"
                }
        }
    }

    private fun insertAtCursor(current: TextFieldValue, insert: String): TextFieldValue {
        val start = current.selection.start.coerceIn(0, current.text.length)
        val end = current.selection.end.coerceIn(start, current.text.length)
        val newText = current.text.substring(0, start) + insert + current.text.substring(end)
        val cursor = start + insert.length
        return TextFieldValue(newText, TextRange(cursor))
    }

    private data class CoreData(
        val problems: List<PracticeProblemEntity>,
        val progress: List<UserProgressEntity>,
        val logs: List<ActivityLogEntity>,
        val bookmarks: List<BookmarkEntity>,
        val mistakes: List<MistakeEntryEntity>
    )

    private data class SecondaryData(
        val tickets: List<SupportTicketEntity>,
        val notices: List<NoticeEntity>,
        val quizzes: List<QuizAttemptEntity>,
        val settings: UserSettings,
        val isOnline: Boolean
    )
}
