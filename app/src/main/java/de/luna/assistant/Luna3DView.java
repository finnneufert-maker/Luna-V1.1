package de.luna.assistant;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import java.nio.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Procedural 3D Luna with separate regular/Chibi proportions and continuous rotation. */
public final class Luna3DView extends GLSurfaceView {
    private final LunaRenderer renderer; private float lastX;
    public Luna3DView(Context c){this(c,null);}
    public Luna3DView(Context c,AttributeSet a){super(c,a);setEGLContextClientVersion(2);renderer=new LunaRenderer();setRenderer(renderer);setRenderMode(RENDERMODE_CONTINUOUSLY);setPreserveEGLContextOnPause(true);}
    public void setExpression(String s){queueEvent(()->renderer.expression=s==null?"idle":s);}
    public boolean toggleChibi(){renderer.chibi=!renderer.chibi;return renderer.chibi;}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==0){lastX=e.getX();return true;}if(e.getAction()==2){renderer.yaw=(renderer.yaw+(.48f*(e.getX()-lastX)))%360f;lastX=e.getX();return true;}if(e.getAction()==1){performClick();return true;}return true;}
    @Override public boolean performClick(){super.performClick();return true;}

    private static final class LunaRenderer implements Renderer {
        private static final String VS="uniform mat4 m;attribute vec3 p;varying float l;void main(){vec3 n=normalize(p);l=.58+.42*max(dot(n,normalize(vec3(-.4,.8,1.))),0.);gl_Position=m*vec4(p,1.);}";
        private static final String FS="precision mediump float;uniform vec4 c;varying float l;void main(){gl_FragColor=vec4(c.rgb*l,c.a);}";
        private int program,pos,mvp,color; private Mesh ball,cone,torso,skirt,limb,panel; private long start; volatile String expression="idle"; volatile float yaw; volatile boolean chibi;
        private final float[] proj=new float[16],view=new float[16],model=new float[16],tmp=new float[16],out=new float[16];
        private float poseY,poseX,poseZ,hairMotion,clothMotion,fall;
        private static final float[] SKIN={.98f,.82f,.79f,1}, SILVER={.84f,.84f,.94f,1}, DARK={.60f,.59f,.74f,1}, DRESS={.075f,.05f,.12f,1}, APRON={.93f,.94f,1,1}, WHITE={1,1,1,1}, PURPLE={.46f,.18f,.64f,1}, EYE={.48f,.18f,.88f,1}, PINK={.9f,.5f,.68f,1}, MOUTH={.48f,.1f,.2f,1}, STOCK={.88f,.9f,.98f,1}, SHOE={.04f,.03f,.07f,1};

        @Override public void onSurfaceCreated(GL10 g,EGLConfig c){GLES20.glClearColor(.075f,.06f,.13f,1);GLES20.glEnable(GLES20.GL_DEPTH_TEST);GLES20.glEnable(GLES20.GL_CULL_FACE);program=link(VS,FS);pos=GLES20.glGetAttribLocation(program,"p");mvp=GLES20.glGetUniformLocation(program,"m");color=GLES20.glGetUniformLocation(program,"c");ball=Mesh.sphere(16,20);cone=Mesh.cone();torso=Mesh.bodyProfile(24);skirt=Mesh.profile(new float[]{.72f,.94f,1.38f,1.55f},24);limb=Mesh.profile(new float[]{.85f,1f,.92f,.72f},12);panel=Mesh.panel();start=System.currentTimeMillis();}
        @Override public void onSurfaceChanged(GL10 g,int w,int h){GLES20.glViewport(0,0,w,h);float r=w/(float)Math.max(1,h);float halfWidth=Math.max(r*1.18f,.82f);Matrix.frustumM(proj,0,-halfWidth,halfWidth,-1.18f,1.18f,2.4f,14);Matrix.setLookAtM(view,0,0,0,5.7f,0,0,0,0,1,0);}
        @Override public void onDrawFrame(GL10 g){
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT|GLES20.GL_DEPTH_BUFFER_BIT);GLES20.glUseProgram(program);
            float t=(System.currentTimeMillis()-start)/1000f,breath=(float)Math.sin(t*1.7f),sway=(float)Math.sin(t*.7f)*2,phase=t%4.4f;
            float blink=phase>4.08f?.06f:1;if("sleeping".equals(expression))blink=.06f;
            float mouth="talking".equals(expression)?.035f+.075f*Math.abs((float)Math.sin(t*10)):.025f;
            // Short idle gestures stay out of the way of speech and explicit expressions.
            boolean idle="idle".equals(expression);
            float knockPhase=t%23f;
            float knock=idle&&knockPhase>18f&&knockPhase<19.1f?Math.max(0,(float)Math.sin((knockPhase-18f)*17f)):("knocking".equals(expression)?Math.max(0,(float)Math.sin(t*9)):0);
            float fallPhase=t%47f;
            float fallEnvelope=idle&&t>15f&&fallPhase>35f&&fallPhase<37.3f?(float)Math.sin(Math.PI*(fallPhase-35f)/2.3f):0;
            fall=fallEnvelope*fallEnvelope;
            float bow="bowing".equals(expression)?28:0,sit="sitting".equals(expression)?-.28f:0;
            float sneeze="sneezing".equals(expression)?(float)Math.sin((t%1.1f)/1.1f*Math.PI):0;
            float wave="wave".equals(expression)?(float)Math.sin(t*7)*22:0;
            float tilt="thinking".equals(expression)?8:sway*.3f,ears="listening".equals(expression)?9:0;
            hairMotion=(float)Math.sin(t*1.35f)*3f+fall*7f;
            clothMotion=(float)Math.sin(t*1.75f)*2.4f+fall*11f;
            poseY=breath*.018f+sit-fall*.57f;
            poseX=bow+fall*23f;
            poseZ=fall*24f;
            if(chibi)chibi(t,blink,mouth,sneeze,knock,wave,tilt,ears,sway);else regular(t,blink,mouth,sneeze,knock,wave,tilt,ears,sway);
        }
        private void regular(float t,float blink,float mouth,float sneeze,float knock,float wave,float tilt,float ears,float sway){
            oval(0,.34f,-.31f,.47f,.77f,.25f,0,0,hairMotion*.35f,SILVER); oval(-.43f,.02f,-.23f,.14f,.68f,.13f,0,0,-4+hairMotion,DARK);oval(.43f,.02f,-.23f,.14f,.68f,.13f,0,0,4+hairMotion,DARK);body(.43f,.65f,.68f,.29f);legs(false,sway);arms(false,sway,knock,wave);head(false,.62f,.48f,.32f,blink,mouth,sneeze,tilt,ears);tail(t,false,sway);}
        private void chibi(float t,float blink,float mouth,float sneeze,float knock,float wave,float tilt,float ears,float sway){
            oval(0,.36f,-.2f,.75f,.78f,.43f,0,0,hairMotion*.35f,SILVER);oval(-.48f,.05f,-.17f,.2f,.54f,.19f,0,0,-5+hairMotion,DARK);oval(.48f,.05f,-.17f,.2f,.54f,.19f,0,0,5+hairMotion,DARK);body(.39f,.46f,.62f,.33f);legs(true,sway);arms(true,sway,knock,wave);head(true,.57f,.59f,.36f,blink,mouth,sneeze,tilt,ears);tail(t,true,sway);}
        private void body(float w,float h,float skirtWidth,float apron){
            // Tapered shoulders, waist and flared skirt keep a distinct human silhouette at every yaw.
            draw(torso,0,-.31f,0,w,h,.43f,0,0,0,DRESS);
            oval(0,-.55f,.01f,w*.77f,.065f,.31f,0,0,0,PURPLE);
            draw(skirt,0,-.80f,0,skirtWidth,.30f,.51f,clothMotion*.35f,0,clothMotion,DRESS);
            oval(0,-.05f,.27f,w*.75f,.09f,.06f,0,0,0,WHITE);
            draw(panel,0,-.34f,.48f,apron,.29f,.065f,0,0,0,APRON);
            draw(panel,0,-.78f,.53f,skirtWidth*.69f,.26f,.10f,clothMotion*.35f,0,clothMotion,APRON);
            oval(0,-1.07f,0,skirtWidth*1.45f,.025f,.49f,0,0,clothMotion,WHITE);
            for(int j=-2;j<=2;j++){float px=j*skirtWidth*.31f;
                draw(panel,px,-.91f,.36f,skirtWidth*.055f,.17f,.05f,clothMotion*.35f,0,clothMotion,j==0?PURPLE:DARK);}
            oval(-w*.58f,-.07f,.24f,.11f,.17f,.11f,0,0,-24,WHITE);
            oval(w*.58f,-.07f,.24f,.11f,.17f,.11f,0,0,24,WHITE);
            oval(-w*.55f,-.38f,-.37f,.23f,.15f,.08f,0,-18,-16,PURPLE);
            oval(w*.55f,-.38f,-.37f,.23f,.15f,.08f,0,18,16,PURPLE);
            oval(0,-.38f,-.4f,.1f,.1f,.07f,0,0,0,WHITE);
            oval(0,-.17f,.35f,.03f,.03f,.02f,0,0,0,PURPLE);
            oval(0,-.33f,.35f,.03f,.03f,.02f,0,0,0,PURPLE);
        }
        private void legs(boolean small,float sway){float x=small?.18f:.21f,y=small?-1.12f:-1.3f,len=small?.3f:.43f;draw(limb,-x,y,.02f,.12f,len,.12f,0,0,-sway,STOCK);draw(limb,x,y,.02f,.12f,len,.12f,0,0,sway,STOCK);float sy=small?-1.38f:-1.62f;oval(-x,sy,.12f,.18f,.11f,.27f,0,0,0,SHOE);oval(x,sy,.12f,.18f,.11f,.27f,0,0,0,SHOE);}
        private void arms(boolean small,float sway,float knock,float wave){
            float x=small?.53f:.56f, wristY=small?-.62f:-.74f;
            for(int side=-1;side<=1;side+=2){
                float ax=side*x, handX=side*(small?.71f:.76f), thrust=knock*.31f;
                float gesture=side>0&&"wave".equals(expression)?-.24f:0;
                oval(ax,-.16f+gesture*.35f,.02f,.18f,.23f,.17f,0,0,side*-12+sway,DRESS);
                oval(ax,-.30f+gesture*.35f,.11f,.15f,.055f,.15f,0,0,0,WHITE);
                oval(side*(x+.07f),-.47f+gesture,.06f+thrust,.105f,small?.22f:.27f,.10f,0,0,side*-19+sway,STOCK);
                oval(handX,wristY+gesture,.075f+thrust,.12f,.055f,.11f,0,0,0,WHITE);
                oval(handX,wristY-.09f+gesture,.085f+thrust,.125f,.13f,.07f,0,0,side*-9,SKIN);
                for(int finger=0;finger<4;finger++){
                    float fx=handX+(finger-1.5f)*.053f;
                    oval(fx,wristY-.215f+gesture+(finger==0||finger==3?.012f:0),.105f+thrust,
                        .025f,.075f,.028f,0,0,side*-4,SKIN);
                }
                oval(handX-side*.13f,wristY-.065f+gesture,.12f+thrust,.038f,.075f,.038f,0,0,side*35,SKIN);
            }
        }
        private void head(boolean small,float y,float hx,float hz,float blink,float mouth,float sneeze,float tilt,float ears){
            float eyeX=small?.25f:.22f, ey=small?.57f:.68f;
            float faceZ=small?.56f:.50f;
            oval(0,y-sneeze*.06f,.23f+sneeze*.1f,hx,small?.61f:.44f,hz,sneeze*10,0,tilt,SKIN);
            oval(0,y+.36f,.02f,hx*1.05f,small?.38f:.30f,hz*1.05f,0,0,tilt,SILVER);
            // Layers of hair frame the face, move slightly, and remain attached when turning.
            oval(-hx*.68f,y+.13f,.17f,.14f,small?.41f:.32f,.13f,0,0,-15+hairMotion*.25f,SILVER);
            oval(hx*.68f,y+.13f,.17f,.14f,small?.41f:.32f,.13f,0,0,15+hairMotion*.25f,SILVER);
            oval(-hx*.35f,y+.27f,.37f,hx*.26f,.19f,.08f,0,0,-19+tilt,DARK);
            oval(hx*.35f,y+.27f,.37f,hx*.26f,.19f,.08f,0,0,19+tilt,DARK);
            float ex=small?.43f:.37f, eyear=small?1.42f:1.35f;
            cone(-ex,eyear,.08f,small?.3f:.27f,small?.38f:.34f,.12f,0,-7-ears,tilt-4,SILVER);
            cone(ex,eyear,.08f,small?.3f:.27f,small?.38f:.34f,.12f,0,7+ears,tilt+4,SILVER);
            cone(-ex,eyear-.03f,.2f,small?.17f:.15f,small?.22f:.19f,.08f,0,-7-ears,tilt-4,PINK);
            cone(ex,eyear-.03f,.2f,small?.17f:.15f,small?.22f:.19f,.08f,0,7+ears,tilt+4,PINK);
            float eyeW=small?.14f:.115f, eyeH=small?.085f:.075f;
            oval(-eyeX,ey,faceZ,eyeW,eyeH*blink,.022f,0,0,tilt,WHITE);
            oval(eyeX,ey,faceZ,eyeW,eyeH*blink,.022f,0,0,tilt,WHITE);
            if(blink>.18f){
                oval(-eyeX,ey,faceZ+.02f,eyeW*.60f,eyeH*.95f,.018f,0,0,tilt,EYE);
                oval(eyeX,ey,faceZ+.02f,eyeW*.60f,eyeH*.95f,.018f,0,0,tilt,EYE);
                oval(-eyeX,ey,faceZ+.039f,eyeW*.18f,eyeH*.71f,.012f,0,0,tilt,DRESS);
                oval(eyeX,ey,faceZ+.039f,eyeW*.18f,eyeH*.71f,.012f,0,0,tilt,DRESS);
                oval(-eyeX-.025f,ey+.027f,faceZ+.053f,.021f,.025f,.009f,0,0,tilt,WHITE);
                oval(eyeX-.025f,ey+.027f,faceZ+.053f,.021f,.025f,.009f,0,0,tilt,WHITE);
            }
            oval(-eyeX,ey+.13f,faceZ-.025f,eyeW*.8f,.018f,.018f,0,0,-8+tilt,DARK);
            oval(eyeX,ey+.13f,faceZ-.025f,eyeW*.8f,.018f,.018f,0,0,8+tilt,DARK);
            oval(0,ey-.18f,faceZ+.015f,.034f,.022f,.025f,0,0,0,PINK);
            oval(0,ey-.31f,faceZ+.018f,.068f,mouth,.02f,0,0,tilt,MOUTH);
            oval(0,eyear-.2f,-.02f,small?.63f:.56f,.09f,small?.4f:.35f,0,0,tilt,WHITE);
        }
        private void tail(float t,boolean small,float sway){
            float x=small?.49f:.57f,y=small?-.68f:-.78f;
            for(int i=0;i<10;i++){
                float q=t*1.25f-i*.2f;
                float wave=(float)Math.sin(q)*(.02f+i*.026f);
                float radius=.145f-i*.006f;
                oval(x+i*(small?.08f:.095f)+wave,y+i*(small?.074f:.084f)+wave*.35f,-.31f,
                    radius*.95f,radius*1.35f,radius*.85f,0,0,-38+sway+wave*15f,DARK);
            }
        }
        private void oval(float x,float y,float z,float sx,float sy,float sz,float rx,float ry,float rz,float[] c){draw(ball,x,y,z,sx,sy,sz,rx,ry,rz,c);}private void cone(float x,float y,float z,float sx,float sy,float sz,float rx,float ry,float rz,float[] c){draw(cone,x,y,z,sx,sy,sz,rx,ry,rz,c);}
        private void draw(Mesh mesh,float x,float y,float z,float sx,float sy,float sz,float rx,float ry,float rz,float[] c){Matrix.setIdentityM(model,0);Matrix.rotateM(model,0,yaw,0,1,0);Matrix.translateM(model,0,0,poseY,0);Matrix.rotateM(model,0,poseX,1,0,0);Matrix.rotateM(model,0,poseZ,0,0,1);Matrix.translateM(model,0,x,y,z);Matrix.rotateM(model,0,rx,1,0,0);Matrix.rotateM(model,0,ry,0,1,0);Matrix.rotateM(model,0,rz,0,0,1);Matrix.scaleM(model,0,sx,sy,sz);Matrix.multiplyMM(tmp,0,view,0,model,0);Matrix.multiplyMM(out,0,proj,0,tmp,0);GLES20.glUniformMatrix4fv(mvp,1,false,out,0);GLES20.glUniform4f(color,c[0],c[1],c[2],c[3]);mesh.draw(pos);}
        private static int link(String a,String b){int x=compile(GLES20.GL_VERTEX_SHADER,a),y=compile(GLES20.GL_FRAGMENT_SHADER,b),p=GLES20.glCreateProgram();GLES20.glAttachShader(p,x);GLES20.glAttachShader(p,y);GLES20.glLinkProgram(p);return p;}private static int compile(int t,String s){int x=GLES20.glCreateShader(t);GLES20.glShaderSource(x,s);GLES20.glCompileShader(x);return x;}
    }
    private static final class Mesh {
        final FloatBuffer v;final ShortBuffer i;final int n;
        Mesh(float[]a,short[]b){v=ByteBuffer.allocateDirect(a.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();v.put(a).position(0);i=ByteBuffer.allocateDirect(b.length*2).order(ByteOrder.nativeOrder()).asShortBuffer();i.put(b).position(0);n=b.length;}
        void draw(int p){v.position(0);i.position(0);GLES20.glEnableVertexAttribArray(p);GLES20.glVertexAttribPointer(p,3,GLES20.GL_FLOAT,false,12,v);GLES20.glDrawElements(GLES20.GL_TRIANGLES,n,GLES20.GL_UNSIGNED_SHORT,i);GLES20.glDisableVertexAttribArray(p);}
        static Mesh sphere(int a,int b){float[]v=new float[(a+1)*(b+1)*3];int k=0;for(int x=0;x<=a;x++){double q=Math.PI*x/a;for(int y=0;y<=b;y++){double r=2*Math.PI*y/b;v[k++]=(float)(Math.sin(q)*Math.cos(r));v[k++]=(float)Math.cos(q);v[k++]=(float)(Math.sin(q)*Math.sin(r));}}short[]z=new short[a*b*6];k=0;for(int x=0;x<a;x++)for(int y=0;y<b;y++){short u=(short)(x*(b+1)+y),w=(short)(u+b+1);z[k++]=u;z[k++]=(short)(u+1);z[k++]=w;z[k++]=(short)(w+1);z[k++]=w;z[k++]=(short)(u+1);}return new Mesh(v,z);}
        static Mesh profile(float[] radii,int sides){int rings=radii.length;float[]v=new float[rings*(sides+1)*3];int k=0;for(int r=0;r<rings;r++){float y=1f-2f*r/(rings-1f);for(int s=0;s<=sides;s++){double a=2*Math.PI*s/sides;v[k++]=(float)Math.cos(a)*radii[r];v[k++]=y;v[k++]=(float)Math.sin(a)*radii[r];}}short[]ix=new short[(rings-1)*sides*6];k=0;for(int r=0;r<rings-1;r++)for(int s=0;s<sides;s++){short a=(short)(r*(sides+1)+s),b=(short)(a+sides+1);ix[k++]=a;ix[k++]=(short)(a+1);ix[k++]=b;ix[k++]=(short)(a+1);ix[k++]=(short)(b+1);ix[k++]=b;}return new Mesh(v,ix);}
        static Mesh bodyProfile(int sides){
            // Shoulder, waist and hip contours; depth differs from width at each ring.
            float[]width={.74f,.91f,.81f,.65f,.88f};
            float[]depth={.72f,.90f,.96f,.78f,1.01f};
            int rings=width.length,k=0;float[]v=new float[rings*(sides+1)*3];
            for(int r=0;r<rings;r++)for(int a=0;a<=sides;a++){
                double angle=2*Math.PI*a/sides;
                v[k++]=(float)Math.cos(angle)*width[r];
                v[k++]=1f-2f*r/(rings-1f);
                v[k++]=(float)Math.sin(angle)*depth[r];
            }
            short[]ix=new short[(rings-1)*sides*6];k=0;
            for(int r=0;r<rings-1;r++)for(int a=0;a<sides;a++){
                short top=(short)(r*(sides+1)+a),bottom=(short)(top+sides+1);
                ix[k++]=top;ix[k++]=(short)(top+1);ix[k++]=bottom;
                ix[k++]=(short)(top+1);ix[k++]=(short)(bottom+1);ix[k++]=bottom;
            }
            return new Mesh(v,ix);
        }
        static Mesh panel(){
            // A curved, double-sided fabric panel instead of a spherical apron.
            int rows=8, cols=10;float[]v=new float[(rows+1)*(cols+1)*3];int k=0;
            for(int y=0;y<=rows;y++)for(int x=0;x<=cols;x++){
                float a=2f*x/cols-1f,b=1f-2f*y/rows;
                v[k++]=a*(.95f-.07f*b);v[k++]=b;v[k++]=.26f*(1f-a*a);
            }
            short[]ix=new short[rows*cols*12];k=0;
            for(int y=0;y<rows;y++)for(int x=0;x<cols;x++){
                short a=(short)(y*(cols+1)+x),b=(short)(a+cols+1);
                ix[k++]=a;ix[k++]=b;ix[k++]=(short)(a+1);
                ix[k++]=(short)(a+1);ix[k++]=b;ix[k++]=(short)(b+1);
                ix[k++]=(short)(a+1);ix[k++]=b;ix[k++]=a;
                ix[k++]=(short)(b+1);ix[k++]=b;ix[k++]=(short)(a+1);
            }
            return new Mesh(v,ix);
        }
        static Mesh cone(){float[]v={0,1,0,-1,-1,1,1,-1,1,1,-1,-1,-1,-1,-1};short[]i={0,1,2,0,2,3,0,3,4,0,4,1,1,4,3,1,3,2};return new Mesh(v,i);}
    }
}
