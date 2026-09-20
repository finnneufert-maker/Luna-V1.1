package de.luna.assistant;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.OutputStream;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int SPEECH = 31;
    private static final int EXPORT = 32;
    private EditText input;
    private TextView answer;
    private TextToSpeech tts;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        tts = new TextToSpeech(this, this);
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 9);
        buildUi();
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(20),dp(18),dp(20),dp(30));
        root.setBackgroundColor(Color.rgb(23,19,38)); scroll.addView(root);

        TextView title = text("Luna  •  Version 1.0", 25); root.addView(title);
        ImageView portrait = new ImageView(this); portrait.setImageResource(R.drawable.luna_maid);
        portrait.setScaleType(ImageView.ScaleType.CENTER_CROP); root.addView(portrait, new LinearLayout.LayoutParams(-1,dp(280)));

        answer = text("Hallo! Ich kann sprechen, deine Notizen beantworten und Berichtsheft-Einträge lokal speichern.", 17);
        answer.setPadding(dp(14),dp(14),dp(14),dp(14)); answer.setBackgroundColor(Color.rgb(38,32,58)); root.addView(answer);

        input = new EditText(this); input.setHint("Schreibe oder diktiere etwas …"); input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY); input.setMinLines(2); root.addView(input);
        root.addView(button("Luna fragen", v -> respond(input.getText().toString())));
        root.addView(button("🎤 Spracheingabe", v -> startSpeech()));
        root.addView(button("📝 Als Berichtsheft-Eintrag speichern", v -> saveReport()));
        root.addView(button("📚 Gespeicherte Berichte anzeigen", v -> showReports()));
        root.addView(button("💾 Berichtsheft als Textdatei exportieren", v -> exportReports()));
        root.addView(button("🐾 Luna über anderen Apps anzeigen", v -> startOverlay()));
        root.addView(button("🎭 Lunas Posen testen", v -> showPosePicker()));
        root.addView(button("Luna vom Bildschirm entfernen", v -> stopService(new Intent(this, OverlayService.class))));

        TextView safety = text("Datenschutz: Version 1 speichert Berichtsheft-Einträge nur lokal. Sie versendet keine Nachrichten und führt keine Finanzgeschäfte aus.", 13);
        safety.setTextColor(Color.LTGRAY); safety.setPadding(0,dp(18),0,0); root.addView(safety);
        setContentView(scroll);
    }

    private void respond(String q) {
        setLunaState("thinking");
        String s = q.trim(); String low = s.toLowerCase(Locale.GERMAN); String r;
        if (s.isEmpty()) r = "Sag oder schreibe mir zuerst etwas.";
        else if (low.contains("hallo") || low.contains("hi ") || low.equals("hi")) r = "Hallo! Was kann ich für dich erledigen?";
        else if (low.contains("datum") || low.contains("welcher tag")) r = "Heute ist der " + new SimpleDateFormat("dd. MMMM yyyy", Locale.GERMANY).format(new Date()) + ".";
        else if (low.contains("berichtsheft") && (low.contains("zeigen") || low.contains("öffnen"))) { showReports(); return; }
        else if (low.contains("was kannst du")) r = "Ich kann Sprache erkennen, antworten, Texte vorlesen, Berichtsheft-Einträge speichern und als Bildschirmfigur erscheinen.";
        else r = "Ich habe verstanden: „" + s + "“. In Version 1 beantworte ich einfache Fragen lokal. Die erweiterte KI kommt in einem späteren Update.";
        answer.setText(r); speak(r);
    }

    private void saveReport() {
        String s=input.getText().toString().trim();
        if (s.isEmpty()) { answer.setText("Bitte trage zuerst deine Tätigkeiten ein."); return; }
        LunaMemory.addReport(this,s); input.setText("");
        String r="Der Berichtsheft-Eintrag wurde auf diesem Gerät gespeichert."; answer.setText(r); speak(r);
    }

    private void showReports() {
        new AlertDialog.Builder(this).setTitle("Berichtsheft").setMessage(LunaMemory.reports(this)).setPositiveButton("Schließen",null).show();
    }

    private void startSpeech() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 10); return;
        }
        setLunaState("listening");
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE"); i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Sprich mit Luna …");
        try { startActivityForResult(i,SPEECH); } catch (Exception e) { answer.setText("Auf diesem Gerät ist keine Spracheingabe verfügbar."); }
    }

    private void exportReports() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TITLE, "Luna-Berichtsheft.txt");
        startActivityForResult(i, EXPORT);
    }

    @Override protected void onActivityResult(int req,int result,Intent data) {
        super.onActivityResult(req,result,data);
        if(req==SPEECH && result==RESULT_OK && data!=null) {
            ArrayList<String> list=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(list!=null&&!list.isEmpty()) { input.setText(list.get(0)); respond(list.get(0)); }
        }
        if(req==EXPORT && result==RESULT_OK && data!=null && data.getData()!=null) {
            try (OutputStream out=getContentResolver().openOutputStream(data.getData())) {
                out.write(LunaMemory.reports(this).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                answer.setText("Das Berichtsheft wurde als Textdatei gespeichert.");
            } catch (Exception e) { answer.setText("Die Textdatei konnte nicht gespeichert werden."); }
        }
    }

    private void startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName())));
            answer.setText("Erlaube bitte „Über anderen Apps einblenden“ und tippe danach erneut auf den Knopf."); return;
        }
        Intent i=new Intent(this,OverlayService.class);
        if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
        answer.setText("Luna ist jetzt als verschiebbare Bildschirmfigur aktiv.");
    }

    private void showPosePicker() {
        if (!Settings.canDrawOverlays(this)) { startOverlay(); return; }
        String[] labels={"Stehen","Zuhören","Denken","Sprechen","Winken","Sitzen","Verbeugen","Schlafen"};
        String[] states={"idle","listening","thinking","talking","wave","sitting","bowing","sleeping"};
        new AlertDialog.Builder(this).setTitle("Lunas Pose").setItems(labels,(d,which)->setLunaState(states[which])).show();
    }

    private Button button(String label, View.OnClickListener l) {
        Button b=new Button(this); b.setText(label); b.setAllCaps(false); b.setOnClickListener(l);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(52)); p.topMargin=dp(8); b.setLayoutParams(p); return b;
    }
    private TextView text(String s,int size) { TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(Color.WHITE); v.setPadding(0,dp(8),0,dp(8)); return v; }
    private void speak(String s) { if(tts!=null) tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"luna"); }
    private void setLunaState(String state) {
        if (!Settings.canDrawOverlays(this)) return;
        Intent i=new Intent(this,OverlayService.class).setAction(OverlayService.ACTION_STATE).putExtra(OverlayService.EXTRA_STATE,state);
        if(Build.VERSION.SDK_INT>=26) startForegroundService(i); else startService(i);
    }
    @Override public void onInit(int status) {
        if(status==TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.GERMANY);
            tts.setOnUtteranceProgressListener(new android.speech.tts.UtteranceProgressListener() {
                public void onStart(String id){ runOnUiThread(() -> setLunaState("talking")); }
                public void onDone(String id){ runOnUiThread(() -> setLunaState("idle")); }
                public void onError(String id){ runOnUiThread(() -> setLunaState("idle")); }
            });
        }
    }
    @Override protected void onDestroy() { if(tts!=null){tts.stop();tts.shutdown();} super.onDestroy(); }
    private int dp(int x){return(int)(x*getResources().getDisplayMetrics().density);}
}
