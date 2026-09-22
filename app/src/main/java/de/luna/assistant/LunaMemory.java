package de.luna.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    private static String formatReport(String raw) {
        if (raw.isEmpty()) return "Keine Tätigkeiten eingetragen.";
        String clean = raw.substring(0, 1).toUpperCase(Locale.GERMAN) + raw.substring(1);
        if (!clean.endsWith(".") && !clean.endsWith("!") && !clean.endsWith("?")) clean += ".";
        return "Ausgeführte Tätigkeiten: " + clean;
    }
}
