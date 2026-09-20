# Luna – Animationsplan

## Verbindliches Design

- lange silberne Haare
- violette Katzenaugen
- zwei silberne Katzenohren, keine menschlichen Ohren
- silberner Katzenschweif
- klassische schwarze, langärmelige Maid-Uniform mit weißer Schürze
- violette Schleife, blickdichte schwarze Strumpfhose, flache schwarze Schuhe
- Outfit und Proportionen bleiben in jeder Ansicht gleich

Das Modellblatt `luna_model_sheet.png` ist die visuelle Hauptreferenz.
Der einsatzfähige Zustandsatlas liegt als `luna_sprite_atlas.png` in den App-Ressourcen. Er enthält acht gleich große Zellen in der Reihenfolge: Idle, Zuhören, Denken, Sprechen, Winken, Sitzen, Verbeugen, Schlafen.

## Richtungen

1. vorne
2. vorne links und vorne rechts
3. links und rechts
4. hinten links und hinten rechts
5. hinten
6. Draufsicht

Gespiegelte Richtungen dürfen nur verwendet werden, wenn Schleife, Schürze, Haare und Schweif dadurch nicht sichtbar inkonsistent werden.

## Zustände für Version 1.x

| Zustand | Mindestbilder | Verhalten |
| --- | ---: | --- |
| Idle | 4 | Atmen, Schweif und Ohren bewegen |
| Blinzeln | 3 | zufällig während Idle |
| Sprechen | 4 | Mundbewegung passend zur Sprachausgabe |
| Gehen | 8 je Richtung | über den Bildschirm laufen |
| Winken | 6 | Begrüßung oder Antippen |
| Sitzen | 3 | am Bildschirmrand warten |
| Verbeugen | 6 | höfliche Bestätigung |
| Schlafen | 4 | nach längerer Inaktivität |
| Denken | 4 | während Verarbeitung |
| Überrascht | 3 | besondere Meldung |

## Sichere Darstellungsregeln

- Die Uniform bleibt bei allen Ansichten und Bewegungen vollständig bedeckend.
- Keine aufreizenden Kamerawinkel oder Posen.
- Beim Sitzen, Verbeugen und in der Draufsicht bleibt die Kleidung natürlich und geschlossen.
- Die Figur muss auch in kleiner Overlay-Größe klar erkennbar bleiben.

## Technische Ablage

Spätere Einzelbilder kommen nach `app/src/main/res/drawable-nodpi/sprites/` und werden zusätzlich in einer Atlas-Datei mit Zustand, Richtung, Bilddauer und Berührungspunkt beschrieben. V1 verwendet bis dahin das Ganzkörperbild mit einer programmierten Leerlaufbewegung.
