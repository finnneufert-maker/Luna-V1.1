package de.luna.assistant;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;

public class OverlayService extends Service {
    public static final String ACTION_STATE = "de.luna.assistant.STATE";
    public static final String EXTRA_STATE = "state";
    private static final String CHANNEL = "luna_overlay";
    private WindowManager windowManager;
    private View luna;
    private ObjectAnimator idleAnimator;
    private final Handler inactivity = new Handler(Looper.getMainLooper());
    private final Runnable sleepTask = () -> setState("sleeping");

    @Override public void onCreate() {
        super.onCreate();
        createChannel();
        Intent open = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_IMMUTABLE);
        Notification n = new Notification.Builder(this, CHANNEL)
                .setContentTitle("Luna ist aktiv")
                .setContentText("Tippe die Figur an, um Luna zu öffnen.")
                .setSmallIcon(de.luna.assistant.R.drawable.luna_icon)
                .setContentIntent(pi).build();
        startForeground(7, n);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Luna3DView image = new Luna3DView(this);
        image.setBackgroundResource(R.drawable.overlay_frame);
        int type = Build.VERSION.SDK_INT >= 26 ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams p = new WindowManager.LayoutParams(dp(150), dp(230), type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        p.gravity = Gravity.TOP | Gravity.START;
        android.content.SharedPreferences pos = getSharedPreferences("overlay_position", MODE_PRIVATE);
        p.x = pos.getInt("x", 20); p.y = pos.getInt("y", 250);
        image.setOnTouchListener(new DragListener(p));
        image.setOnClickListener(v -> {
            setState("knocking");
            inactivity.postDelayed(() -> {
                startActivity(open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                setState("idle"); resetSleepTimer();
            }, 480L);
        });
        luna = image;
        windowManager.addView(luna, p);
        setState("idle");
        resetSleepTimer();
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STATE.equals(intent.getAction())) {
            setState(intent.getStringExtra(EXTRA_STATE));
        }
        return START_STICKY;
    }

    private void setState(String state) {
        if (luna == null) return;
        if (idleAnimator != null) idleAnimator.cancel();
        luna.animate().cancel();
        luna.setAlpha(1f); luna.setScaleX(1f); luna.setScaleY(1f); luna.setRotation(0f);
        String s = state == null ? "idle" : state;
        Luna3DView model = (Luna3DView) luna;
        model.setExpression(s);
        if ("listening".equals(s)) {
            luna.animate().scaleX(1.07f).scaleY(1.07f).setDuration(220).withEndAction(() ->
                    luna.animate().scaleX(1f).scaleY(1f).setDuration(220));
        } else if ("talking".equals(s)) {
            idleAnimator = ObjectAnimator.ofFloat(luna, "scaleX", 1f, 1.035f, 1f);
            idleAnimator.setDuration(520); idleAnimator.setRepeatCount(5); idleAnimator.start();
        } else if ("thinking".equals(s)) {
            idleAnimator = ObjectAnimator.ofFloat(luna, "rotation", -2f, 2f, -2f);
            idleAnimator.setDuration(850); idleAnimator.setRepeatCount(ValueAnimator.INFINITE); idleAnimator.start();
        } else if ("sleeping".equals(s)) {
            luna.animate().alpha(.68f).scaleX(.94f).scaleY(.94f).setDuration(600);
        } else if ("wave".equals(s)) {
            luna.animate().rotation(-2f).setDuration(180).withEndAction(() ->
                    luna.animate().rotation(2f).setDuration(180));
        } else if ("sitting".equals(s)) {
        } else if ("bowing".equals(s)) {
        } else {
            idleAnimator = ObjectAnimator.ofFloat(luna, "translationY", 0f, -10f, 0f);
            idleAnimator.setDuration(2400); idleAnimator.setRepeatCount(ValueAnimator.INFINITE); idleAnimator.start();
        }
    }

    private void resetSleepTimer() {
        inactivity.removeCallbacks(sleepTask);
        inactivity.postDelayed(sleepTask, 120_000L);
    }

    private class DragListener implements View.OnTouchListener {
        private final WindowManager.LayoutParams p;
        private float downX, downY; private int startX, startY; private boolean moved;
        DragListener(WindowManager.LayoutParams p) { this.p = p; }
        public boolean onTouch(View v, android.view.MotionEvent e) {
            if (e.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                setState("idle"); resetSleepTimer();
                downX=e.getRawX(); downY=e.getRawY(); startX=p.x; startY=p.y; moved=false; return true;
            }
            if (e.getAction() == android.view.MotionEvent.ACTION_MOVE) {
                float dx=e.getRawX()-downX, dy=e.getRawY()-downY;
                moved = moved || Math.abs(dx)>8 || Math.abs(dy)>8;
                android.util.DisplayMetrics dm=getResources().getDisplayMetrics();
                p.x=Math.max(0,Math.min(startX+(int)dx,dm.widthPixels-luna.getWidth()));
                p.y=Math.max(0,Math.min(startY+(int)dy,dm.heightPixels-luna.getHeight()));
                windowManager.updateViewLayout(luna,p); return true;
            }
            if (e.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (!moved) v.performClick();
                else getSharedPreferences("overlay_position",MODE_PRIVATE).edit().putInt("x",p.x).putInt("y",p.y).apply();
                return true;
            }
            return false;
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CHANNEL, "Luna Bildschirmfigur", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }
    private int dp(int x) { return (int)(x * getResources().getDisplayMetrics().density); }
    @Override public void onDestroy() { inactivity.removeCallbacksAndMessages(null); if (idleAnimator != null) idleAnimator.cancel(); if (luna != null) windowManager.removeView(luna); super.onDestroy(); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
