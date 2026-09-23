package de.luna.assistant;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Displays one compact chibi reaction from Luna's 4x2 atlas. */
public final class ChibiAtlasView extends View {
    private final Bitmap[] reactions = new Bitmap[8];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private int frame;

    public ChibiAtlasView(Context context) {
        super(context);
        int[] ids={R.drawable.luna_emoji_wave,R.drawable.luna_emoji_thumbsup,
                R.drawable.luna_emoji_thinking,R.drawable.luna_emoji_surprised,
                R.drawable.luna_emoji_sorry,R.drawable.luna_emoji_sleep,
                R.drawable.luna_emoji_excited,R.drawable.luna_emoji_ok};
        for(int i=0;i<ids.length;i++) reactions[i]=BitmapFactory.decodeResource(getResources(),ids[i]);
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
        Bitmap reaction=reactions[frame];
        Rect src=new Rect(0,0,reaction.getWidth(),reaction.getHeight());
        float scale=Math.min(getWidth()/(float)src.width(),getHeight()/(float)src.height());
        float w=src.width()*scale,h=src.height()*scale;
        RectF dst=new RectF((getWidth()-w)/2f,(getHeight()-h)/2f,(getWidth()+w)/2f,(getHeight()+h)/2f);
        canvas.drawBitmap(reaction,src,dst,paint);
    }
}
