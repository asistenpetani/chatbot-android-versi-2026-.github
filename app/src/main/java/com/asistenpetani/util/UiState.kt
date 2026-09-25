package com.asistenpetani.util

/**
 * A sealed class representing generic UI State for handling asynchronous actions
 * such as api token calls, database loads, or Gemini queries gracefully.
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
