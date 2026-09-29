package de.luna.assistant;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;

/** Loads the authored Luna OBJ asset. Each named mesh can move independently. */
final class LunaObjModel {
    private static final class Part {
        String name;
        float[] color;
        FloatBuffer vertices,normals;
        int count;
    }
    private final ArrayList<Part> parts = new ArrayList<>();
    private static FloatBuffer buffer(float[] data) {
        FloatBuffer b=ByteBuffer.allocateDirect(data.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        b.put(data).position(0);return b;
    }
    static LunaObjModel load(Context context,String asset) throws Exception {
        LunaObjModel model=new LunaObjModel();
        ArrayList<float[]> positions=new ArrayList<>(),normals=new ArrayList<>();
        ArrayList<Float> points=new ArrayList<>(),directions=new ArrayList<>();
        String partName="body",material="dress";
        HashMap<String,float[]> palette=new HashMap<>();
        palette.put("skin",new float[]{.98f,.79f,.75f,1});palette.put("hair",new float[]{.85f,.85f,.95f,1});
        palette.put("nail",new float[]{1f,.84f,.85f,1});
        palette.put("hairlight",new float[]{.96f,.95f,1,1});palette.put("dress",new float[]{.065f,.045f,.11f,1});
        palette.put("apron",new float[]{.93f,.94f,1,1});palette.put("purple",new float[]{.43f,.16f,.61f,1});
        palette.put("pink",new float[]{.87f,.48f,.63f,1});palette.put("eye",new float[]{.47f,.16f,.78f,1});
        palette.put("black",new float[]{.035f,.025f,.07f,1});palette.put("white",new float[]{1,1,1,1});
        palette.put("stock",new float[]{.86f,.87f,.96f,1});
        try(BufferedReader reader=new BufferedReader(new InputStreamReader(context.getAssets().open(asset)))) {
            String line;
            while((line=reader.readLine())!=null){
                String[] s=line.trim().split("\\s+");if(s.length<2)continue;
                switch(s[0]){
                    case "v":positions.add(new float[]{Float.parseFloat(s[1]),Float.parseFloat(s[2]),Float.parseFloat(s[3])});break;
                    case "vn":normals.add(new float[]{Float.parseFloat(s[1]),Float.parseFloat(s[2]),Float.parseFloat(s[3])});break;
                    case "o":case "g":model.finish(partName,material,palette,points,directions);partName=s[1];break;
                    case "usemtl":model.finish(partName,material,palette,points,directions);material=s[1];break;
                    case "f":
                        for(int corner=2;corner<s.length-1;corner++){
                            int[] fan={1,corner,corner+1};
                            for(int c:fan){String[] ref=s[c].split("/",-1);
                                float[] p=positions.get(Integer.parseInt(ref[0])-1);
                                float[] n=normals.get(Integer.parseInt(ref[ref.length-1])-1);
                                for(float v:p)points.add(v);for(float v:n)directions.add(v);
                            }
                        }break;
                }
            }
        }
        model.finish(partName,material,palette,points,directions);
        if(model.parts.isEmpty())throw new IllegalStateException("Empty Luna model: "+asset);
        return model;
    }
    private void finish(String name,String material,HashMap<String,float[]> palette,ArrayList<Float> points,ArrayList<Float> normals){
        if(points.isEmpty())return;
        float[] p=new float[points.size()],n=new float[normals.size()];
        for(int j=0;j<p.length;j++)p[j]=points.get(j);
        for(int j=0;j<n.length;j++)n[j]=normals.get(j);
        Part part=new Part();part.name=name;part.color=palette.getOrDefault(material,palette.get("dress"));
        part.vertices=buffer(p);part.normals=buffer(n);part.count=p.length/3;parts.add(part);
        points.clear();normals.clear();
    }
    void draw(int position,int normal,int matrix,int normalMatrix,int color,float[] projection,float[] view,
              float yaw,float pitch,float time,float blink,float speech){
        float[] transform=new float[16],tmp=new float[16],mvp=new float[16];
        for(Part part:parts){
            Matrix.setIdentityM(transform,0);
            Matrix.rotateM(transform,0,yaw,0,1,0);Matrix.rotateM(transform,0,pitch,1,0,0);
            Matrix.translateM(transform,0,0,(float)Math.sin(time*1.7f)*.018f,0);
            if(part.name.startsWith("tail")){
                Matrix.translateM(transform,0,.48f,-.70f,-.28f);
                Matrix.rotateM(transform,0,(float)Math.sin(time*1.3f)*9f,0,0,1);
                Matrix.translateM(transform,0,-.48f,.70f,.28f);
            }
            if(part.name.startsWith("hair"))Matrix.rotateM(transform,0,(float)Math.sin(time*1.1f)*.4f,0,0,1);
            if(part.name.startsWith("eye_")||part.name.startsWith("iris_")||part.name.startsWith("pupil_")||part.name.startsWith("eyelight_")||part.name.startsWith("eyelash_")){
                Matrix.translateM(transform,0,0,.75f,0);
                Matrix.scaleM(transform,0,1,Math.max(.06f,blink),1);
                Matrix.translateM(transform,0,0,-.75f,0);
            }
            if(part.name.equals("mouth")){
                Matrix.translateM(transform,0,0,.51f,0);
                Matrix.scaleM(transform,0,1,1+speech*8f,1);
                Matrix.translateM(transform,0,0,-.51f,0);
            }
            GLES20.glUniformMatrix4fv(normalMatrix,1,false,transform,0);
            Matrix.multiplyMM(tmp,0,view,0,transform,0);Matrix.multiplyMM(mvp,0,projection,0,tmp,0);
            GLES20.glUniformMatrix4fv(matrix,1,false,mvp,0);
            float[] c=part.color;GLES20.glUniform4f(color,c[0],c[1],c[2],c[3]);
            part.vertices.position(0);part.normals.position(0);
            GLES20.glEnableVertexAttribArray(position);GLES20.glEnableVertexAttribArray(normal);
            GLES20.glVertexAttribPointer(position,3,GLES20.GL_FLOAT,false,12,part.vertices);
            GLES20.glVertexAttribPointer(normal,3,GLES20.GL_FLOAT,false,12,part.normals);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,part.count);
        }
        GLES20.glDisableVertexAttribArray(position);GLES20.glDisableVertexAttribArray(normal);
    }
}
