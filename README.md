# FieldVet

An offline, on-device LLM-powered livestock triage assistant for farmers. The
farmer enters an animal's species and symptoms, the app retrieves relevant
entries from a local veterinary knowledge base, and generates grounded triage
advice using an on-device Gemma model. Network access is only used once, on
first launch, to download the model file — retrieval and inference themselves
work fully offline.

## How it works

1. **Retrieval** — the farmer's symptoms are matched against a local SQLite
   FTS5 knowledge base of veterinary entries. If nothing relevant matches, the
   app returns a "no match" result instead of guessing.
2. **Grounded prompting** — only after a knowledge base match is found is a
   prompt built from that entry and sent to the model, so the LLM never
   answers from an ungrounded state.
3. **Inference** — a quantized Gemma model runs on-device via LiteRT-LM to
   produce the triage advice.
4. **Urgency** — the urgency level shown to the farmer comes from the matched
   knowledge base entry's `urgency` field, not from free-form model output.

## Tech stack

- Kotlin + Jetpack Compose
- MVVM architecture
- On-device LLM inference via [LiteRT-LM](https://ai.google.dev/edge/litert)
  running a quantized Gemma model
- SQLite with FTS5 for local knowledge base retrieval (no Room)
- `minSdk = 29`

## Project structure

```
com.fieldvet.view       — Composable screens only, no business logic
com.fieldvet.viewmodel  — TriageViewModel, ModelDownloadViewModel, UI state
com.fieldvet.model      — Domain logic (TriageUseCase, UrgencyClassifier) and
                           data layer (RetrievalEngine, KnowledgeBase,
                           InferenceEngine, PromptBuilder, model download)
```

## Minimum requirements

Not formally pinned anywhere else in the project — derived from the model
file (Gemma 3 1B-IT, q4-quantized, ~584MB) and what the code actually checks.
Treat these as estimates, not measured figures.

| | Minimum | Recommended |
|---|---|---|
| Android version | 10 (API 29) — hard floor, `minSdk` in `build.gradle.kts` | — |
| RAM | ~3GB | 4GB+ |
| Free storage | ~1GB (model + temp download file + margin) | 2GB+ |

Notes:
- **RAM isn't enforced by the app.** `ModelDownloadWorker` only checks free
  *disk* space (via `StatFs`) before downloading; nothing checks available
  memory, so a low-RAM device won't be blocked upfront — it'll just OOM or
  thrash during inference instead of failing gracefully.
- **Real devices are strongly recommended over emulators.** On-device Gemma
  inference relies on CPU/NPU performance that emulators don't reflect
  accurately, so inference latency is only meaningful on real hardware. The
  project is perf-tested against a OnePlus 12R (Snapdragon 8 Gen 2, 8–16GB
  RAM) — emulators are fine for verifying the build and UI/retrieval flow,
  not for judging performance.


## Setup

### Prerequisites

- Android Studio (latest stable)
- JDK 17
- An Android device or emulator meeting the [minimum requirements](#minimum-requirements) above

### Get the code

```bash
git clone <repo-url>
cd FieldVet2
```

### Build and run

1. Open the project root in Android Studio and let it sync Gradle.
2. Connect a physical device (`adb devices` to confirm it's visible) or start
   an emulator.
3. Run the `app` configuration, or from the command line:

   ```bash
   ./gradlew installDebug
   ```

### First launch: model download

The Gemma `.litertlm` model file is **not** bundled in the APK and **not**
committed to git. On first launch, the app downloads it from a public GitHub
Release asset, verifies its SHA-256 checksum, and stores it in app-private
storage. A download screen with progress is shown until this completes; on
every subsequent launch this is a cheap file-exists check, so the screen only
appears once.

This requires a network connection on first launch only. If the app is
backgrounded or killed mid-download, the download restarts from 0% on next
launch (no resumable downloads yet).

**For fast local iteration** without waiting for a full download, you can
push a model file directly into the same location the download would use (no
root/`run-as` needed):

```bash
adb push <model-file> /sdcard/Android/data/com.example.fieldvet/files/model.litertlm
```

## Scope

This is an MVP. Photo-based input, multi-device sync, and user accounts are explicitly out of scope.
This is a native Android app — no Flutter/React Native equivalents are
planned.
