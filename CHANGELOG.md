# Changelog

Alle nennenswerten Aenderungen an jFileWork werden hier dokumentiert.

## [Unreleased]

### Added
- Toolbar-Buttons zeigen nun echte Image-Icons (konfigurierbar via `icon` in `app-config.json`) mit `label` als Tooltip
- `os.exec` Actions ohne eigenes `icon` verwenden automatisch das System-Icon der `.exe`-Datei (Windows)
- `FileTypeIconResolver` liefert nun `ImageView` statt ASCII-Text (`[D]`, `[T]`, etc.) für die Datei-Spalte in den Panes
- ".." Parent-Link zeigt nun das Ordner-Icon statt des Datei-Icons

### Changed
- Weitere Stabilisierung und kleine Doku-Korrekturen.

## [0.0.1-Alpha] - 2026-09-01

### Added
- Dual-pane file manager core with navigation, selection, sorting, and watcher-based refresh.
- Built-in file operations via command engine (`copy`, `move`, `mkdir`, `delete`, `rename`).
- Config-driven UI actions (toolbar, menus, hotkeys) with template parameters.
- Plugin architecture with isolated loading and sample SHA-256 plugin.
- UX polish: tabs, inline filter, drag-and-drop, quick preview.
- Windows packaging workflow using `jlink` and `jpackage`.
- Integration tests for critical flows and JaCoCo coverage reports.

### Changed
- Projekt-Dokumentation erweitert um M6-Status, Packaging-Hinweise und Testabdeckung.
- Benennung und Javadocs in neuen M6-Test- und Packaging-Dateien konsolidiert.

### Notes
- Aktueller UI-Screenshot liegt unter `docs/screenshots/m6-main-window.png`.

