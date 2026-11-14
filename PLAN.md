# jFileWork — Entwicklungsplan

## Technologie-Stack

| Technologie | Verwendung |
|---|---|
| Java 26 | Implementierungssprache |
| JavaFX 23+ (OpenJFX) | UI-Framework |
| Jackson | JSON-Konfiguration |
| SLF4J + Logback | Logging |
| JUnit Jupiter 6 | Unit- und Integrationstests |
| TestFX + Monocle | UI-Tests (headless) |
| Maven (Multi-Modul) | Build-System |

---

## Maven-Module

| Modul | Verantwortlichkeit |
|---|---|
| `core` | Dateisystem-Logik, Command-Engine, Konfiguration, Events, Security, Plugin-Host |
| `ui-javafx` | Split-View, Tabellen, Menüs, Toolbar, Keybindings, Theme |
| `plugin-api` | Stabile Schnittstellen für externe Command-Plugins |

---

## Paketstruktur

```
com.github.uscexp.jfilework
  ├─ core
  │   ├─ fs           (FileService, FileWatcherService, PathResolver)
  │   ├─ command      (Command, CommandContext, CommandRegistry, CommandExecutor, builtin/*)
  │   ├─ config       (AppConfig, UiConfig, CommandBindingConfig, ConfigService)
  │   ├─ plugin       (Plugin, PluginDescriptor, PluginManager, PluginClassLoaderFactory)
  │   ├─ security     (CommandPolicy, PathPolicy)
  │   └─ event        (EventBus)
  ├─ ui
  │   └─ javafx
  │       ├─ MainApp
  │       ├─ controller  (MainWindowController, FilePaneController, ProgressDialogController, ...)
  │       ├─ model       (FileItemViewModel, PaneStateViewModel)
  │       └─ action      (UiActionMapper, ToolbarBuilder, MenuBuilder, KeyBindingService)
  └─ api
      └─ plugin          (CommandPlugin, PluginContext)
```

---

## Konfigurationsdesign (`config/app-config.json`)

```json
{
  "ui": {
    "theme": "dark",
    "showHiddenFiles": false,
    "leftStartPath": "C:/",
    "rightStartPath": "D:/"
  },
  "actions": [
    {
      "id": "copyToOtherPane",
      "label": "Copy",
      "icon": "/image/copy.png",
      "commandId": "file.copy",
      "params": { "target": "${inactivePane.path}" },
      "shortcut": "F5",
      "placement": ["toolbar.main", "menu.file"]
    },
    {
      "id": "zipWith7z",
      "label": "Zip (7z)",
      "commandId": "os.exec",
      "params": {
        "exe": "C:/Program Files/7-Zip/7z.exe",
        "args": "a ${activePane.path}/archive.7z ${selection.paths}"
      },
      "placement": ["menu.tools"]
    }
  ],
  "plugins": {
    "directory": "plugins",
    "enabled": ["sample-hash-plugin"]
  },
  "security": {
    "allowOsCommands": true,
    "allowedExecutables": [
      "C:/Program Files/7-Zip/7z.exe",
      "C:/Windows/System32/robocopy.exe"
    ]
  }
}
```

---

## Plugin-API

```java
public interface CommandPlugin {
    PluginDescriptor descriptor();
    void initialize(PluginContext context);
    List<Command> commands();
}
```

---

## Sicherheit (pragmatisch)

- OS-Kommandos nur wenn `allowOsCommands=true`
- Executable-Whitelist vor Ausführung geprüft
- Parameter-Sanitizer gegen Shell-Injection
- `PathPolicy`: Pfade nur innerhalb erlaubter Roots
- Lange Operationen nur async (`ExecutorService` + `CancellationToken`)

---

## UI-Test-Prinzipien

| Prinzip | Details |
|---|---|
| **Headless via Monocle** | `-Dglass.platform=Monocle`, CI-tauglich ohne Display |
| **`@TempDir` Fixture-FS** | Temporäre Verzeichnisstrukturen, keine Produktivdaten |
| **`FxTestBase`** | App-Start, Stage, FxRobot, sauberes Beenden |
| **Scope** | Interaktionsebene — keine Business-Logik die Unit-Tests abdecken |
| **Separierbar** | UI-Tests per `@Tag("ui")` von Unit-Tests trennbar |

---

## Meilenstein-Backlog

### Milestone 0 ✅ — Projekt-Setup & Fundament
> **Ziel:** Lauffähiges Projekt-Skeleton, alle Werkzeuge konfiguriert, erstes JavaFX-Fenster öffnet sich.

| # | Story | Artefakte |
|---|-------|-----------|
| 0.1 | `pom.xml` auf Java 26, JavaFX 23+, Jackson, SLF4J/Logback, JUnit 5 erweitern | `pom.xml` |
| 0.2 | Maven-Modulstruktur anlegen: `core`, `ui-javafx`, `plugin-api` als Sub-Module im Parent-POM | `pom.xml` (parent + 3×module) |
| 0.3 | `Main.java` als JavaFX-Startpunkt (`Application.launch`) umbauen | `Main.java` |
| 0.4 | Leeres Hauptfenster (800×600, SplitPane 50/50) mit Dummy-Inhalt anzeigen | `MainApp.java`, `main-window.fxml` |
| 0.5 | Logging-Konfiguration, `.gitignore` vervollständigen | `logback.xml`, `.gitignore` |
| 0.6 | Basis-`README.md` mit Build- und Run-Anleitung | `README.md` |
| 0.7 | TestFX + Monocle (Headless-JavaFX) als Test-Dependencies in `pom.xml` konfigurieren | `pom.xml` |
| 0.8 | Basis-Testhelfer anlegen: `FxTestBase`, `HeadlessTestConfig` | `FxTestBase`, `HeadlessTestConfig` |

---

### Milestone 1 ✅ — Dual-Pane Dateiansicht
> **Ziel:** Beide Panels zeigen das Dateisystem navigierbar an.

| # | Story | Kernsymbole |
|---|-------|-------------|
| 1.1 | `FileService` implementieren: `listDirectory`, `exists`, `isDirectory`, `getMetadata` via `java.nio.file` | `FileService` |
| 1.2 | `FileItemViewModel` (Name, Größe, Datum, Typ, Pfad, Icon-Hint) als JavaFX-Property-Modell | `FileItemViewModel` |
| 1.3 | `PaneStateViewModel` (aktueller Pfad, Sortierung, Filter, Auswahl-Liste) | `PaneStateViewModel` |
| 1.4 | `FilePaneController` mit `TableView<FileItemViewModel>`: Spalten Name, Größe, Datum, Typ | `FilePaneController`, `file-pane.fxml` |
| 1.5 | Navigation: Doppelklick auf Ordner wechselt Verzeichnis, `..` geht hoch | `FilePaneController` |
| 1.6 | Breadcrumb / Pfad-Eingabefeld oben je Pane (direkte Eingabe möglich) | `FilePaneController` |
| 1.7 | Einzel- und Multi-Selektion (Maus + Shift/Ctrl) | `PaneStateViewModel` |
| 1.8 | Sortierung je Spalte (klickbar) | `FilePaneController` |
| 1.9 | `ActivePaneTracker` — merkt, welche Seite zuletzt Fokus hatte | `ActivePaneTracker` |
| 1.10 | `FileWatcherService` — Verzeichnis-Änderungen triggern automatisches Reload der Pane | `FileWatcherService` |
| 1.11 | Unit-Tests für `FileService` mit JUnit 5 `@TempDir` | `FileServiceTest` |
| 1.12 | UI-Test: Fenster öffnet sich, beide Panes sichtbar, SplitPane korrekt geteilt | `MainWindowUiTest` |
| 1.13 | UI-Test: Navigation per Doppelklick wechselt Pfad in Breadcrumb | `FilePaneNavigationUiTest` |
| 1.14 | UI-Test: Multi-Selektion via Shift/Ctrl korrekt in `PaneStateViewModel` | `FilePaneSelectionUiTest` |

---

### Milestone 2 ✅ — Built-in Commands & Command-Engine
> **Ziel:** Alle klassischen Dateioperationen funktionieren intern. Command-System steht.

| # | Story | Kernsymbole |
|---|-------|-------------|
| 2.1 | `Command`-Interface: `id()`, `execute(CommandContext, Map<String,String>)`, `canExecute(CommandContext)` | `Command` |
| 2.2 | `CommandContext`: aktive/inaktive Pane, Auswahl-Snapshot, Services, `ProgressSink`, `CancellationToken` | `CommandContext`, `SelectionSnapshot` |
| 2.3 | `CommandResult` (SUCCESS, PARTIAL, FAILED, CANCELLED + Nachricht) | `CommandResult` |
| 2.4 | `CommandRegistry`: Commands registrieren, per ID abrufen | `CommandRegistry` |
| 2.5 | `CommandExecutor`: async auf `ExecutorService`, Progress-Callback, Fehlerbehandlung | `CommandExecutor` |
| 2.6 | Built-in Command `file.copy` (F5): in inaktive Pane mit Überschreib-Dialog | `CopyCommand` |
| 2.7 | Built-in Command `file.move` (F6): copy + delete | `MoveCommand` |
| 2.8 | Built-in Command `file.mkdir` (F7): Verzeichnis anlegen mit Namens-Dialog | `MkdirCommand` |
| 2.9 | Built-in Command `file.delete` (F8): Bestätigungsdialog, sicheres Löschen | `DeleteCommand` |
| 2.10 | Built-in Command `file.rename` (F2/Shift+F6): in-place oder Dialog | `RenameCommand` |
| 2.11 | Progress-Dialog mit Cancel-Button für lange Operationen | `ProgressDialogController` |
| 2.12 | Toast/Statusbar-Feedback für schnelle Operationen | `StatusBarController` |
| 2.13 | Unit-Tests für alle Built-in Commands mit `@TempDir` | `*CommandTest` |
| 2.14 | UI-Test: F5 öffnet Copy-Bestätigungsdialog, Datei erscheint danach in Ziel-Pane | `CopyCommandUiTest` |
| 2.15 | UI-Test: F8 öffnet Lösch-Dialog, Cancel bricht ab ohne Änderung | `DeleteCommandUiTest` |
| 2.16 | UI-Test: Progress-Dialog erscheint bei langer Operation, Cancel-Button bricht ab | `ProgressDialogUiTest` |

---

### Milestone 3 ✅ — Konfigurierbare UI (Menüs, Toolbar, Hotkeys)
> **Ziel:** Toolbar, Menüleiste und Shortcuts werden vollständig aus JSON-Konfiguration aufgebaut.

| # | Story | Kernsymbole |
|---|-------|-------------|
| 3.1 | JSON-Schema für `config/app-config.json` definieren und dokumentieren | `app-config.json`, `config-schema.md` |
| 3.2 | `AppConfig`, `UiConfig`, `CommandBindingConfig`, `ActionConfig` als Jackson-POJOs | `AppConfig`, `ActionConfig` |
| 3.3 | `ConfigService`: laden, validieren, speichern, Defaults mergen | `ConfigService` |
| 3.4 | `UiActionMapper`: liest Actions aus Config, sucht Command in Registry, erstellt JavaFX-Actions | `UiActionMapper` |
| 3.5 | Dynamischer Toolbar-Builder aus Config (`placement: ["toolbar.main"]`) | `ToolbarBuilder` |
| 3.6 | Dynamischer Menüleisten-Builder aus Config (`placement: ["menu.file"]`) | `MenuBuilder` |
| 3.7 | Hotkey-Binding aus Config (`shortcut: "F5"`) via JavaFX `KeyCombination` | `KeyBindingService` |
| 3.8 | Nicht-vorhandene Commands → UI-Element disabled + Log-Warnung | `UiActionMapper` |
| 3.9 | Parameter-Template-Engine: `${activePane.path}`, `${inactivePane.path}`, `${selection.paths}` | `ParamTemplateEngine` |
| 3.10 | UI-Einstellungen-Dialog (Theme, versteckte Dateien, Startpfade) | `PreferencesController` |
| 3.11 | Persistenz: letzter Fensterzustand und Pfad je Pane beim Start wiederherstellen | `SessionService` |
| 3.12 | UI-Test: Toolbar-Button aus Config erscheint und löst korrekten Command aus | `ToolbarConfigUiTest` |
| 3.13 | UI-Test: Hotkey (z. B. F5) löst Command aus ohne Mausklick | `HotkeyBindingUiTest` |
| 3.14 | UI-Test: nicht vorhandenes Command → Button disabled | `DisabledActionUiTest` |

---

### Milestone 4 ✅ — OS-Command-Adapter & Sicherheit
> **Ziel:** Externe Programme als Commands einbindbar. Sicherheitsregeln greifen.

| # | Story | Kernsymbole |
|---|-------|-------------|
| 4.1 | `OsExecCommand` (`os.exec`): startet externen Prozess mit Parametern, fängt stdout/stderr | `OsExecCommand` |
| 4.2 | `CommandPolicy`: prüft ob `os.exec` erlaubt (`allowOsCommands`-Flag) | `CommandPolicy` |
| 4.3 | `PathPolicy`: validiert, dass Pfade innerhalb erlaubter Roots liegen | `PathPolicy` |
| 4.4 | Executable-Whitelist: `allowedExecutables`-Liste in Config, Prüfung vor Ausführung | `CommandPolicy` |
| 4.5 | Parameter-Sanitizer: verhindert Shell-Injection in Template-Auflösung | `ParamSanitizer` |
| 4.6 | Prozess-Output-Fenster: stdout/stderr live anzeigen, Prozess killbar | `ProcessOutputController` |
| 4.7 | Policy-Verletzung → klar verständlicher Dialog + Log-Entry | `CommandExecutor`, `PolicyException` |
| 4.8 | Konfigurations-Beispiele für 7-Zip, Notepad++, Git-Bash in `README.md` | `README.md` |
| 4.9 | UI-Test: Prozess-Output-Fenster zeigt Live-Ausgabe, Kill-Button beendet Prozess | `ProcessOutputUiTest` |
| 4.10 | UI-Test: Policy-Verletzung zeigt Fehlerdialog | `PolicyViolationUiTest` |

---

### Milestone 5 ✅ — Plugin-System
> **Ziel:** Externe JARs liefern neue Commands, werden sicher geladen und in UI integriert.

| # | Story | Kernsymbole |
|---|-------|-------------|
| 5.1 | `plugin-api` Modul stabilisieren: `CommandPlugin`, `PluginDescriptor`, `PluginContext` (nur sichere Facades) | `CommandPlugin`, `PluginContext` |
| 5.2 | API-Versionierung: semantische Version in `PluginDescriptor`, Kompatibilitätsprüfung beim Laden | `PluginDescriptor`, `PluginManager` |
| 5.3 | `PluginClassLoaderFactory`: isolierter `URLClassLoader` je Plugin | `PluginClassLoaderFactory` |
| 5.4 | `PluginManager`: Discovery via `ServiceLoader<CommandPlugin>` aus `plugins/`-Ordner | `PluginManager` |
| 5.5 | Plugin-Lifecycle: `initialize(PluginContext)` → Commands registrieren → `destroy()` | `PluginManager` |
| 5.6 | Inkompatibles/fehlerhaftes Plugin isoliert abfangen, Rest läuft weiter | `PluginManager` |
| 5.7 | Plugin-Liste in Einstellungen anzeigen (Name, Version, Status, En-/Disable) | `PluginSettingsController` |
| 5.8 | Demo-Plugin `hash-plugin`: berechnet MD5/SHA-256 für gewählte Dateien, zeigt Ergebnis | `HashPlugin`, `HashCommand` |
| 5.9 | Plugin-Entwickler-Doku: wie schreibe ich ein Plugin, wie paketiere ich es | `PLUGIN_DEV.md` |
| 5.10 | UI-Test: Demo-Plugin erscheint in Plugin-Liste als aktiv | `PluginSettingsUiTest` |
| 5.11 | UI-Test: Hash-Command aus Plugin taucht in Toolbar auf (wenn konfiguriert) und liefert Ergebnis-Dialog | `HashPluginUiTest` |

---

### Milestone 6 ✅ — Polishing, UX & Stabilisierung
> **Ziel:** Produktionsreife Qualität, angenehmere UX, vollständige Dokumentation.

| # | Story | Kernsymbole |
|---|-------|-------------|
| 6.1 ✅ | Datei-Icons je Typ (via `FileTypeIconResolver`, ImageView statt ASCII-Text) | `FileTypeIconResolver` |
| 6.2 ✅ | Filter / Schnellsuche je Pane (Dateiname-Muster) | `FilePaneController` |
| 6.3 ✅ | Tabs je Pane für mehrere parallele Verzeichnisse | `MainWindowController`, `main-window.fxml` |
| 6.4 ✅ | Drag & Drop zwischen Panes (löst `file.copy`/`file.move` aus) | `FilePaneController`, `MainWindowController` |
| 6.5 ✅ | Datei-Vorschau (Text, Bild) in separatem Panel oder Dialog | `PreviewController` |
| 6.6 ✅ | Benennungskonventionen, Javadoc, Code-Review | alle Module |
| 6.7 ✅ | Integrationstests: kritische Flows (Copy, Move, Plugin-Load) | `*IntegrationTest` |
| 6.8 ✅ | Native Packaging: `jpackage` für Windows-Installer (`jlink`-optimiertes Runtime) | `pom.xml`, `packaging/` |
| 6.9 ✅ | Changelog, vollständige `README.md`, Screenshot in Doku | `CHANGELOG.md`, `README.md` |
| 6.10 ✅ | UI-Test: Drag & Drop löst Copy/Move aus, beide Panes aktualisiert | `DragDropUiTest` |
| 6.11 ✅ | UI-Test: Filter-/Suchfeld blendet nicht-passende Einträge aus | `FilterUiTest` |
| 6.12 ✅ | UI-Test-Coverage-Report in Maven-Ausgabe (Surefire + JaCoCo) | `pom.xml` |

---

## Empfohlene Bearbeitungsreihenfolge

```
M0 (1–2 Tage) → M1 (3–5 Tage) → M2 (4–6 Tage) → M3 (3–4 Tage)
→ M4 (2–3 Tage) → M5 (4–6 Tage) → M6 (3–5 Tage)
```

> **Hinweis:** M6 (Tabs, Vorschau) ist optional — kann nach M5 je nach Bedarf priorisiert oder übersprungen werden.
