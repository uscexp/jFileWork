# Release Checklist

Use this checklist before publishing a new release.

## 1) Versioning

- [ ] Parent version in `pom.xml` is updated from `-SNAPSHOT` to the intended release tag
- [ ] All modules inherit the same release version
- [ ] Release notes and changelog mention the same version string

## 2) Build and Tests

- [ ] Full build succeeds
- [ ] Unit, integration, and UI tests are green
- [ ] JaCoCo reports are generated

```powershell
mvn clean verify
```

## 3) Packaging

- [ ] Windows packaging script runs successfully
- [ ] If EXE packaging is required, WiX is installed
- [ ] Installer or app-image artifacts are validated

```powershell
.\packaging\windows\build-installer.ps1 -AllowAppImageFallback
```

## 4) Documentation

- [ ] `README.md` reflects current versions and feature status
- [ ] `CHANGELOG.md` includes release section and migration notes if needed
- [ ] `RELEASE_NOTES.md` is updated for users
- [ ] Screenshot references are current

## 5) Publish

- [ ] Tag created (for example `v0.0.1-alpha`)
- [ ] Release artifacts attached to the release page
- [ ] Post-release version moved to next `-SNAPSHOT`

