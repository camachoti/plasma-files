package com.camachoti.plasmafiles.ui.screens

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.camachoti.plasmafiles.ui.components.*
import com.camachoti.plasmafiles.ui.viewmodel.*

@Composable
fun FileManagerScreen(vm: FileManagerViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val theme = state.theme

    RequestPermissions(onGranted = { vm.refresh() })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.bg)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Status bar
            StatusBar(theme)

            // Tab strip
            if (state.tabs.isNotEmpty()) {
                TabStrip(
                    tabs        = state.tabs,
                    activeTabId = state.activeTabId,
                    onSelectTab = { vm.switchTab(it) },
                    onCloseTab  = { vm.closeTab(it) },
                    onNewTab    = { vm.newTab() },
                    titleFor    = { vm.titleForPath(it) },
                    theme       = theme,
                )
            }

            // Path bar
            PathBar(
                path          = state.currentPath,
                canGoBack     = state.canGoBack,
                canGoForward  = state.canGoForward,
                canGoUp       = state.canGoUp,
                onBack        = { vm.goBack() },
                onForward     = { vm.goForward() },
                onUp          = { vm.goUp() },
                onSearch      = { vm.setSearchOpen(!state.isSearchOpen) },
                onMenu        = { vm.setDrawerOpen(true) },
                onNavigateTo  = { vm.navigateTo(it) },
                theme         = theme,
            )

            // Search bar
            if (state.isSearchOpen) {
                SearchBar(
                    query         = state.searchQuery,
                    onQueryChange = { vm.setSearchQuery(it) },
                    onClose       = { vm.setSearchOpen(false) },
                    theme         = theme,
                )
            }

            // Ribbon / Selection bar
            if (state.showRibbon) {
                if (state.selectedItems.isEmpty()) {
                    Ribbon(
                        items = buildRibbonItems(state, vm),
                        dividers = listOf(1, 5, 9),
                        theme = theme,
                    )
                } else {
                    SelectionBar(
                        count    = state.selectedItems.size,
                        onCancel = { vm.clearSelection() },
                        onCopy   = { vm.copySelected() },
                        onCut    = { vm.cutSelected() },
                        onShare  = { vm.showToast("Share sheet") },
                        onDelete = { vm.deleteSelected() },
                        onMore   = { vm.showToast("More options") },
                        theme    = theme,
                    )
                }
            }

            // File body
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (state.view) {
                    ViewMode.DETAILS -> FileDetailsView(
                        items          = state.items,
                        selectedItems  = state.selectedItems,
                        anySelected    = state.selectedItems.isNotEmpty(),
                        density        = state.density,
                        sortKey        = state.sortKey,
                        sortDir        = state.sortDir,
                        onSort         = { vm.setSortKey(it) },
                        onOpen         = { vm.openItem(it) },
                        onToggleSelect = { vm.toggleSelection(it.name) },
                        onToggleAll    = { if (state.selectedItems.size == state.items.size) vm.clearSelection() else vm.selectAll() },
                        theme          = theme,
                        emptyLabel     = if (state.searchQuery.isNotEmpty()) "No matches for “${state.searchQuery}”" else "This folder is empty",
                    )
                    ViewMode.TILES -> FileTilesView(
                        items          = state.items,
                        selectedItems  = state.selectedItems,
                        anySelected    = state.selectedItems.isNotEmpty(),
                        onOpen         = { vm.openItem(it) },
                        onToggleSelect = { vm.toggleSelection(it.name) },
                        theme          = theme,
                        emptyLabel     = if (state.searchQuery.isNotEmpty()) "No matches for “${state.searchQuery}”" else "This folder is empty",
                    )
                }

                // Loading indicator
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center).size(32.dp),
                        color = theme.accent,
                        strokeWidth = 3.dp,
                    )
                }
            }

            // Status footer
            if (state.showFooter) {
                HorizontalDivider(color = theme.line, thickness = 1.dp)
                StatusFooter(
                    count         = state.items.size,
                    selectedCount = state.selectedItems.size,
                    freeText      = vm.freeSpaceText(),
                    theme         = theme,
                )
            }

            // Gesture nav pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
                    .background(theme.bg),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .width(108.dp)
                        .height(4.dp)
                        .background(theme.text.copy(alpha = 0.35f))
                )
            }
        }

        // Side drawer (overlaid)
        if (state.isDrawerOpen) {
            SideDrawer(
                open        = state.isDrawerOpen,
                currentPath = state.currentPath,
                volumes     = state.volumes,
                onNavigate  = { vm.navigateTo(it) },
                onClose     = { vm.setDrawerOpen(false) },
                theme       = theme,
            )
        }

        // Properties bottom sheet
        state.propsItem?.let { item ->
            PropsBottomSheet(
                item      = item,
                onClose   = { vm.setPropsItem(null) },
                onShare   = { vm.showToast("Share") },
                onRename  = { vm.showToast("Rename") },
                onDelete  = { vm.deleteSelected(); vm.setPropsItem(null) },
                theme     = theme,
            )
        }

        // Toast
        state.toast?.let { message ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(theme.text, shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(message, fontSize = 12.5.sp, color = theme.bg)
            }
        }
    }
}

@Composable
private fun buildRibbonItems(state: FileManagerUiState, vm: FileManagerViewModel): List<RibbonItem> = listOf(
    RibbonItem(Icons.Default.CreateNewFolder, "New")    { vm.showToast("New folder") },
    RibbonItem(Icons.Default.ContentCut,     "Cut",    state.selectedItems.isNotEmpty()) { vm.cutSelected() },
    RibbonItem(Icons.Default.ContentCopy,    "Copy",   state.selectedItems.isNotEmpty()) { vm.copySelected() },
    RibbonItem(Icons.Default.ContentPaste,   "Paste",  state.clipboard != null) { vm.pasteClipboard() },
    RibbonItem(Icons.Default.Edit,           "Rename", state.selectedItems.size == 1) { vm.showToast("Rename") },
    RibbonItem(Icons.Default.Share,          "Share",  state.selectedItems.isNotEmpty()) { vm.showToast("Share") },
    RibbonItem(Icons.Default.Delete,         "Delete", state.selectedItems.isNotEmpty(), warn = state.selectedItems.isNotEmpty()) { vm.deleteSelected() },
    RibbonItem(Icons.Default.Sort,           "Sort")   { vm.setSortKey(state.sortKey) },
    RibbonItem(Icons.Default.ViewModule,     "View")   { vm.setView(if (state.view == ViewMode.DETAILS) ViewMode.TILES else ViewMode.DETAILS) },
    RibbonItem(Icons.Default.Info,           "Details",state.selectedItems.isNotEmpty()) { state.items.find { it.name in state.selectedItems }?.let { vm.setPropsItem(it) } },
)

@Composable
private fun RequestPermissions(onGranted: () -> Unit) {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        if (!Environment.isExternalStorageManager()) {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { if (Environment.isExternalStorageManager()) onGranted() }
            LaunchedEffect(Unit) {
                launcher.launch(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:${context.packageName}")))
            }
        }
    } else {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { if (it.values.any { granted -> granted }) onGranted() }
        LaunchedEffect(Unit) {
            launcher.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ))
        }
    }
}
