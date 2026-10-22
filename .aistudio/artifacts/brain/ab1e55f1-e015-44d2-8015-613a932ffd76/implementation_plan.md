# Fix Microphone Capture Conflict & Speech-to-Text Pronunciation Scoring

A comprehensive, zero-lag architecture to resolve Android microphone hardware locks, ensure reliable Speech-to-Text on all mobile devices, and deliver real-time pronunciation scoring and waveform feedback.

## User Review & Critical Decisions

> [!IMPORTANT]
> The primary reason speech recognition and pronunciation scoring fail on mobile devices is the simultaneous hardware capture of `AudioSource.MIC` by both `MediaRecorder` and Android's `SpeechRecognizer`. Because Android AudioFlinger allows only one exclusive capture client, `SpeechRecognizer` is starved of audio, resulting in empty transcripts and a 0% score.

- **Confirmed Decision 1: Single-Engine Real-Time Audio Capture**: Transition from running `MediaRecorder` and `SpeechRecognizer` concurrently to a dedicated `SpeechRecognizer` capture flow during speech tests.
- **Confirmed Decision 2: Native Decibel Metering for Waveform**: Utilize `RecognitionListener.onRmsChanged(rmsdB: Float)` to drive the live visual waveform directly, completely eliminating background audio polling loops that cause UI lag.
- **Confirmed Decision 3: Standard BCP-47 Language Tag**: Change `Locale.getDefault().toString()` (`"en_US"`) to `Locale.US.toLanguageTag()` (`"en-US"`) to ensure compatibility across all Android OEM speech engines (Samsung, Google, Xiaomi, OnePlus, Motorola).
- **Confirmed Decision 4: Graceful Speech Engine Health & Fallback**: Implement automatic detection of Google Speech Services with user-friendly status banners if speech recognition is disabled or requires speech pack installation.

---

## 1. Overview & Core Concept

### What It Does
When the user taps the microphone button to practice speaking (in Daily Sentences, Grammar Lessons, Paragraph Reading, Tongue Twisters, or Conversations):
1. The app starts a clean, uninhibited `SpeechRecognizer` session.
2. The user sees a smooth, real-time waveform driven natively by microphone sound energy (`rmsdB`).
3. Partial spoken words appear on screen in real time as the user speaks.
4. When the user taps stop (or pauses after finishing), the final transcript is immediately evaluated word-by-word against the target text, calculating an accurate pronunciation score and highlighting correct vs. mispronounced words in green and red.

### Key Value
- **Zero Audio Collisions**: Speech recognition works 100% reliably on all physical Android devices.
- **Zero UI Lag / Hangs**: Eliminates heavy file I/O and busy polling coroutines on the UI thread.
- **Real-Time Visual Delight**: Responsive waveform visualizer reacting to spoken speech energy without stutter.

---

## 2. User Experience & Visual Design

### Key User Flows
```
┌──────────────────┐     ┌────────────────────────┐     ┌──────────────────────┐
│ Tap "Record" Mic │ ──> │ Speak English Sentence │ ──> │ Tap "Stop" / Auto-End│
└──────────────────┘     └────────────────────────┘     └──────────────────────┘
         │                           │                              │
         ▼                           ▼                              ▼
Unlocks Mic & Binds       Live Waveform Animated         Word-by-word alignment
SpeechRecognizer session  Partial words appear live      Accurate % score shown
```

- **Recording State**: Distinct pulsing recording ring, active decibel-driven waveform bars, and real-time live preview of recognized words.
- **Scoring State**: Instant calculation with color-coded word chips:
  - **Green (Accent)**: Accurately pronounced words.
  - **Amber/Orange**: Near matches / phonetically close words.
  - **Red**: Mispronounced or missed words.
- **Device Readiness Banner**: If a device lacks Google Speech Services or speech recognition is temporarily offline, a friendly card explains how to enable speech services in Android Settings without freezing or crashing the app.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Pure SpeechRecognizer Audio Flow vs. Concurrent Capture
- **Chosen Approach**: Exclusive `SpeechRecognizer` capture during pronunciation testing. Real-time amplitude is derived from `RecognitionListener.onRmsChanged(rmsdB)`.
- **Why**: Android's Audio Policy Manager strictly forbids concurrent recording on `AudioSource.MIC` across separate processes. Bypassing `MediaRecorder` during recognition gives `SpeechRecognizer` 100% access to clear, uncorrupted audio.
- **Alternative Considered**: Running `AudioRecord` and streaming raw PCM to an external speech server. *Trade-off*: Requires high network bandwidth, API keys, and introduces cloud latency. On-device `SpeechRecognizer` is instantaneous and free.

### Decision 2: Waveform Amplitude Generation
- **Chosen Approach**: Native decibel mapping from `onRmsChanged(rmsdB: Float)`.
  - Android returns RMS in dB (typically -2.0 dB to 10.0 dB).
  - We normalize this to an integer scale (0–100) and update the Compose state smoothly.
- **Why**: Completely eliminates `while(isRecording) { delay(100); getMaxAmplitude(); }` polling loops that waste CPU cycles and cause UI stutters.

### Decision 3: Language Specification (`en-US`)
- **Chosen Approach**: Use `RecognizerIntent.EXTRA_LANGUAGE` with `Locale.US.toLanguageTag()` (`"en-US"`), and set `EXTRA_LANGUAGE_MODEL` to `LANGUAGE_MODEL_FREE_FORM`.
- **Why**: OEM engines like Samsung Voice or Xiaomi MIUI Speech often fail silently or return `ERROR_CLIENT` when passed underscore-delimited strings like `en_US` or device-specific locales like `en_IN` when English models are required.

---

## 4. Technical Architecture & Data Strategy

### Architecture & Component Diagram

```
                     ┌───────────────────────────────────────────┐
                     │          User Taps "Record" Button        │
                     └─────────────────────┬─────────────────────┘
                                           │
                                           ▼
                     ┌───────────────────────────────────────────┐
                     │          EnglishViewModel.kt              │
                     │  - Cancels previous listeners             │
                     │  - Sets SpeechRecognizer intent (en-US)   │
                     │  - speechRecognizer.startListening()      │
                     └─────────────────────┬─────────────────────┘
                                           │
                     ┌─────────────────────┴─────────────────────┐
                     │                                           │
                     ▼                                           ▼
     ┌───────────────────────────────┐           ┌───────────────────────────────┐
     │  onRmsChanged(rmsdB: Float)   │           │ onPartialResults / onResults  │
     │  - Normalizes dB -> 0..100    │           │ - Updates recognizedText      │
     │  - Powers smooth visualizer   │           │ - Real-time word display      │
     └───────────────────────────────┘           └───────────────┬───────────────┘
                                                                 │
                                                                 ▼
                                                 ┌───────────────────────────────┐
                                                 │   evaluateVoiceInput()        │
                                                 │   - Tokenizes target & spoken │
                                                 │   - Fuzzy phonetic alignment  │
                                                 │   - Computes score (0..100%)  │
                                                 └───────────────────────────────┘
```

### Component & State Mapping

1. **`EnglishViewModel.kt`**:
   - `startRecording()`: Initializes `SpeechRecognizer` cleanly on `Dispatchers.Main`, cancels any prior instance, binds `RecognitionListener`, and starts listening with BCP-47 `"en-US"`.
   - `onRmsChanged(rmsdB: Float)`: Maps `-2f..10f` dB to `0..100` amplitude, updating `maxRecordedAmplitude` and `amplitudeSamples` for the Compose waveform without any `MediaRecorder` or file writing.
   - `stopRecording()`: Calls `speechRecognizer.stopListening()`, collects `recognizedText`, and calls `evaluateVoiceInput()`.
   - `onError(error: Int)`: Properly distinguishes between non-speech timeouts (`ERROR_NO_MATCH`, `ERROR_SPEECH_TIMEOUT`), network states, and permission states, setting descriptive diagnostics instead of leaving the app in an indeterminate state.

2. **`VoiceRecorder.kt`**:
   - Retained for standalone voice recording if the user wants dedicated playback, but completely decoupled from live pronunciation scoring to prevent mic locking.

3. **`SpokenVoiceToTextCard.kt` & Waveform UI**:
   - Visualizes live recognized speech as words stream in, showing immediate user feedback.
   - Displays clear status badges when ready, listening, or processing.
