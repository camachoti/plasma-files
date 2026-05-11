package com.camachoti.plasmafiles.ui.viewmodel

import android.app.Application
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camachoti.plasmafiles.data.model.FileItem
import com.camachoti.plasmafiles.data.model.FileKind
import com.camachoti.plasmafiles.data.model.StorageVolume
import com.camachoti.plasmafiles.data.model.formatDate
import com.camachoti.plasmafiles.data.model.formatSize
import com.camachoti.plasmafiles.data.repository.FileRepository
import com.camachoti.plasmafiles.ui.theme.AccentColor
import com.camachoti.plasmafiles.ui.theme.AppThemeMode
import com.camachoti.plasmafiles.ui.theme.PlasmaTheme
import com.camachoti.plasmafiles.ui.theme.buildTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

enum class ViewMode    { DETAILS, TILES }
enum class SortKey     { NAME, MODIFIED, SIZE }
enum class SortDir     { ASC, DESC }
enum class Density     { COMPACT, COZY, REGULAR }

data class Tab(val id: Int, val path: String)
data class NavHistory(val stack: List<String>, val index: Int)
data class Clipboard(val op: String, val items: List<String>) // items are absolute file paths

data class FileManagerUiState(
    val tabs: List<Tab> = emptyList(),
    val activeTabId: Int = 1,
    val histories: Map<Int, NavHistory> = emptyMap(),
    val items: List<FileItem> = emptyList(),
    val volumes: List<StorageVolume> = emptyList(),
    val selectedItems: Set<String> = emptySet(),
    val clipboard: Clipboard? = null,
    val view: ViewMode = ViewMode.DETAILS,
    val sortKey: SortKey = SortKey.NAME,
    val sortDir: SortDir = SortDir.ASC,
    val searchQuery: String = "",
    val isSearchOpen: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val propsItem: FileItem? = null,
    val toast: String? = null,
    val showRibbon: Boolean = true,
    val showFooter: Boolean = true,
    val density: Density = Density.REGULAR,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT,
    val accent: AccentColor = AccentColor.TEAL,
    val isLoading: Boolean = false,
) {
    val currentTab: Tab           get() = tabs.find { it.id == activeTabId } ?: tabs.firstOrNull() ?: Tab(1, "/")
    val currentHistory: NavHistory get() = histories[activeTabId] ?: NavHistory(listOf(currentTab.path), 0)
    val currentPath: String       get() = currentTab.path
    val theme: PlasmaTheme        get() = buildTheme(themeMode, accent)
    val canGoBack: Boolean        get() = currentHistory.index > 0
    val canGoForward: Boolean     get() = currentHistory.index < currentHistory.stack.size - 1
    val canGoUp: Boolean          get() = File(currentPath).parent != null
}

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = FileRepository(application)
    private val _state = MutableStateFlow(FileManagerUiState())
    val state: StateFlow<FileManagerUiState> = _state.asStateFlow()

    private val defaultPath: String
        get() = Environment.getExternalStorageDirectory().absolutePath

    init {
        val startPath = defaultPath
        _state.update { s ->
            s.copy(
                tabs = listOf(Tab(1, startPath)),
                histories = mapOf(1 to NavHistory(listOf(startPath), 0)),
            )
        }
        loadCurrentFolder()
        loadVolumes()
    }

    // ── Navigation ─────────────────────────────────────────────────────────────────────────────

    fun navigateTo(path: String) {
        val tabId = _state.value.activeTabId
        val hist  = _state.value.histories[tabId] ?: NavHistory(listOf(path), 0)
        val newStack = hist.stack.take(hist.index + 1) + path
        _state.update { s ->
            s.copy(
                tabs      = s.tabs.map { if (it.id == tabId) it.copy(path = path) else it },
                histories = s.histories + (tabId to NavHistory(newStack, newStack.size - 1)),
                selectedItems = emptySet(),
            )
        }
        loadCurrentFolder()
    }

    fun goBack() {
        val s = _state.value
        if (!s.canGoBack) return
        val hist   = s.currentHistory
        val newIdx = hist.index - 1
        val path   = hist.stack[newIdx]
        _state.update { it.copy(
            tabs      = it.tabs.map { t -> if (t.id == it.activeTabId) t.copy(path = path) else t },
            histories = it.histories + (it.activeTabId to hist.copy(index = newIdx)),
            selectedItems = emptySet(),
        )}
        loadCurrentFolder()
    }

    fun goForward() {
        val s = _state.value
        if (!s.canGoForward) return
        val hist   = s.currentHistory
        val newIdx = hist.index + 1
        val path   = hist.stack[newIdx]
        _state.update { it.copy(
            tabs      = it.tabs.map { t -> if (t.id == it.activeTabId) t.copy(path = path) else t },
            histories = it.histories + (it.activeTabId to hist.copy(index = newIdx)),
            selectedItems = emptySet(),
        )}
        loadCurrentFolder()
    }

    fun goUp() {
        File(_state.value.currentPath).parent?.let { navigateTo(it) }
    }

    // ── Tabs ───────────────────────────────────────────────────────────────────────────────

    fun switchTab(id: Int) {
        if (_state.value.activeTabId == id) return
        _state.update { it.copy(activeTabId = id, selectedItems = emptySet()) }
        loadCurrentFolder()
    }

    fun newTab(path: String = defaultPath) {
        val newId = (_state.value.tabs.maxOfOrNull { it.id } ?: 0) + 1
        _state.update { s ->
            s.copy(
                tabs        = s.tabs + Tab(newId, path),
                histories   = s.histories + (newId to NavHistory(listOf(path), 0)),
                activeTabId = newId,
                selectedItems = emptySet(),
            )
        }
        loadCurrentFolder()
    }

    fun closeTab(id: Int) {
        val s = _state.value
        if (s.tabs.size <= 1) return
        val newTabs   = s.tabs.filter { it.id != id }
        val newActive = if (s.activeTabId == id) newTabs.first().id else s.activeTabId
        _state.update { it.copy(
            tabs        = newTabs,
            activeTabId = newActive,
            histories   = it.histories - id,
        )}
        if (s.activeTabId == id) loadCurrentFolder()
    }

    // ── Content loading ───────────────────────────────────────────────────────────────

    fun refresh() = loadCurrentFolder()

    private fun loadCurrentFolder() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val path = _state.value.currentPath
            val raw  = repo.listFiles(path)
            val s    = _state.value
            val filtered = if (s.searchQuery.isNotEmpty())
                raw.filter { it.name.contains(s.searchQuery, ignoreCase = true) }
            else raw
            _state.update { it.copy(items = sort(filtered, it.sortKey, it.sortDir), isLoading = false) }
        }
    }

    private fun loadVolumes() {
        _state.update { it.copy(volumes = repo.getStorageVolumes()) }
    }

    private fun sort(items: List<FileItem>, key: SortKey, dir: SortDir): List<FileItem> {
        val folders = items.filter { it.kind == FileKind.FOLDER }
        val files   = items.filter { it.kind != FileKind.FOLDER }
        val cmp: Comparator<FileItem> = when (key) {
            SortKey.NAME     -> compareBy { it.name.lowercase() }
            SortKey.MODIFIED -> compareBy { it.lastModified }
            SortKey.SIZE     -> compareBy { it.size }
        }
        fun List<FileItem>.s() = if (dir == SortDir.ASC) sortedWith(cmp) else sortedWith(cmp).reversed()
        return folders.s() + files.s()
    }

    // ── Selection ─────────────────────────────────────────────────────────────────────────

    fun toggleSelection(name: String) {
        _state.update {
            val sel = it.selectedItems.toMutableSet()
            if (name in sel) sel.remove(name) else sel.add(name)
            it.copy(selectedItems = sel)
        }
    }

    fun clearSelection() = _state.update { it.copy(selectedItems = emptySet()) }

    fun selectAll() = _state.update {
        it.copy(selectedItems = it.items.map { i -> i.name }.toSet())
    }

    // ── Clipboard ───────────────────────────────────────────────────────────────────────────

    fun copySelected() {
        val currentPath = _state.value.currentPath
        val sel = _state.value.selectedItems.map { File(currentPath, it).absolutePath }
        _state.update { it.copy(clipboard = Clipboard("copy", sel)) }
        showToast("Copied ${sel.size} item(s)")
        clearSelection()
    }

    fun cutSelected() {
        val currentPath = _state.value.currentPath
        val sel = _state.value.selectedItems.map { File(currentPath, it).absolutePath }
        _state.update { it.copy(clipboard = Clipboard("cut", sel)) }
        showToast("Cut ${sel.size} item(s)")
        clearSelection()
    }

    fun pasteClipboard() {
        val s    = _state.value
        val clip = s.clipboard ?: return
        viewModelScope.launch {
            val destDir    = File(s.currentPath)
            val failedPaths = mutableListOf<String>()
            clip.items.forEach { fullPath ->
                val src = File(fullPath)
                val ok  = if (clip.op == "copy") repo.copyFile(src, destDir)
                          else repo.moveFile(src, destDir)
                if (!ok) failedPaths.add(fullPath)
            }
            val successCount = clip.items.size - failedPaths.size
            if (failedPaths.isEmpty()) {
                _state.update { it.copy(clipboard = null) }
                showToast("Pasted ${clip.items.size} item(s)")
            } else {
                _state.update { it.copy(clipboard = clip.copy(items = failedPaths)) }
                if (successCount > 0) showToast("Pasted $successCount item(s); ${failedPaths.size} failed")
                else showToast("Failed to paste ${failedPaths.size} item(s)")
            }
            loadCurrentFolder()
        }
    }

    // ── File operations ───────────────────────────────────────────────────────────────

    fun deleteSelected() {
        val s       = _state.value
        val targets = s.selectedItems.map { File(s.currentPath, it) }
        viewModelScope.launch {
            repo.deleteFiles(targets)
            _state.update { it.copy(selectedItems = emptySet()) }
            loadCurrentFolder()
            showToast("Deleted ${targets.size} item(s)")
        }
    }

    fun deleteItem(item: FileItem) {
        viewModelScope.launch {
            repo.deleteFiles(listOf(item.file))
            loadCurrentFolder()
            showToast("Deleted “${item.name}”")
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            if (repo.createFolder(_state.value.currentPath, name)) {
                loadCurrentFolder()
                showToast("Folder “$name” created")
            }
        }
    }

    fun renameItem(item: FileItem, newName: String) {
        viewModelScope.launch {
            if (repo.renameFile(item.file, newName)) {
                loadCurrentFolder()
                showToast("Renamed to “$newName”")
            }
        }
    }

    // ── Search ──────────────────────────────────────────────────────────────────────────────

    fun setSearchOpen(open: Boolean) {
        _state.update { it.copy(isSearchOpen = open, searchQuery = if (open) it.searchQuery else "") }
        if (!open) loadCurrentFolder()
    }

    fun setSearchQuery(q: String) {
        _state.update { it.copy(searchQuery = q) }
        loadCurrentFolder()
    }

    // ── UI state setters ───────────────────────────────────────────────────────────────

    fun openItem(item: FileItem) {
        if (item.kind == FileKind.FOLDER) navigateTo(item.file.absolutePath)
        else _state.update { it.copy(propsItem = item) }
    }

    fun setSortKey(key: SortKey) {
        _state.update {
            val dir = if (it.sortKey == key && it.sortDir == SortDir.ASC) SortDir.DESC else SortDir.ASC
            it.copy(sortKey = key, sortDir = dir)
        }
        loadCurrentFolder()
    }

    fun setDrawerOpen(open: Boolean) = _state.update { it.copy(isDrawerOpen = open) }
    fun setPropsItem(item: FileItem?) = _state.update { it.copy(propsItem = item) }
    fun setView(v: ViewMode)          = _state.update { it.copy(view = v) }
    fun setDensity(d: Density)        = _state.update { it.copy(density = d) }
    fun setThemeMode(m: AppThemeMode) = _state.update { it.copy(themeMode = m) }
    fun setAccent(a: AccentColor)     = _state.update { it.copy(accent = a) }
    fun setShowRibbon(v: Boolean)     = _state.update { it.copy(showRibbon = v) }
    fun setShowFooter(v: Boolean)     = _state.update { it.copy(showFooter = v) }

    // ── Toast ──────────────────────────────────────────────────────────────────────────────

    fun showToast(message: String) {
        _state.update { it.copy(toast = message) }
        viewModelScope.launch {
            delay(1800)
            _state.update { it.copy(toast = null) }
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────────────────

    fun freeSpaceText(): String {
        return try {
            val stat = StatFs(_state.value.currentPath)
            val free = stat.availableBlocksLong * stat.blockSizeLong
            free.formatSize() + " free"
        } catch (e: Exception) { "" }
    }

    fun titleForPath(path: String): String {
        val file = File(path)
        return when (file.name) {
            "storage", "0" -> "Internal storage"
            "sdcard"        -> "SD card"
            else            -> file.name.ifEmpty { "Home" }
        }
    }
}
