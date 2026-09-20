package de.luna.assistant;

import org.json.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Talks only to Luna's own backend. No OpenAI key is stored in the APK. */
final class AiClient {
    interface Callback { void onSuccess(String answer); void onError(String message); }
    private static final ExecutorService EXECUTOR=Executors.newSingleThreadExecutor();

    static boolean isConfigured(){
        return BuildConfig.LUNA_BACKEND_URL!=null && BuildConfig.LUNA_BACKEND_URL.startsWith("https://");
    }

    static void ask(String question,Callback callback){
        EXECUTOR.execute(()->{
            HttpURLConnection connection=null;
            try{
                URL url=new URL(BuildConfig.LUNA_BACKEND_URL.replaceAll("/$","")+"/v1/luna/answer");
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
