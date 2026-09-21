package de.luna.assistant;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Talks only to Luna's own backend. No OpenAI key is stored in the APK. */
final class AiClient {
    private static final String PREFS="luna_ai";
    private static final String KEY_URL="backend_url";
    interface Callback { void onSuccess(String answer); void onError(String message); }
    private static final ExecutorService EXECUTOR=Executors.newSingleThreadExecutor();

    static String backendUrl(Context context){
        SharedPreferences prefs=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        String saved=prefs.getString(KEY_URL,"").trim();
        if(saved.startsWith("https://")) return saved.replaceAll("/$","");
        String built=BuildConfig.LUNA_BACKEND_URL==null?"":BuildConfig.LUNA_BACKEND_URL.trim();
        return built.startsWith("https://")?built.replaceAll("/$",""):"";
    }

    static boolean isConfigured(Context context){ return !backendUrl(context).isEmpty(); }

    static boolean saveBackendUrl(Context context,String value){
        String url=value==null?"":value.trim().replaceAll("/$","");
        if(!url.isEmpty()&&!url.startsWith("https://")) return false;
        context.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(KEY_URL,url).apply();
        return true;
    }

    static void ask(Context context,String question,Callback callback){
        final String endpoint=backendUrl(context);
        EXECUTOR.execute(()->{
            HttpURLConnection connection=null;
            try{
                if(endpoint.isEmpty()) throw new IOException("Keine Serveradresse");
                URL url=new URL(endpoint+"/v1/luna/answer");
                connection=(HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST"); connection.setConnectTimeout(12_000); connection.setReadTimeout(45_000);
                connection.setDoOutput(true); connection.setRequestProperty("Content-Type","application/json; charset=utf-8");
                byte[] body=new JSONObject().put("question",question).toString().getBytes(StandardCharsets.UTF_8);
                try(OutputStream out=connection.getOutputStream()){out.write(body);}
                int status=connection.getResponseCode();
                InputStream stream=status>=200&&status<300?connection.getInputStream():connection.getErrorStream();
                String response=read(stream);
                if(status<200||status>=300) throw new IOException("Serverstatus "+status);
                String answer=new JSONObject(response).optString("answer","").trim();
                if(answer.isEmpty()) throw new IOException("Leere Antwort");
                callback.onSuccess(answer);
            }catch(Exception e){callback.onError("Die Online-KI ist gerade nicht erreichbar.");}
            finally{if(connection!=null)connection.disconnect();}
        });
    }

    private static String read(InputStream input)throws IOException{
        if(input==null)return "";
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(input,StandardCharsets.UTF_8))){
            StringBuilder result=new StringBuilder(); String line;
            while((line=reader.readLine())!=null && result.length()<100_000)result.append(line);
            return result.toString();
        }
    }
}
