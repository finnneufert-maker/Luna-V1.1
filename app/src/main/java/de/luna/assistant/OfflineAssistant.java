package de.luna.assistant;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Small, private fallback so Luna always gives a useful answer without a server. */
final class OfflineAssistant {
    private OfflineAssistant() {}

    static String answer(Context context, String question) {
        String q = question == null ? "" : question.trim();
        String low = q.toLowerCase(Locale.GERMAN);
        if (q.isEmpty()) return "Sag oder schreibe mir zuerst etwas.";
        if (low.matches(".*\\b(hallo|hi|hey|guten morgen|guten abend)\\b.*"))
            return "Hallo! Was kann ich für dich erledigen?";
        if (low.contains("uhrzeit") || low.equals("wie spät ist es"))
            return "Es ist " + new SimpleDateFormat("HH:mm", Locale.GERMANY).format(new Date()) + " Uhr.";
        if (low.contains("datum") || low.contains("welcher tag") || low.contains("welches datum"))
            return "Heute ist der " + new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMANY).format(new Date()) + ".";
        if (low.contains("was kannst du"))
            return "Ich kenne deine bisherigen Berichtsheft-Notizen von August 2025 bis September 2026, kann Monatsinhalte nennen, Tätigkeiten speichern und Formulierungen vorschlagen. Für völlig freie Wissensfragen brauchst du weiterhin einen sicheren KI-Server.";

        if (low.contains("wiederkehr") || low.contains("regelmäßig") || low.contains("jeden monat"))
            return "Wiederkehrende Tätigkeiten: Gemüse schneiden, Ware für den nächsten Tag richten und vorbereiten, sicher und hygienisch arbeiten sowie reinigen und aufräumen.";

        if (low.contains("06.09") || low.contains("6. september") || low.contains("1520") || low.contains("1.520"))
            return "Am 06.09.2026 hast du 1.520 Bratwürste gebraten. Du bist nicht zum Veranstaltungsort mitgefahren.";
        if (low.contains("19.09") || low.contains("19. september") || low.contains("erdbeermousse"))
            return "Am 19.09.2026 hast du Erdbeermousse, Tiramisu und Mascarponecreme hergestellt.";
        if (low.contains("04.11") || low.contains("4. november"))
            return "Für den 04.11.2025 ist notiert, dass du krank warst. Weitere sichere Einzelheiten liegen dazu nicht vor.";

        if (asksMonth(low, "august"))
            return "August 2025: Betrieb, Küche, Abläufe und Lagerorte kennengelernt; Küchengeräte erklärt bekommen; Krallengriff sowie Hygiene und sicheres Arbeiten an heißen Geräten behandelt.";
        if (asksMonth(low, "september"))
            return "September 2025: erster Schultag, Besteck und Tischdekoration, Geflügel und Eier, pochiertes Ei, Spiegelei, Tranchieren, Trockenmarinade und Brownie-Masse. September 2026: am 06.09. 1.520 Bratwürste gebraten und am 19.09. Erdbeermousse, Tiramisu und Mascarponecreme hergestellt.";
        if (asksMonth(low, "oktober"))
            return "Oktober 2025: französische Küche und Fachbegriffe, Warenkunde zum Thema Wein sowie Rechte und Pflichten von Auszubildenden und Arbeitgebern.";
        if (asksMonth(low, "november"))
            return "November 2025: Am 04.11. warst du krank. Weitere Einzelheiten sind noch nicht sicher notiert, deshalb erfinde ich nichts hinzu.";
        if (asksMonth(low, "dezember"))
            return "Für Dezember 2025 sind noch keine sicheren Einzelheiten gespeichert. Ich erfinde nichts hinzu.";
        if (asksMonth(low, "januar"))
            return "Januar 2026: Umgang mit Fritteuse, Konvektomat und Gasherd. In der Berufsschule: Sauerteig, salziger und süßer Teig sowie Schokobrötchen.";
        if (asksMonth(low, "februar"))
            return "Februar 2026: Sicherheitsunterweisung durch den Sicherheitsbeauftragten.";
        if (asksMonth(low, "märz") || asksMonth(low, "maerz"))
            return "Für März 2026 sind noch keine sicheren Einzelheiten gespeichert. Ich erfinde nichts hinzu.";
        if (asksMonth(low, "april"))
            return "April 2026: Am 13.04. war der erste Arbeitstag nach den Osterferien. Du hast Ratatouille-Gemüse zubereitet und weitere Küchengeräte kennengelernt. Unterrichtsthemen waren Eiweiß, Proteine und Fette.";
        if (asksMonth(low, "mai"))
            return "Mai 2026: Crème brûlée und Mousse au Chocolat zubereitet sowie Catering-Einsatz. Bereits früher gelernte Süßspeisen sollen nicht erneut als neu gelernt erscheinen.";
        if (asksMonth(low, "juni") || asksMonth(low, "juli"))
            return "Juni und Juli 2026: Gemüse geschnitten, Ware für den nächsten Tag gerichtet, sicher und hygienisch gearbeitet sowie gereinigt und aufgeräumt.";

        if (low.contains("krall") || low.contains("schneidtechnik"))
            return "Beim Krallengriff werden die Fingerkuppen nach innen genommen; die Knöchel führen die Messerklinge. So bleiben die Fingerspitzen besser geschützt.";
        if (low.contains("ei") && (low.contains("gelernt") || low.contains("zubereit") || low.contains("thema")))
            return "Zu Eiern sind pochiertes Ei und Spiegelei notiert. In einer weiteren Aufgabe kamen gekochte Eier, Omelett und Rührei hinzu.";
        if (low.contains("süßspeise") || low.contains("dessert"))
            return "Notierte Süßspeisen sind Brownies, Crème brûlée, Mousse au Chocolat, Erdbeermousse, Tiramisu und Mascarponecreme. Bereits Bekanntes soll nicht erneut als neu gelernt beschrieben werden.";
        if (low.contains("sicherheit") || low.contains("hygiene"))
            return "Wichtig sind saubere Arbeitsflächen und Hände, geeignete Arbeitskleidung, sichere Messerführung, vorsichtiger Umgang mit heißen Geräten und die Sicherheitsunterweisung im Februar 2026.";
        if (low.contains("berufsschule") || low.contains("unterricht"))
            return "Gespeicherte Schulthemen: Besteck und Tischdekoration; Geflügel und Ei; Teige und Massen; französische Küche und Fachbegriffe; Warenkunde zum Thema Wein; Rechte und Pflichten; Sauerteig sowie Eiweiß, Proteine und Fette.";
        if (low.contains("was habe ich gelernt") || low.contains("was hab ich gelernt"))
            return "Du hast unter anderem den Krallengriff, sicheres und hygienisches Arbeiten, Geflügel- und Eierzubereitungen, Tranchieren, Trockenmarinade, Teige und Massen, Ratatouille sowie mehrere Süßspeisen behandelt. Auch Geräte wie Fritteuse, Konvektomat und Gasherd sind notiert.";
        if (low.contains("formulieren") || low.contains("monatsbericht") || low.contains("zusammenfassen"))
            return "Ich formuliere ehrlich mit dem Monat als Überschrift, trenne Betrieb und Berufsschule, erfinde nichts und stelle bereits Gelerntes nicht erneut als neu dar. Sammle zuerst Stichpunkte; am Monatsende kann daraus ein Bericht entstehen.";
        if (low.contains("alle") && (low.contains("notiz") || low.contains("bericht")))
            return "Tippe auf „Gespeicherte Berichte anzeigen“. Dort stehen alle eingebauten Berichtsheft-Notizen von August 2025 bis September 2026 zusammen mit deinen neuen Einträgen.";
        if (low.contains("berichtsheft"))
            return "Ich kenne deine bisherigen Berichtsheft-Inhalte. Frage zum Beispiel: „Was war im April?“, „Was habe ich in der Berufsschule gelernt?“ oder „Was war am 19.09.?“. Neue Tätigkeiten kannst du weiterhin speichern und exportieren.";
        if (low.contains("hilfe") || low.contains("einstellungen"))
            return "Für Sprache nutze das Mikrofon. Für Berichte nutze die Berichtsheft-Knöpfe. Unter „KI-Verbindung einstellen“ kannst du später deinen sicheren Online-KI-Server eintragen.";
        if (low.contains("danke")) return "Gern geschehen!";
        if (low.contains("gute nacht")) return "Gute Nacht und erhol dich gut.";
        return "Darauf kann ich offline noch nicht zuverlässig antworten. Verbinde unter „KI-Verbindung einstellen“ einen sicheren KI-Server; dann kann ich auch freie und schwierigere Fragen beantworten.";
    }

    private static boolean asksMonth(String low, String month) {
        return low.contains(month) && (low.contains("was") || low.contains("bericht") || low.contains("gemacht") || low.contains("gelernt") || low.equals(month));
    }
}
