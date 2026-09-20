package de.luna.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

final class LunaMemory {
    private static final String PREFS = "luna_memory";
    private static final String REPORTS = "reports";

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
