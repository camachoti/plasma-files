package com.camachoti.plasmafiles.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.camachoti.plasmafiles.data.model.AnalysisResult
import com.camachoti.plasmafiles.data.model.FileItem
import com.camachoti.plasmafiles.data.repository.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AnalyzerUiState(
    val scanPath: String = "",
    val result: AnalysisResult? = null,
    val isScanning: Boolean = false,
)

class StorageAnalyzerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = FileRepository(application)
    private val _state = MutableStateFlow(AnalyzerUiState())
    val state: StateFlow<AnalyzerUiState> = _state.asStateFlow()

    fun startScan(path: String) {
        _state.update { it.copy(scanPath = path, isScanning = true) }
        viewModelScope.launch {
            val result = repo.analyzeStorage(path)
            _state.update { it.copy(result = result, isScanning = false) }
        }
    }

    fun deleteFiles(files: List<FileItem>) {
        val path = _state.value.scanPath
        viewModelScope.launch {
            repo.deleteFiles(files.map { it.file })
            if (path.isNotEmpty()) startScan(path)
        }
    }
}
