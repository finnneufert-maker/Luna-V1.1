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
            return "Ich kann Sprache erkennen, vorlesen, Berichtsheft-Einträge speichern, Posen zeigen und einfache Fragen sofort beantworten. Für freie Wissensfragen kannst du in den KI-Einstellungen einen sicheren Server verbinden.";
        if (low.contains("berichtsheft"))
            return "Schreibe deine Tätigkeiten in das Feld und tippe auf „Als Berichtsheft-Eintrag speichern“. Gespeicherte Einträge kannst du anzeigen oder exportieren.";
        if (low.contains("hilfe") || low.contains("einstellungen"))
            return "Für Sprache nutze das Mikrofon. Für Berichte nutze die Berichtsheft-Knöpfe. Unter „KI-Verbindung einstellen“ kannst du später deinen sicheren Online-KI-Server eintragen.";
        if (low.contains("danke")) return "Gern geschehen!";
        if (low.contains("gute nacht")) return "Gute Nacht und erhol dich gut.";
        return "Darauf kann ich offline noch nicht zuverlässig antworten. Verbinde unter „KI-Verbindung einstellen“ einen sicheren KI-Server; dann kann ich auch freie und schwierigere Fragen beantworten.";
    }
}
