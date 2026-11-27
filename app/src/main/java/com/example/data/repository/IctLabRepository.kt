package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.CurriculumSeed
import com.example.data.model.ActivityLogEntity
import com.example.data.model.BookmarkEntity
import com.example.data.model.CodeDraftEntity
import com.example.data.model.EditorThemeMode
import com.example.data.model.MistakeEntryEntity
import com.example.data.model.NoticeEntity
import com.example.data.model.PracticeProblemEntity
import com.example.data.model.QuizAttemptEntity
import com.example.data.model.SupportTicketEntity
import com.example.data.model.SyncStatus
import com.example.data.model.TicketStatus
import com.example.data.model.UserProgressEntity
import com.example.data.network.CloudCompilerClient
import com.example.data.network.NetworkMonitor
import com.example.data.network.WandboxCompileRequest
import com.example.data.prefs.UserPreferencesRepository
import com.example.data.prefs.UserSettings
import com.example.engine.CExecutionResult
import com.example.engine.CLocalEngine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class IctLabRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val problemDao = db.practiceProblemDao()
    private val progressDao = db.progressDao()
    private val activityDao = db.activityLogDao()
    private val bookmarkDao = db.bookmarkDao()
    private val mistakeDao = db.mistakeDao()
    private val ticketDao = db.supportTicketDao()
    private val noticeDao = db.noticeDao()
    private val quizDao = db.quizAttemptDao()
    private val draftDao = db.codeDraftDao()

    val prefs = UserPreferencesRepository(context)
    val networkMonitor = NetworkMonitor(context)

    val problemsFlow: Flow<List<PracticeProblemEntity>> = problemDao.observeAllProblems()
    val progressFlow: Flow<List<UserProgressEntity>> = progressDao.observeAllProgress()
    val activityLogsFlow: Flow<List<ActivityLogEntity>> = activityDao.observeRecentLogs()
    val bookmarksFlow: Flow<List<BookmarkEntity>> = bookmarkDao.observeBookmarks()
    val mistakesFlow: Flow<List<MistakeEntryEntity>> = mistakeDao.observeMistakes()
    val ticketsFlow: Flow<List<SupportTicketEntity>> = ticketDao.observeTickets()
    val noticesFlow: Flow<List<NoticeEntity>> = noticeDao.observeNotices()
    val quizAttemptsFlow: Flow<List<QuizAttemptEntity>> = quizDao.observeAttempts()
    val draftsFlow: Flow<List<CodeDraftEntity>> = draftDao.observeAllDrafts()
    val settingsFlow: Flow<UserSettings> = prefs.settingsFlow
    val isOnlineFlow: Flow<Boolean> = networkMonitor.isOnlineFlow

    suspend fun ensureSeeded() = withContext(Dispatchers.IO) {
        if (problemDao.countProblems() == 0) {
            problemDao.insertAll(CurriculumSeed.initialProblems)
        }
        if (noticeDao.countNotices() == 0) {
            noticeDao.insertAll(CurriculumSeed.initialNotices)
        }
    }

    suspend fun getSavedCodeAndStdin(problemId: String): Pair<String, String?>? = withContext(Dispatchers.IO) {
        val draft = draftDao.getDraftForProblem(problemId)
        if (draft != null && draft.code.isNotBlank()) {
            return@withContext draft.code to draft.stdin.takeIf { it.isNotEmpty() }
        }
        val progress = progressDao.getProgressForProblem(problemId)
        if (progress != null && progress.lastCode.isNotBlank()) {
            return@withContext progress.lastCode to null
        }
        null
    }

    suspend fun saveCodeDraft(
        problemId: String,
        language: String,
        code: String,
        stdin: String = ""
    ) = withContext(Dispatchers.IO) {
        draftDao.upsertDraft(
            CodeDraftEntity(
                problemId = problemId,
                language = language,
                code = code,
                stdin = stdin,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearCodeDraft(problemId: String) = withContext(Dispatchers.IO) {
        draftDao.deleteDraft(problemId)
    }

    suspend fun recordPracticeAttempt(
        problem: PracticeProblemEntity,
        code: String,
        passed: Boolean,
        accuracy: Int
    ) = withContext(Dispatchers.IO) {
        val existing = progressDao.getProgressForProblem(problem.id)
        val wasCompleted = existing?.completed == true
        val nowCompleted = wasCompleted || passed
        val updated = UserProgressEntity(
            problemId = problem.id,
            language = problem.language,
            moduleKey = problem.moduleKey,
            completed = nowCompleted,
            attempts = (existing?.attempts ?: 0) + 1,
            bestAccuracy = maxOf(existing?.bestAccuracy ?: 0, accuracy),
            lastCode = code,
            updatedAt = System.currentTimeMillis(),
            synced = networkMonitor.isCurrentlyOnline()
        )
        progressDao.upsertProgress(updated)

        val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val existingLog = activityDao.getLogForDate(todayKey)
        val newlySolved = if (!wasCompleted && nowCompleted) 1 else 0
        activityDao.upsertLog(
            ActivityLogEntity(
                dateKey = todayKey,
                problemsSolved = (existingLog?.problemsSolved ?: 0) + newlySolved,
                runsCount = (existingLog?.runsCount ?: 0) + 1,
                accuracySum = (existingLog?.accuracySum ?: 0) + accuracy,
                accuracySamples = (existingLog?.accuracySamples ?: 0) + 1,
                synced = networkMonitor.isCurrentlyOnline()
            )
        )
    }

    suspend fun runCProgram(
        code: String,
        stdin: String,
        expectedOutput: String?,
        preferCloud: Boolean
    ): CExecutionResult = withContext(Dispatchers.IO) {
        val localResult = CLocalEngine.execute(code, stdin, expectedOutput)
        if (!preferCloud || !networkMonitor.isCurrentlyOnline()) {
            return@withContext localResult
        }
        return@withContext try {
            val response = CloudCompilerClient.api.compileC(
                WandboxCompileRequest(code = code, stdin = stdin)
            )
            val compErr = response.compilerError?.trim().orEmpty()
            val progErr = response.programError?.trim().orEmpty()
            if (compErr.isNotEmpty()) {
                CExecutionResult(
                    success = false,
                    output = compErr,
                    executionMode = "Cloud GCC",
                    error = CLocalEngine.translateCompilerError(compErr),
                    warningsBn = localResult.warningsBn
                )
            } else {
                val out = listOfNotNull(
                    response.programOutput?.trimEnd('\n'),
                    progErr.takeIf { it.isNotEmpty() }
                ).joinToString("\n").ifBlank { "(কোনো আউটপুট নেই)" }
                val testPassed = expectedOutput?.takeIf { it.isNotBlank() }?.let { exp ->
                    out.lines().joinToString("\n") { it.trim() }.trim() ==
                        exp.lines().joinToString("\n") { it.trim() }.trim()
                }
                CExecutionResult(
                    success = true,
                    output = out,
                    executionMode = "Cloud GCC",
                    warningsBn = localResult.warningsBn,
                    dryRunSteps = localResult.dryRunSteps,
                    passedTestCase = testPassed
                )
            }
        } catch (_: Exception) {
            // Graceful fallback to offline engine if cloud compiler times out or is unreachable
            localResult.copy(
                warningsBn = localResult.warningsBn + "ক্লাউড কম্পাইলার সাড়া দেয়নি—অফলাইন C ইঞ্জিনে রান করা হয়েছে।"
            )
        }
    }

    suspend fun addBookmark(title: String, language: String, code: String, noteBn: String) =
        withContext(Dispatchers.IO) {
            bookmarkDao.upsertBookmark(
                BookmarkEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    language = language,
                    code = code,
                    noteBn = noteBn
                )
            )
        }

    suspend fun deleteBookmark(id: String) = withContext(Dispatchers.IO) {
        bookmarkDao.deleteBookmark(id)
    }

    suspend fun recordMistake(
        language: String,
        title: String,
        brokenCode: String,
        errorTitleBn: String,
        explanationBn: String,
        fixTipBn: String
    ) = withContext(Dispatchers.IO) {
        mistakeDao.upsertMistake(
            MistakeEntryEntity(
                id = UUID.randomUUID().toString(),
                language = language,
                title = title,
                brokenCode = brokenCode,
                errorTitleBn = errorTitleBn,
                explanationBn = explanationBn,
                fixTipBn = fixTipBn
            )
        )
    }

    suspend fun toggleMistakeResolved(id: String, resolved: Boolean) = withContext(Dispatchers.IO) {
        mistakeDao.setResolved(id, resolved)
    }

    suspend fun deleteMistake(id: String) = withContext(Dispatchers.IO) {
        mistakeDao.deleteMistake(id)
    }

    suspend fun submitSupportTicket(
        studentName: String,
        subject: String,
        category: String,
        message: String,
        codeSnippet: String,
        errorMessage: String,
        attachmentUri: String?
    ) = withContext(Dispatchers.IO) {
        val online = networkMonitor.isCurrentlyOnline()
        ticketDao.upsertTicket(
            SupportTicketEntity(
                id = UUID.randomUUID().toString(),
                studentName = studentName,
                subject = subject,
                category = category,
                message = message,
                codeSnippet = codeSnippet,
                errorMessage = errorMessage,
                attachmentUri = attachmentUri,
                status = TicketStatus.PENDING.name,
                syncStatus = if (online) SyncStatus.SYNCED.name else SyncStatus.QUEUED_OFFLINE.name
            )
        )
    }

    suspend fun adminReplyToTicket(ticketId: String, reply: String, status: TicketStatus) =
        withContext(Dispatchers.IO) {
            ticketDao.replyToTicket(
                id = ticketId,
                reply = reply,
                status = status.name,
                updatedAt = System.currentTimeMillis()
            )
        }

    suspend fun adminSaveProblem(problem: PracticeProblemEntity) = withContext(Dispatchers.IO) {
        problemDao.upsertProblem(problem)
    }

    suspend fun adminDeleteProblem(id: String) = withContext(Dispatchers.IO) {
        problemDao.deleteProblemById(id)
    }

    suspend fun adminPublishNotice(title: String, body: String, tag: String, pinned: Boolean) =
        withContext(Dispatchers.IO) {
            noticeDao.upsertNotice(
                NoticeEntity(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    body = body,
                    tag = tag,
                    pinned = pinned
                )
            )
        }

    suspend fun adminDeleteNotice(id: String) = withContext(Dispatchers.IO) {
        noticeDao.deleteNotice(id)
    }

    suspend fun saveQuizAttempt(category: String, score: Int, total: Int, durationSec: Int) =
        withContext(Dispatchers.IO) {
            val accuracy = if (total > 0) ((score.toFloat() / total) * 100).toInt() else 0
            quizDao.insertAttempt(
                QuizAttemptEntity(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    score = score,
                    total = total,
                    accuracyPercent = accuracy,
                    durationSec = durationSec,
                    synced = networkMonitor.isCurrentlyOnline()
                )
            )
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val existingLog = activityDao.getLogForDate(todayKey)
            activityDao.upsertLog(
                ActivityLogEntity(
                    dateKey = todayKey,
                    problemsSolved = existingLog?.problemsSolved ?: 0,
                    runsCount = (existingLog?.runsCount ?: 0) + 1,
                    accuracySum = (existingLog?.accuracySum ?: 0) + accuracy,
                    accuracySamples = (existingLog?.accuracySamples ?: 0) + 1,
                    synced = networkMonitor.isCurrentlyOnline()
                )
            )
        }

    suspend fun syncPendingData(): Boolean = withContext(Dispatchers.IO) {
        if (!networkMonitor.isCurrentlyOnline()) return@withContext false
        progressDao.markAllSynced()
        activityDao.markAllSynced()
        ticketDao.markQueuedTicketsSynced()
        quizDao.markAllSynced()
        prefs.updateLastSyncedAt(System.currentTimeMillis())
        true
    }

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        val progressArr = JSONArray()
        progressDao.getAllProgressSnapshot().forEach { p ->
            progressArr.put(JSONObject().apply {
                put("problemId", p.problemId)
                put("language", p.language)
                put("moduleKey", p.moduleKey)
                put("completed", p.completed)
                put("attempts", p.attempts)
                put("bestAccuracy", p.bestAccuracy)
                put("lastCode", p.lastCode)
                put("updatedAt", p.updatedAt)
            })
        }
        val bookmarkArr = JSONArray()
        bookmarkDao.getAllBookmarksSnapshot().forEach { b ->
            bookmarkArr.put(JSONObject().apply {
                put("id", b.id)
                put("title", b.title)
                put("language", b.language)
                put("code", b.code)
                put("noteBn", b.noteBn)
                put("createdAt", b.createdAt)
            })
        }
        val mistakeArr = JSONArray()
        mistakeDao.getAllMistakesSnapshot().forEach { m ->
            mistakeArr.put(JSONObject().apply {
                put("id", m.id)
                put("language", m.language)
                put("title", m.title)
                put("brokenCode", m.brokenCode)
                put("errorTitleBn", m.errorTitleBn)
                put("explanationBn", m.explanationBn)
                put("fixTipBn", m.fixTipBn)
                put("resolved", m.resolved)
                put("createdAt", m.createdAt)
            })
        }
        val draftArr = JSONArray()
        draftDao.getAllDraftsSnapshot().forEach { d ->
            draftArr.put(JSONObject().apply {
                put("problemId", d.problemId)
                put("language", d.language)
                put("code", d.code)
                put("stdin", d.stdin)
                put("updatedAt", d.updatedAt)
            })
        }
        root.put("version", 2)
        root.put("exportedAt", System.currentTimeMillis())
        root.put("progress", progressArr)
        root.put("bookmarks", bookmarkArr)
        root.put("mistakes", mistakeArr)
        root.put("drafts", draftArr)
        root.toString(2)
    }

    suspend fun importBackupJson(jsonText: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val root = JSONObject(jsonText)
            var restored = 0
            val pArr = root.optJSONArray("progress") ?: JSONArray()
            val progressList = buildList {
                for (i in 0 until pArr.length()) {
                    val o = pArr.getJSONObject(i)
                    add(
                        UserProgressEntity(
                            problemId = o.getString("problemId"),
                            language = o.optString("language", "C"),
                            moduleKey = o.optString("moduleKey", ""),
                            completed = o.optBoolean("completed", false),
                            attempts = o.optInt("attempts", 1),
                            bestAccuracy = o.optInt("bestAccuracy", 0),
                            lastCode = o.optString("lastCode", ""),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            if (progressList.isNotEmpty()) {
                progressDao.insertAllProgress(progressList)
                restored += progressList.size
            }
            val bArr = root.optJSONArray("bookmarks") ?: JSONArray()
            val bookmarkList = buildList {
                for (i in 0 until bArr.length()) {
                    val o = bArr.getJSONObject(i)
                    add(
                        BookmarkEntity(
                            id = o.getString("id"),
                            title = o.optString("title", "Bookmark"),
                            language = o.optString("language", "HTML"),
                            code = o.optString("code", ""),
                            noteBn = o.optString("noteBn", ""),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            if (bookmarkList.isNotEmpty()) {
                bookmarkDao.insertAllBookmarks(bookmarkList)
                restored += bookmarkList.size
            }
            val mArr = root.optJSONArray("mistakes") ?: JSONArray()
            val mistakeList = buildList {
                for (i in 0 until mArr.length()) {
                    val o = mArr.getJSONObject(i)
                    add(
                        MistakeEntryEntity(
                            id = o.getString("id"),
                            language = o.optString("language", "C"),
                            title = o.optString("title", "Mistake"),
                            brokenCode = o.optString("brokenCode", ""),
                            errorTitleBn = o.optString("errorTitleBn", ""),
                            explanationBn = o.optString("explanationBn", ""),
                            fixTipBn = o.optString("fixTipBn", ""),
                            resolved = o.optBoolean("resolved", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            if (mistakeList.isNotEmpty()) {
                mistakeDao.insertAllMistakes(mistakeList)
                restored += mistakeList.size
            }
            val dArr = root.optJSONArray("drafts") ?: JSONArray()
            val draftList = buildList {
                for (i in 0 until dArr.length()) {
                    val o = dArr.getJSONObject(i)
                    add(
                        CodeDraftEntity(
                            problemId = o.getString("problemId"),
                            language = o.optString("language", "C"),
                            code = o.optString("code", ""),
                            stdin = o.optString("stdin", ""),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
            }
            if (draftList.isNotEmpty()) {
                draftDao.insertAllDrafts(draftList)
                restored += draftList.size
            }
            restored
        }
    }

    suspend fun setDarkMode(enabled: Boolean) = prefs.setDarkMode(enabled)
    suspend fun setEditorFontSize(sizeSp: Int) = prefs.setEditorFontSize(sizeSp)
    suspend fun setEditorTheme(theme: EditorThemeMode) = prefs.setEditorTheme(theme)
    suspend fun setStudentName(name: String) = prefs.setStudentName(name)
    suspend fun setAutoSync(enabled: Boolean) = prefs.setAutoSync(enabled)
}
