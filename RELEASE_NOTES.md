# Release Notes

## 0.0.1-Alpha

jFileWork 0.0.1-Alpha delivers the first complete alpha of the local dual-pane file manager.

### Highlights

- Dual-pane navigation with multi-selection and sorting
- Built-in file commands (`copy`, `move`, `mkdir`, `delete`, `rename`)
- Config-driven toolbar, menus, and hotkeys
- OS command integration with pragmatic policy checks
- Plugin system with ServiceLoader-based discovery and sample hash plugin
- M6 UX updates: tabs, inline filters, drag-and-drop, quick preview
- Windows packaging workflow via `jlink` + `jpackage`

### Test and Quality

- Unit tests, integration tests, and headless JavaFX UI tests are in place
- JaCoCo reports are generated with `mvn verify`

### Known Limitations

- Security is pragmatic and not a full sandbox
- EXE installer generation requires WiX on Windows
- Packaging documentation currently focuses on Windows

### Documentation

- Roadmap: `PLAN.md`
- Changelog: `CHANGELOG.md`
- Screenshot reference: `docs/screenshots/m6-main-window.png`
- Packaging guide: `packaging/windows/README.md`

