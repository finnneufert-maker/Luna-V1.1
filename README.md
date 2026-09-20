# Luna Assistant 1.0

Eine lokale Android-Vorabversion der persönlichen Bildschirmassistentin Luna.

## Enthalten

- verschiebbare Luna-Figur über anderen Apps
- sanfte Leerlaufbewegung der Bildschirmfigur
- Reaktionszustände für Zuhören, Denken, Sprechen und Schlafen
- echter 4×2-Sprite-Atlas mit acht unterschiedlichen Luna-Posen
- gespeicherte Bildschirmposition mit Schutz vor versehentlichem Verschieben außerhalb des Displays
- deutsche Spracheingabe und Sprachausgabe über Android
- einfache lokale Antworten ohne Cloud-Konto
- lokale Berichtsheft-Einträge mit Datum
- Export des Berichtshefts als Textdatei
- keine Nachrichten-, Bank- oder Handelszugriffe

## In Android Studio installieren

1. Den Ordner `LunaAssistant` in Android Studio öffnen.
2. Gradle synchronisieren lassen.
3. Das Redmi per USB-Debugging verbinden oder einen Emulator starten.
4. **Run** drücken. Für eine APK: **Build → Build APK(s)**.
5. Beim ersten Start Mikrofon, Benachrichtigungen und „Über anderen Apps einblenden“ erlauben.

## Ohne PC über GitHub bauen

Bei jedem Push auf `main` startet `.github/workflows/build-apk.yml` automatisch einen Android-Build. Nach erfolgreichem Abschluss befindet sich unter **Actions → Luna APK bauen → Artifacts** das Paket `Luna-Assistant-v1-debug-apk`. Darin liegt die installierbare `app-debug.apk`.

## Sicherheitsgrenzen

Spätere WhatsApp-Funktionen sollen Entwürfe erstellen und vor dem Senden bestätigen lassen. Finanzfunktionen dürfen Übersichten und Warnungen liefern, aber keine autonomen Käufe oder Verkäufe ausführen.

Das verbindliche Figuren- und Animationskonzept steht in `docs/ANIMATION_PLAN.md`.
