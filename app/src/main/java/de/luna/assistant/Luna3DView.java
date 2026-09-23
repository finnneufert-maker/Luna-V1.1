package de.luna.assistant;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/** Battery-friendly 3D-rendered turntable with four checked, full-body views. */
public final class Luna3DView extends View {
    private final Bitmap[] views = new Bitmap[4];
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private float downX, drag;
    private int angle;
    private boolean running = true;
    private long started = SystemClock.uptimeMillis();

    public Luna3DView(Context context) { this(context, null); }
    public Luna3DView(Context context, AttributeSet attrs) {
        super(context, attrs);
        views[0] = BitmapFactory.decodeResource(getResources(), R.drawable.luna_3d_front);
        views[1] = BitmapFactory.decodeResource(getResources(), R.drawable.luna_3d_threequarter);
        views[2] = BitmapFactory.decodeResource(getResources(), R.drawable.luna_3d_side);
        views[3] = BitmapFactory.decodeResource(getResources(), R.drawable.luna_3d_back);
        setContentDescription("Luna als vollständige 3D-Ansicht; zum Drehen nach links oder rechts wischen");
    }

    public void setExpression(String state) {
        if ("thinking".equals(state)) angle = 1;
        else if ("listening".equals(state)) angle = 2;
        else if ("idle".equals(state)) angle = 0;
        invalidate();
    }

    public void onPause() { running = false; }
    public void onResume() { running = true; started = SystemClock.uptimeMillis(); invalidate(); }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Bitmap bitmap = views[Math.max(0, Math.min(angle, views.length - 1))];
        if (bitmap == null) return;
        float t = (SystemClock.uptimeMillis() - started) / 1000f;
        float bob = running ? (float)Math.sin(t * 1.45f) * getHeight() * .006f : 0f;
        float pulse = running ? 1f + (float)Math.sin(t * 1.45f) * .006f : 1f;
        float scale = Math.min(getWidth() * .94f / bitmap.getWidth(), getHeight() * .96f / bitmap.getHeight()) * pulse;
        float w = bitmap.getWidth() * scale, h = bitmap.getHeight() * scale;
        float left = (getWidth() - w) / 2f, top = (getHeight() - h) / 2f + bob;
        canvas.drawBitmap(bitmap, null, new RectF(left, top, left + w, top + h), paint);
        if (running) postInvalidateOnAnimation();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) { downX = event.getX(); drag = 0; return true; }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            drag += event.getX() - downX; downX = event.getX();
            if (Math.abs(drag) >= Math.max(55f, getWidth() * .12f)) {
                angle = (angle + (drag < 0 ? 1 : 3)) % 4; drag = 0; invalidate();
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) { performClick(); return true; }
        return super.onTouchEvent(event);
    }

    @Override public boolean performClick() { super.performClick(); angle = (angle + 1) % 4; invalidate(); return true; }
}
