package de.luna.assistant;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Lightweight code-built 3D Luna: no external model file or heavy engine required. */
public final class Luna3DView extends GLSurfaceView {
    private final LunaRenderer renderer;

    public Luna3DView(Context context) { this(context, null); }
    public Luna3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        renderer = new LunaRenderer();
        setRenderer(renderer);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
        setPreserveEGLContextOnPause(true);
    }

    public void setExpression(String state) { queueEvent(() -> renderer.expression = state == null ? "idle" : state); }

    private static final class LunaRenderer implements Renderer {
        private static final String VS = "uniform mat4 uMvp;attribute vec3 aPos;void main(){gl_Position=uMvp*vec4(aPos,1.0);}";
        private static final String FS = "precision mediump float;uniform vec4 uColor;void main(){gl_FragColor=uColor;}";
        private int program, position, mvpHandle, colorHandle;
        private Mesh sphere, ear;
        private final float[] projection=new float[16], view=new float[16], model=new float[16], temp=new float[16], mvp=new float[16];
        private long started;
        volatile String expression="idle";

        @Override public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0.09f,0.075f,0.15f,1f);
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
            program=link(VS,FS);
            position=GLES20.glGetAttribLocation(program,"aPos");
            mvpHandle=GLES20.glGetUniformLocation(program,"uMvp");
            colorHandle=GLES20.glGetUniformLocation(program,"uColor");
            sphere=Mesh.sphere(14,18);
            ear=Mesh.pyramid();
            started=System.currentTimeMillis();
        }

        @Override public void onSurfaceChanged(GL10 gl,int width,int height) {
            GLES20.glViewport(0,0,width,height);
            float ratio=width/(float)Math.max(1,height);
            Matrix.frustumM(projection,0,-ratio,ratio,-1,1,2.4f,16f);
            Matrix.setLookAtM(view,0,0,0.05f,6.2f,0,-0.05f,0,0,1,0);
        }

        @Override public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);
            GLES20.glUseProgram(program);
            float t=(System.currentTimeMillis()-started)/1000f;
            float breathe=(float)Math.sin(t*1.8f)*0.018f;
            float bob=(float)Math.sin(t*1.8f)*0.018f;
            float sway=(float)Math.sin(t*0.7f)*2.0f;
            float step=(float)Math.sin(t*0.7f)*0.018f;
            float blink=((t%4.2f)>3.95f)?0.08f:1f;
            if("surprised".equals(expression)) blink=1.35f;
            if("sleeping".equals(expression)) blink=0.06f;
            float talk=("talking".equals(expression))?(0.06f+0.08f*Math.abs((float)Math.sin(t*10f))):0.035f;

            // Joined full-body silhouette: hair, legs, dress, torso and arms share one gentle bob.
            part(0,0.28f+bob,-0.12f,0.64f,1.10f,0.34f,sway,0.78f,0.76f,0.88f,1);
            part(-0.20f,-1.28f+bob+step,0.01f,0.14f,0.39f,0.14f,-1,0.94f,0.94f,0.98f,1);
            part(0.20f,-1.28f+bob-step,0.01f,0.14f,0.39f,0.14f,1,0.94f,0.94f,0.98f,1);
            part(-0.20f,-1.59f+bob+step,0.11f,0.20f,0.13f,0.30f,-2,0.07f,0.05f,0.09f,1);
            part(0.20f,-1.59f+bob-step,0.11f,0.20f,0.13f,0.30f,2,0.07f,0.05f,0.09f,1);
            part(0,-0.79f+bob,0.00f,0.66f,0.47f,0.39f,sway*.25f,0.10f,0.075f,0.14f,1);
            part(0,-0.39f+bob,0.02f,0.43f+breathe,0.55f+breathe,0.28f,sway*.30f,0.11f,0.085f,0.16f,1);
            part(0,-0.59f+bob,0.31f,0.35f,0.43f,0.09f,sway*.25f,0.95f,0.95f,0.99f,1);
            part(0,-0.94f+bob,0.24f,0.59f,0.075f,0.10f,sway*.25f,0.97f,0.97f,1,1);
            part(0,0.02f+bob,0.25f,0.33f,0.10f,0.09f,sway,0.96f,0.96f,1,1);
            part(-0.43f,-0.18f+bob,0.02f,0.22f,0.22f,0.20f,8+sway,0.96f,0.96f,0.99f,1);
            part(0.43f,-0.18f+bob,0.02f,0.22f,0.22f,0.20f,-8-sway,0.96f,0.96f,0.99f,1);
            part(-0.52f,-0.52f+bob,0.03f,0.12f,0.40f,0.12f,8+sway,0.97f,0.84f,0.81f,1);
            part(0.52f,-0.52f+bob,0.03f,0.12f,0.40f,0.12f,-8-sway,0.97f,0.84f,0.81f,1);
            part(-0.57f,-0.82f+bob,0.05f,0.14f,0.15f,0.13f,8+sway,0.97f,0.84f,0.81f,1);
            part(0.57f,-0.82f+bob,0.05f,0.14f,0.15f,0.13f,-8-sway,0.97f,0.84f,0.81f,1);

            // Neck and overlapping hair remove the hard seam around the head.
            part(0,0.17f+bob,0.08f,0.16f,0.22f,0.16f,sway,0.97f,0.84f,0.81f,1);
            part(0,0.70f+bob,0.18f,0.61f,0.65f,0.48f,sway,0.97f,0.84f,0.81f,1);
            part(0,0.97f+bob,0.00f,0.66f,0.51f,0.47f,sway,0.86f,0.84f,0.94f,1);
            part(-0.25f,0.98f+bob,0.48f,0.31f,0.22f,0.10f,-16+sway,0.86f,0.84f,0.94f,1);
            part(0.22f,0.99f+bob,0.48f,0.34f,0.22f,0.10f,14+sway,0.86f,0.84f,0.94f,1);
            pyramid(-0.39f,1.43f+bob,0.04f,0.28f,0.38f,0.22f,sway-4,0.82f,0.80f,0.91f,1);
            pyramid(0.39f,1.43f+bob,0.04f,0.28f,0.38f,0.22f,sway+4,0.82f,0.80f,0.91f,1);
            pyramid(-0.39f,1.43f+bob,0.25f,0.14f,0.22f,0.07f,sway-4,0.82f,0.48f,0.63f,1);
            pyramid(0.39f,1.43f+bob,0.25f,0.14f,0.22f,0.07f,sway+4,0.82f,0.48f,0.63f,1);
            part(-0.22f,0.72f+bob,0.65f,0.12f,0.15f*blink,0.035f,sway,0.54f,0.27f,0.91f,1);
            part(0.22f,0.72f+bob,0.65f,0.12f,0.15f*blink,0.035f,sway,0.54f,0.27f,0.91f,1);
            part(-0.22f,0.72f+bob,0.69f,0.035f,0.11f*blink,0.018f,sway,0.09f,0.05f,0.12f,1);
            part(0.22f,0.72f+bob,0.69f,0.035f,0.11f*blink,0.018f,sway,0.09f,0.05f,0.12f,1);
            part(0,0.55f+bob,0.67f,0.045f,0.035f,0.025f,sway,0.88f,0.66f,0.67f,1);
            part(0,0.42f+bob,0.67f,0.075f,talk,0.025f,sway,0.49f,0.13f,0.23f,1);
            // Tail: animated chain of rounded segments.
            for(int i=0;i<5;i++){
                float a=t*1.5f+i*0.45f;
                float x=0.57f+i*0.14f+(float)Math.sin(a)*0.04f;
                float y=-0.76f+bob+i*0.13f+(float)Math.cos(a)*0.035f;
                part(x,y,-0.12f,0.19f,0.22f,0.16f,sway,0.82f,0.80f,0.90f,1);
            }
        }

        private void part(float x,float y,float z,float sx,float sy,float sz,float rz,float r,float g,float b,float a){
            draw(sphere,x,y,z,sx,sy,sz,rz,r,g,b,a);
        }
        private void pyramid(float x,float y,float z,float sx,float sy,float sz,float rz,float r,float g,float b,float a){
            draw(ear,x,y,z,sx,sy,sz,rz,r,g,b,a);
        }
        private void draw(Mesh mesh,float x,float y,float z,float sx,float sy,float sz,float rz,float r,float g,float b,float a){
            Matrix.setIdentityM(model,0);
            Matrix.translateM(model,0,x,y,z);
            Matrix.rotateM(model,0,rz,0,1,0);
            Matrix.scaleM(model,0,sx,sy,sz);
            Matrix.multiplyMM(temp,0,view,0,model,0);
            Matrix.multiplyMM(mvp,0,projection,0,temp,0);
            GLES20.glUniformMatrix4fv(mvpHandle,1,false,mvp,0);
            GLES20.glUniform4f(colorHandle,r,g,b,a);
            mesh.draw(position);
        }

        private static int link(String vs,String fs){
            int v=compile(GLES20.GL_VERTEX_SHADER,vs), f=compile(GLES20.GL_FRAGMENT_SHADER,fs);
            int p=GLES20.glCreateProgram(); GLES20.glAttachShader(p,v); GLES20.glAttachShader(p,f); GLES20.glLinkProgram(p); return p;
        }
        private static int compile(int type,String source){
            int s=GLES20.glCreateShader(type); GLES20.glShaderSource(s,source); GLES20.glCompileShader(s); return s;
        }
    }

    private static final class Mesh {
        final FloatBuffer vertices; final ShortBuffer indices; final int count;
        Mesh(float[] v,short[] i){
            vertices=ByteBuffer.allocateDirect(v.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer(); vertices.put(v).position(0);
            indices=ByteBuffer.allocateDirect(i.length*2).order(ByteOrder.nativeOrder()).asShortBuffer(); indices.put(i).position(0); count=i.length;
        }
        void draw(int position){
            vertices.position(0); indices.position(0);
            GLES20.glEnableVertexAttribArray(position);
            GLES20.glVertexAttribPointer(position,3,GLES20.GL_FLOAT,false,12,vertices);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,count,GLES20.GL_UNSIGNED_SHORT,indices);
            GLES20.glDisableVertexAttribArray(position);
        }
        static Mesh sphere(int stacks,int slices){
            float[] v=new float[(stacks+1)*(slices+1)*3]; int k=0;
            for(int i=0;i<=stacks;i++){
                double phi=Math.PI*i/stacks;
                for(int j=0;j<=slices;j++){
                    double th=2*Math.PI*j/slices;
                    v[k++]=(float)(Math.sin(phi)*Math.cos(th)); v[k++]=(float)Math.cos(phi); v[k++]=(float)(Math.sin(phi)*Math.sin(th));
                }
            }
            short[] ix=new short[stacks*slices*6]; k=0;
            for(int i=0;i<stacks;i++) for(int j=0;j<slices;j++){
                short a=(short)(i*(slices+1)+j), b=(short)(a+slices+1);
                ix[k++]=a;ix[k++]=b;ix[k++]=(short)(a+1);ix[k++]=(short)(a+1);ix[k++]=b;ix[k++]=(short)(b+1);
            }
            return new Mesh(v,ix);
        }
        static Mesh pyramid(){
            float[] v={0,1,0,-1,-1,1,1,-1,1,1,-1,-1,-1,-1,-1};
            short[] i={0,1,2,0,2,3,0,3,4,0,4,1,1,4,3,1,3,2}; return new Mesh(v,i);
        }
    }
}
