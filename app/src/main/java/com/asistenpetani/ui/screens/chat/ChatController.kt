package com.asistenpetani.ui.screens.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class ChatController(private val context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    var isTtsReady = false
        private set

    init {
        // Initialize Text-To-Speech
        try {
            textToSpeech = TextToSpeech(context, this, "com.google.android.tts")
        } catch (ex: Exception) {
            try {
                textToSpeech = TextToSpeech(context, this)
            } catch (ex2: Exception) {
                Log.e("ChatController", "Gagal inisialisasi TextToSpeech: ${ex2.message}")
            }
        }

        // Initialize Speech Recognizer
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            Log.e("ChatController", "Speech Recognition tidak tersedia di perangkat ini.")
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.setPitch(1.0f)
            textToSpeech?.setSpeechRate(0.95f)

            var isVoiceSet = false
            try {
                val availableVoices = textToSpeech?.voices
                if (!availableVoices.isNullOrEmpty()) {
                    val idVoices = availableVoices.filter { it.locale.language == "id" }
                    if (idVoices.isNotEmpty()) {
                        val premiumVoice = idVoices.find {
                            it.name.contains("natural", ignoreCase = true) ||
                            it.name.contains("local", ignoreCase = true) ||
                            it.name.contains("id-id-x", ignoreCase = true)
                        } ?: idVoices.firstOrNull()

                        premiumVoice?.let {
                            textToSpeech?.setVoice(it)
                            isVoiceSet = true
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("ChatController", "Gagal memindai daftar suara: ${e.message}")
            }

            if (!isVoiceSet) {
                val result = textToSpeech?.setLanguage(Locale.forLanguageTag("id-ID"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech?.setLanguage(Locale.getDefault())
                }
            }
            isTtsReady = true
        } else {
            Log.e("ChatController", "Inisialisasi TextToSpeech gagal.")
        }
    }

    fun speakOut(text: String) {
        if (!isTtsReady || textToSpeech == null) return

        val cleanText = text
            .replace(Regex("[*#_`~]"), "")
            .replace(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+"), "") // Remove emojis
            .replace(Regex("\\n+"), " . ")

        try {
            textToSpeech?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "aspek_speech_id")
        } catch (e: Exception) {
            Log.e("ChatController", "Gagal mengucapkan teks: ${e.message}")
        }
    }

    fun startListening(onResult: (String) -> Unit, onError: (String) -> Unit, onReady: () -> Unit = {}) {
        if (speechRecognizer == null) {
            onError("Speech Recognizer belum diinisialisasi")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Silakan bicara...")
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { onReady() }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                val errorMsg = when(error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                    else -> "Unknown error"
                }
                onError(errorMsg)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    onResult(matches[0])
                } else {
                    onError("No text recognized")
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun destroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }
}
