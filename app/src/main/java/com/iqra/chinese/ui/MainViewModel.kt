package com.iqra.chinese.ui
import com.iqra.chinese.firebase.FirebaseManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import androidx.lifecycle.*
import androidx.lifecycle.asLiveData
import com.iqra.chinese.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class MainViewModel(val repo: Repository) : ViewModel() {

    private val _xp     = MutableLiveData(repo.prefs.xp)
    private val _streak = MutableLiveData(repo.prefs.streak)
    private val _daily  = MutableLiveData(repo.prefs.dailySecs)
    private val _ach    = MutableLiveData<Achievement?>(null)

    val xp:     LiveData<Int>          = _xp
    val streak: LiveData<Int>          = _streak
    val daily:  LiveData<Int>          = _daily
    val ach:    LiveData<Achievement?> = _ach

    val showPinyin:  Boolean get() = repo.prefs.showPinyin
    val showMeaning: Boolean get() = repo.prefs.showMeaning
    val unlocked:    List<Int> get() = repo.prefs.unlocked
    val bestTest:    Int       get() = repo.prefs.bestTest
    val prefs get() = repo.prefs

    private var timerJob: Job? = null

    fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                repo.tickDaily()
                _xp.postValue(repo.prefs.xp)
                _streak.postValue(repo.prefs.streak)
                _daily.postValue(repo.prefs.dailySecs)
                // Once a day: push backup to Firebase (no-ops if already done today / not logged in)
                repo.maybeBackupToFirebase()
            }
        }
    }

    fun stopTimer() = timerJob?.cancel()

    fun record(id: String, type: String, level: Int, passed: Boolean) = viewModelScope.launch {
        repo.record(id, type, level, passed)
        _xp.postValue(repo.prefs.xp)
        val mastered = repo.masteredCount().first()
        val news = repo.checkAchievements(mastered)
        if (news.isNotEmpty()) {
            _ach.postValue(news.first())
            delay(3000)
            _ach.postValue(null)
        }
    }

    fun unlockLevel(l: Int) {
        repo.prefs.unlock(l)
        FirebaseManager.logLevelUnlocked(l)
        viewModelScope.launch {
            val mastered = repo.masteredCount().first()
            repo.checkAchievements(mastered)
        }
    }

    /** Force an immediate Firebase backup (called from Settings "Sync now" button). */
    fun manualBackup() = viewModelScope.launch { repo.maybeBackupToFirebase() }

    /**
     * Restore cloud stats + attempts to local device after sign-in (handles
     * reinstall + relogin case). [onDone] receives true if data was restored.
     */
    fun restoreFromCloud(onDone: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val restored = FirebaseManager.restoreStats(repo.prefs, repo.attemptDao)
        refresh()
        onDone(restored)
    }

    fun setShowPinyin(v: Boolean)  { repo.prefs.showPinyin = v }
    fun setShowMeaning(v: Boolean) { repo.prefs.showMeaning = v }
    fun setBestTest(pct: Int)      { if (pct > repo.prefs.bestTest) repo.prefs.bestTest = pct }
    fun createGroup(name: String)  = viewModelScope.launch { repo.createGroup(name) }
    fun addWordToGroup(g: String, id: String) = viewModelScope.launch { repo.addWordToGroup(g, id) }
    fun deleteGroup(g: StudyGroup) = viewModelScope.launch { repo.deleteGroup(g) }
    fun wordsForGroup(g: StudyGroup) = repo.wordsForGroup(g)
    fun resetProgress() = viewModelScope.launch { repo.resetProgress(); refresh() }
    fun resetAll()      = viewModelScope.launch { repo.resetAll(); refresh() }

    private fun refresh() {
        _xp.postValue(repo.prefs.xp)
        _streak.postValue(repo.prefs.streak)
        _daily.postValue(0)
    }

    fun allAttempts() = repo.allAttempts()
    fun masteryForLevel(l: Int) = repo.masteryForLevel(l)
    fun masteredCount() = repo.masteredCount()
    fun allGroups() = repo.allGroups()
    val groupsLive: LiveData<List<StudyGroup>> get() = repo.allGroups().asLiveData()
    fun attemptsForLevel(l: Int) = repo.attemptsForLevel(l)
}

class VMFactory(private val repo: Repository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(c: Class<T>) = MainViewModel(repo) as T
}
