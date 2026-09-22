package de.luna.assistant;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Displays one compact chibi reaction from Luna's 4x2 atlas. */
public final class ChibiAtlasView extends View {
    private final Bitmap atlas;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private int frame;

    public ChibiAtlasView(Context context) {
        super(context);
        // Reuse the installed full-body atlas so the APK has no loose/missing artwork.
        atlas = BitmapFactory.decodeResource(getResources(), R.drawable.luna_sprite_atlas_v2);
    }

    public void setFrame(int value) {
        frame = Math.max(0, Math.min(7, value));
        invalidate();
    }

    public void playReaction() {
        animate().cancel();
        setScaleX(.88f); setScaleY(.88f); setRotation(-3f);
        animate().scaleX(1.08f).scaleY(1.08f).rotation(3f).setDuration(180)
                .withEndAction(() -> animate().scaleX(1f).scaleY(1f).rotation(0f).setDuration(180));
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int col=frame%4, row=frame/4;
        int left=Math.round(col*atlas.getWidth()/4f), right=Math.round((col+1)*atlas.getWidth()/4f);
        int top=Math.round(row*atlas.getHeight()/2f), bottom=Math.round((row+1)*atlas.getHeight()/2f);
        Rect src=new Rect(left,top,right,bottom);
        float scale=Math.min(getWidth()/(float)src.width(),getHeight()/(float)src.height());
        float w=src.width()*scale,h=src.height()*scale;
        RectF dst=new RectF((getWidth()-w)/2f,(getHeight()-h)/2f,(getWidth()+w)/2f,(getHeight()+h)/2f);
        canvas.drawBitmap(atlas,src,dst,paint);
    }
}
