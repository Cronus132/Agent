# Ledger — Offline Personal Finance

A native Android app for tracking what you earn and spend, item by item, with a
live running balance. **Everything stays on your phone** — the app requests no
permissions at all (no `INTERNET`), so no data is ever uploaded or shared.

## Features

- **Live balance** — updates instantly as you add income or expenses.
- **Add entries** — amount, item/description, category, income vs. expense, and date.
- **Today & this month at a glance** — earned/spent today plus month income, spending and net saved on the home screen.
- **Monthly budget** — set a spending limit and watch how much is left (or how far over) each month.
- **Reports** — this month's income, expenses, net saved, **savings rate**, and a spending-by-category breakdown.
- **Savings goals** — set targets, track progress with a bar, and add or withdraw contributions (tracked as separate pots).
- **History** — grouped by day with each day's net total; **search** by item/category and filter by income/expense; delete with a tap.
- **Settings** — pick your currency symbol, set a monthly budget, or wipe all data.
- **Pleasant, reactive UI** — Jetpack Compose + Material 3, compact cards, light/dark mode, and
  wallpaper-based dynamic color on Android 12+.

## Tech stack

| Concern            | Choice                                   |
|--------------------|------------------------------------------|
| Language           | Kotlin                                   |
| UI                 | Jetpack Compose + Material 3             |
| Local storage      | Room (SQLite) — fully offline            |
| Architecture       | MVVM with `StateFlow` (reactive)         |
| Min / target SDK   | 26 (Android 8.0) / 35 (Android 15)       |

Amounts are stored as integer **cents**, so money math is always exact.

## Build & run

You need **Android Studio** (Ladybug / 2024.2 or newer).

1. Open Android Studio → **Open** → select the project folder (`E:\Projects\Agent`).
2. Let it sync. Android Studio will download the Gradle distribution and
   generate the Gradle wrapper JAR automatically.
3. Plug in an Android phone (with USB debugging on) or start an emulator.
4. Press **Run ▶**. The app installs as **Ledger**.

To build an installable APK from the command line instead:

```bash
./gradlew assembleDebug
```

The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

> **Note:** `gradle/wrapper/gradle-wrapper.jar` (a binary file) is not included in
> this scaffold. Opening the project in Android Studio generates it for you. If you
> prefer the command line and have Gradle installed, run `gradle wrapper` once in
> the project folder first to create it.

## Project layout

```
app/src/main/java/com/ledger/app/
├─ LedgerApp.kt            Application: owns the database + repository
├─ MainActivity.kt         Navigation scaffold (Home / History / Reports / Goals, Settings + Add)
├─ data/                   Room entities (transactions, savings goals), DAOs, database (v2 + migration), repository, prefs
├─ ui/
│  ├─ FinanceViewModel.kt  Reactive state: balance, today/month totals, category spend, goals
│  ├─ theme/               Material 3 colors & theme
│  └─ screens/             Dashboard, History, Reports, Goals, Add, Settings, shared components
└─ util/                   Money/date formatting, categories
```

## Version note

If Gradle sync complains about the KSP plugin version, it must match the Kotlin
version. This project uses Kotlin `2.0.21` with KSP `2.0.21-1.0.25` (in the
root `build.gradle.kts`). If needed, bump KSP to the newest `2.0.21-*` release.
