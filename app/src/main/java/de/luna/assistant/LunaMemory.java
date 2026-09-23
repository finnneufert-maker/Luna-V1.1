package de.luna.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;

final class LunaMemory {
    private static final String PREFS = "luna_memory";
    private static final String REPORTS = "reports";
    private static final String SEEDED = "berichtsheft_wissen_v1";

    private static final String BUILT_IN_REPORTS =
            "BERICHTSHEFT-WISSEN (August 2025 bis September 2026)\n\n" +
            "August 2025\n" +
            "• Betrieb, Küche, Arbeitsabläufe und Lagerorte kennengelernt.\n" +
            "• Küchengeräte gezeigt und erklärt bekommen.\n" +
            "• Krallengriff als sichere Schneidtechnik gelernt.\n" +
            "• Hygiene, Arbeitssicherheit und vorsichtiger Umgang mit heißen Geräten und Öfen behandelt.\n\n" +
            "September 2025\n" +
            "• Erster Schultag und Grundlagen zu Besteck, Tischdecken und Tischdekoration.\n" +
            "• Geflügelarten und Eier: pochiertes Ei, Spiegelei und Tranchieren.\n" +
            "• Trockenmarinade sowie Teige und Massen, darunter Brownie-Masse.\n\n" +
            "Oktober 2025\n" +
            "• Französische Küche und französische Hotel- und Küchenfachbegriffe.\n" +
            "• Warenkunde zum Thema Wein im Unterricht.\n" +
            "• Rechte und Pflichten von Auszubildenden und Arbeitgebern.\n\n" +
            "November 2025\n" +
            "• Am 04.11.2025 krank. Weitere Einzelheiten sind noch nicht sicher bekannt.\n\n" +
            "Dezember 2025\n" +
            "• Noch keine sicheren Einzelheiten notiert.\n\n" +
            "Januar 2026\n" +
            "• Umgang mit Fritteuse, Konvektomat und Gasherd.\n" +
            "• Berufsschule: Sauerteig, salziger Teig, süßer Teig und Schokobrötchen.\n\n" +
            "Februar 2026\n" +
            "• Sicherheitsunterweisung durch den Sicherheitsbeauftragten.\n\n" +
            "März 2026\n" +
            "• Noch keine sicheren Einzelheiten notiert.\n\n" +
            "April 2026\n" +
            "• Nach den Osterferien am 13.04. wieder im Betrieb.\n" +
            "• Ratatouille-Gemüse zubereitet und weitere Küchengeräte kennengelernt.\n" +
            "• Unterrichtsthemen: Eiweiß, Proteine und Fette.\n\n" +
            "Mai 2026\n" +
            "• Crème brûlée und Mousse au Chocolat zubereitet.\n" +
            "• Catering-Einsatz im neunten bis zehnten Ausbildungsmonat.\n" +
            "• Bereits früher gelernte Süßspeisen nicht erneut als neu gelernt darstellen.\n\n" +
            "Juni und Juli 2026\n" +
            "• Gemüse geschnitten, Ware für den nächsten Tag gerichtet und vorbereitet.\n" +
            "• Sicher und hygienisch gearbeitet sowie gereinigt und aufgeräumt.\n\n" +
            "September 2026\n" +
            "• 06.09.: 1.520 Bratwürste gebraten; nicht zum Veranstaltungsort mitgefahren.\n" +
            "• 19.09.: Erdbeermousse, Tiramisu und Mascarponecreme hergestellt.\n\n" +
            "Wiederkehrende Tätigkeiten\n" +
            "• Gemüse schneiden, Ware für den nächsten Tag vorbereiten, sicheres und hygienisches Arbeiten, Reinigen und Aufräumen.\n\n" +
            "Schreibregeln\n" +
            "• Erst Stichpunkte sammeln und am Monatsende ehrlich zusammenfassen.\n" +
            "• Betrieb und Berufsschule berücksichtigen, den Monat als Überschrift setzen und nichts erfinden.\n" +
            "• Bereits Gelerntes nicht erneut als neu darstellen und nicht behaupten, etwas selbstständig geplant zu haben.";

    static void seedKnownReports(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (p.getBoolean(SEEDED, false)) return;
        String old = p.getString(REPORTS, "");
        String combined = BUILT_IN_REPORTS + (old.isEmpty() ? "" : "\n\n— — —\n\nEIGENE EINTRÄGE\n\n" + old);
        p.edit().putString(REPORTS, combined).putBoolean(SEEDED, true).apply();
    }

    static void addReport(Context context, String raw) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String stamp = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(new Date());
        String entry = stamp + "\n" + formatReport(raw.trim());
        String old = p.getString(REPORTS, "");
        p.edit().putString(REPORTS, entry + (old.isEmpty() ? "" : "\n\n— — —\n\n" + old)).apply();
    }

    static String reports(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(REPORTS, "Noch keine Einträge gespeichert.");
    }

    static boolean importReports(Context context, String imported) {
        if (imported == null) return false;
        String clean = imported.trim();
        if (clean.isEmpty()) return false;
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String current = p.getString(REPORTS, "");
        if (current.contains(clean)) return true;
        // A complete old Luna export already contains the built-in knowledge. Prefer it
        // over duplicating the same long block, while preserving newer local entries.
        String merged;
        if (clean.contains("BERICHTSHEFT-WISSEN")) {
            merged = clean;
            if (!current.isEmpty() && !clean.contains(current))
                merged += "\n\n— — —\n\nSPÄTERE EINTRÄGE\n\n" + current;
        } else {
            merged = current + (current.isEmpty() ? "" : "\n\n— — —\n\n") +
                    "IMPORTIERTE EINTRÄGE\n\n" + clean;
        }
        p.edit().putString(REPORTS, merged).putBoolean(SEEDED, true).commit();
        return true;
    }

    static String findRelevant(Context context, String question) {
        if (question == null) return "";
        Set<String> words = new HashSet<>();
        for (String word : question.toLowerCase(Locale.GERMAN).split("[^a-zäöüß0-9]+"))
            if (word.length() >= 4 && !isStopWord(word)) words.add(word);
        if (words.isEmpty()) return "";
        StringBuilder found = new StringBuilder();
        int matches = 0;
        for (String line : reports(context).split("\\r?\\n")) {
            String low = line.toLowerCase(Locale.GERMAN);
            boolean hit = false;
            for (String word : words) if (low.contains(word)) { hit = true; break; }
            if (hit && !line.trim().isEmpty()) {
                if (matches++ > 0) found.append("\n");
                found.append("• ").append(line.replaceFirst("^[•\\-]\\s*", "").trim());
                if (matches == 4) break;
            }
        }
        return found.toString();
    }

    private static boolean isStopWord(String w) {
        return w.equals("habe") || w.equals("heute") || w.equals("bitte") ||
                w.equals("kannst") || w.equals("wurde") || w.equals("mein") ||
                w.equals("eine") || w.equals("einen") || w.equals("über");
    }

    private static String formatReport(String raw) {
        if (raw.isEmpty()) return "Keine Tätigkeiten eingetragen.";
        String clean = raw.substring(0, 1).toUpperCase(Locale.GERMAN) + raw.substring(1);
        if (!clean.endsWith(".") && !clean.endsWith("!") && !clean.endsWith("?")) clean += ".";
        return "Ausgeführte Tätigkeiten: " + clean;
    }
}
