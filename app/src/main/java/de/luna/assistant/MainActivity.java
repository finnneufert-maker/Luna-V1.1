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
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class MainActivity extends Activity implements TextToSpeech.OnInitListener {
    private static final int SPEECH = 31;
    private static final int EXPORT = 32;
    private static final int IMPORT = 33;
    private static final int BACKUP = 34;
    private EditText input;
    private TextView answer;
    private TextToSpeech tts;
    private Luna3DView luna3d;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        LunaMemory.seedKnownReports(this);
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

        TextView title = text("Luna  •  Version 1.9", 25); root.addView(title);
        TextView modelHint = text("Echtes 3D: Ziehe Luna stufenlos nach links oder rechts, um sie vollständig zu drehen.", 13);
        modelHint.setTextColor(Color.LTGRAY); root.addView(modelHint);
        luna3d = new Luna3DView(this);
        root.addView(luna3d, new LinearLayout.LayoutParams(-1,dp(500)));
        Button modelStyle = button("🐱 Zur Chibi-Variante wechseln", v -> {
            boolean isChibi=luna3d.toggleChibi();
            ((Button)v).setText(isChibi?"✨ Zur normalen Variante wechseln":"🐱 Zur Chibi-Variante wechseln");
        });
        root.addView(modelStyle);

        answer = text("Hallo! Ich kann sprechen, deine Notizen beantworten und Berichtsheft-Einträge lokal speichern.", 17);
        answer.setPadding(dp(14),dp(14),dp(14),dp(14)); answer.setBackgroundColor(Color.rgb(38,32,58)); root.addView(answer);

        input = new EditText(this); input.setHint("Schreibe oder diktiere etwas …"); input.setTextColor(Color.WHITE);
        input.setHintTextColor(Color.LTGRAY); input.setMinLines(2); root.addView(input);
        root.addView(button("Luna fragen", v -> respond(input.getText().toString())));
        root.addView(button("⚙️ KI-Verbindung einstellen", v -> showAiSettings()));
        root.addView(button("🎤 Spracheingabe", v -> startSpeech()));
        root.addView(button("📝 Als Berichtsheft-Eintrag speichern", v -> saveReport()));
        root.addView(button("📚 Gespeicherte Berichte anzeigen", v -> showReports()));
        root.addView(button("💾 Berichtsheft als Textdatei exportieren", v -> exportReports()));
        root.addView(button("📥 Daten einer früheren Luna importieren", v -> importOldData()));
        root.addView(button("🔐 Vollständige Luna-Sicherung erstellen", v -> exportBackup()));
        root.addView(button("🐾 Luna über anderen Apps anzeigen", v -> startOverlay()));
        root.addView(button("🎭 Lunas Posen testen", v -> showPosePicker()));
        root.addView(button("🐱 Chibi-Reaktionen ansehen", v -> showReactionPicker()));
        root.addView(button("💬 Chibi über WhatsApp teilen", v -> shareChibiToWhatsApp()));
        root.addView(button("Luna vom Bildschirm entfernen", v -> stopService(new Intent(this, OverlayService.class))));

        TextView safety = text("Datenschutz: Version 1 speichert Berichtsheft-Einträge nur lokal. Sie versendet keine Nachrichten und führt keine Finanzgeschäfte aus.", 13);
        safety.setTextColor(Color.LTGRAY); safety.setPadding(0,dp(18),0,0); root.addView(safety);
        setContentView(scroll);
    }

    private void respond(String q) {
        setLunaState("thinking");
        if(luna3d!=null) luna3d.setExpression("thinking");
        String s = q.trim(); String low = s.toLowerCase(Locale.GERMAN);
        if (low.contains("berichtsheft") && (low.contains("zeigen") || low.contains("öffnen"))) { showReports(); return; }
        if(AiClient.isConfigured(this)) {
            answer.setText("Ich denke darüber nach …");
            AiClient.ask(this,s,new AiClient.Callback(){
                public void onSuccess(String result){runOnUiThread(()->{answer.setText(result);speak(result);});}
                public void onError(String message){runOnUiThread(()->{
                    String fallback=OfflineAssistant.answer(MainActivity.this,s);
                    answer.setText(message+"\n\nOffline-Antwort: "+fallback); speak(fallback); setLunaState("idle");
                });}
            });
            return;
        }
        String r=OfflineAssistant.answer(this,s);
        answer.setText(r); speak(r);
    }

    private void showAiSettings() {
        EditText field=new EditText(this);
        field.setHint("https://dein-luna-server.example");
        field.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);
        field.setText(AiClient.backendUrl(this));
        int pad=dp(18); field.setPadding(pad,pad,pad,pad);
        new AlertDialog.Builder(this)
                .setTitle("Sichere KI-Verbindung")
                .setMessage("Trage die HTTPS-Adresse deines Luna-KI-Servers ein. API-Schlüssel gehören niemals direkt in die App.")
                .setView(field)
                .setNegativeButton("Abbrechen",null)
                .setNeutralButton("Entfernen",(d,w)->{AiClient.saveBackendUrl(this,"");answer.setText("Online-KI-Verbindung entfernt. Luna antwortet weiterhin offline.");})
                .setPositiveButton("Speichern",(d,w)->{
                    if(AiClient.saveBackendUrl(this,field.getText().toString()))
                        answer.setText(AiClient.isConfigured(this)?"KI-Server gespeichert. Stelle jetzt eine Frage.":"Keine Serveradresse gespeichert. Luna antwortet offline.");
                    else answer.setText("Bitte verwende eine vollständige HTTPS-Adresse.");
                }).show();
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

    private void importOldData() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, IMPORT);
    }

    private void exportBackup() {
        Intent i = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.setType("application/json");
        i.putExtra(Intent.EXTRA_TITLE, "Luna-Datensicherung.json");
        startActivityForResult(i, BACKUP);
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
        if(req==IMPORT && result==RESULT_OK && data!=null && data.getData()!=null) {
            try(InputStream in=getContentResolver().openInputStream(data.getData())) {
                String content=readText(in);
                String reports=content;
                if(content.trim().startsWith("{")) {
                    JSONObject json=new JSONObject(content);
                    reports=json.optString("reports","");
                    String backend=json.optString("backend_url","");
                    if(!backend.isEmpty()) AiClient.saveBackendUrl(this,backend);
                    JSONObject pos=json.optJSONObject("overlay_position");
                    if(pos!=null) getSharedPreferences("overlay_position",MODE_PRIVATE).edit()
                            .putInt("x",pos.optInt("x",20)).putInt("y",pos.optInt("y",250)).apply();
                }
                answer.setText(LunaMemory.importReports(this,reports)
                        ?"Die früheren Luna-Daten wurden übernommen."
                        :"Die Datei enthielt keine verwendbaren Luna-Daten.");
            } catch(Exception e) { answer.setText("Diese Datei konnte nicht als Luna-Datensicherung gelesen werden."); }
        }
        if(req==BACKUP && result==RESULT_OK && data!=null && data.getData()!=null) {
            try(OutputStream out=getContentResolver().openOutputStream(data.getData())) {
                android.content.SharedPreferences pos=getSharedPreferences("overlay_position",MODE_PRIVATE);
                JSONObject json=new JSONObject()
                        .put("format","luna-backup")
                        .put("schema_version",1)
                        .put("reports",LunaMemory.reports(this))
                        .put("backend_url",AiClient.backendUrl(this))
                        .put("overlay_position",new JSONObject()
                                .put("x",pos.getInt("x",20)).put("y",pos.getInt("y",250)));
                out.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
                answer.setText("Die vollständige Luna-Datensicherung wurde gespeichert.");
            } catch(Exception e) { answer.setText("Die Luna-Datensicherung konnte nicht gespeichert werden."); }
        }
    }

    private String readText(InputStream in)throws java.io.IOException {
        if(in==null) return "";
        ByteArrayOutputStream out=new ByteArrayOutputStream();
        byte[] buffer=new byte[8192]; int count;
        while((count=in.read(buffer))!=-1 && out.size()<2_000_000) out.write(buffer,0,count);
        return out.toString("UTF-8");
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
        String[] labels={"Stehen","Zuhören","Denken","Sprechen","Winken","Sitzen","Verbeugen","Schlafen","Niesen","An Bildschirm klopfen"};
        String[] states={"idle","listening","thinking","talking","wave","sitting","bowing","sleeping","sneezing","knocking"};
        new AlertDialog.Builder(this).setTitle("Lunas Pose").setItems(labels,(d,which)->setLunaState(states[which])).show();
    }

    private void showReactionPicker() {
        String[] labels={"Winken","Zustimmung","Denken","Überrascht","Entschuldigung","Schlafen","Begeistert","OK"};
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(18),dp(8),dp(18),0);
        ChibiAtlasView preview=new ChibiAtlasView(this); box.addView(preview,new LinearLayout.LayoutParams(-1,dp(250)));
        Spinner spinner=new Spinner(this); spinner.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,labels)); box.addView(spinner);
        Button play=button("Reaktion abspielen",v->preview.playReaction()); box.addView(play);
        spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int position,long id){preview.setFrame(position);preview.playReaction();}
            public void onNothingSelected(android.widget.AdapterView<?> p){}
        });
        new AlertDialog.Builder(this).setTitle("Lunas Chibi-Reaktionen").setView(box).setPositiveButton("Schließen",null).show();
    }

    private void shareChibiToWhatsApp() {
        String[] labels={"Winken","Zustimmung","Denken","Überrascht","Entschuldigung","Schlafen","Begeistert","OK"};
        new AlertDialog.Builder(this).setTitle("Chibi für WhatsApp auswählen").setItems(labels,(d,which)->{
            Uri sticker=Uri.parse("content://"+getPackageName()+".stickers/reaction/"+which);
            Intent send=new Intent(Intent.ACTION_SEND);
            send.setType("image/png");
            send.putExtra(Intent.EXTRA_STREAM,sticker);
            send.putExtra(Intent.EXTRA_TEXT,"Luna: "+labels[which]);
            send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            send.setPackage("com.whatsapp");
            try { startActivity(send); }
            catch (Exception e) {
                send.setPackage(null);
                try { startActivity(Intent.createChooser(send,"Lunas Chibi teilen")); }
                catch (Exception ignored) { answer.setText("Auf diesem Gerät ist keine passende Teilen-App verfügbar."); }
            }
        }).show();
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
                public void onStart(String id){ runOnUiThread(() -> {setLunaState("talking");if(luna3d!=null)luna3d.setExpression("talking");}); }
                public void onDone(String id){ runOnUiThread(() -> {setLunaState("idle");if(luna3d!=null)luna3d.setExpression("idle");}); }
                public void onError(String id){ runOnUiThread(() -> {setLunaState("idle");if(luna3d!=null)luna3d.setExpression("idle");}); }
            });
        }
    }
    @Override protected void onPause(){if(luna3d!=null)luna3d.onPause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(luna3d!=null)luna3d.onResume();}
    @Override protected void onDestroy() { if(tts!=null){tts.stop();tts.shutdown();} super.onDestroy(); }
    private int dp(int x){return(int)(x*getResources().getDisplayMetrics().density);}
}
