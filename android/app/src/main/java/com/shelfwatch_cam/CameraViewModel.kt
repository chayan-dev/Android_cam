package com.shelfwatch_cam

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class CameraViewModel : ViewModel()  {

  // Expose screen UI state
  private val _uiState = MutableStateFlow(CameraUiState())
  val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

  //business logic
  fun updateUiState(){

  }
}

data class CameraUiState(
  val right: String = "",
  val left : String = "",
  val top : String = ""
)
