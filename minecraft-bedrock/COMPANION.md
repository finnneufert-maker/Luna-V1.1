# Luna als Mitspielerin in einer privaten Switch-Welt

Ziel: Luna tritt mit einer eigenen Spieleridentität einer auf der Nintendo Switch
gehosteten Bedrock-Welt bei, die für eingeladene Freunde geöffnet ist. Andere eingeladene
Spieler sehen sie ebenfalls. Sie trägt einen Luna-Skin, folgt auf Wunsch, erkundet
mit der Gruppe und hilft beim Bauen. Die Android-App ist Steuerung und Gespräch,
nicht selbst eine Minecraft-Installation.

## Technischer Weg

1. Ein eigenständiger Bedrock-Client für Luna muss sich mit einer eigenen zulässigen
   Spieleridentität anmelden und den Beitritt über die Freundesliste nachweislich unterstützen. Keine Weltdatei und
   kein Add-on muss dafür auf der Switch installiert werden.
2. Skin-Daten gehören an diese Spieleridentität. Der klassische 64x64-Skin kann
   Haare, Augen und Uniform zeigen; abstehende Ohren und ein Schweif benötigen
   zusätzliche, plattformabhängige Geometrie und sind zunächst nicht zugesichert.
3. Ereignisse aus der Welt (Position, Blöcke, Spieler, Inventar, Gefahren) speisen
   eine begrenzte Zustandsmaschine: folgen, warten, erkunden, schützen, bauen.
   Luna liest den Spielchat und antwortet dort kurz und verständlich. Sie soll
   Nachrichten der Mitspieler nur in dieser Welt verarbeiten und auf Wunsch
   stummgeschaltet werden können. Die App zeigt denselben Gesprächsverlauf.
4. Bauaufträge werden als Vorschau mit Materialbedarf und Bauplatz geprüft. Luna
   platziert Blöcke schrittweise mit ihrer Figur und hält an, wenn etwas im Weg ist.
5. Die Android-App sendet nur freigegebene Befehle an den Client und zeigt Status.
   Der Client muss auf einem laufenden Gerät ausgeführt werden. Bei einem LAN-Beitritt
   muss dieses Gerät zusätzlich im selben Netzwerk sein.

## Fertigstellungskriterien

- Getesteter Beitritt über die Freundesliste zur Switch-Welt, während die Switch hostet.
- Sichtbarer Luna-Skin auf der Switch und einem weiteren Bedrock-Mitspieler.
- Folgen, Stoppen und Wiederfinden ohne beschädigte Bauten.
- Ein kleines Haus durch tatsächliches Platzieren von Blöcken bauen, inklusive
  Prüfung freier Flächen und Materialbedarf.
- Unterbrechung bei Verbindungsabbruch, vollem Inventar und gefährlichem Weg.
- Android-App kann die Aktionen starten und stoppen.
- Luna kann im Spielchat sprechen, Fragen der Mitspieler beantworten und auf
  „Luna, stopp“ reagieren, während sie baut oder folgt.

## Stand

Ein experimenteller Node-Client mit Chat-Befehlen liegt vor. Der Pfad für eine
eingeladene Welt ist noch ungetestet; Anmeldung, Freundeslisten-Beitritt,
Skin-Übertragung, Bewegung und Bauen sind nicht bestätigt. Ein eigenes
Microsoft-Spielkonto und eine laufende Client-Umgebung werden für den Test benötigt.
Kein echter Beitritt und kein Spieltest auf Switch. `luna-builder` ist nur ein lokales Skript-Prototyp-Paket
für eine kompatible Host-Welt und erfüllt dieses Mitspieler-Ziel nicht.
