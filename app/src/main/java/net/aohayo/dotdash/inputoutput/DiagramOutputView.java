package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.aohayo.dotdash.R;
import net.aohayo.dotdash.main.SettingsActivity;

public class DiagramOutputView extends SurfaceView implements SurfaceHolder.Callback {

    private final SurfaceHolder holder;
    private TimingDiagramThread thread;
    private Context context;

    public DiagramOutputView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        holder = getHolder();
        holder.addCallback(this);
        if (isInEditMode()) return;
        thread = new TimingDiagramThread();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (isInEditMode()) return;
        if (thread == null) {
            thread = new TimingDiagramThread();
        }
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setDimensions(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // try again shutting down the thread
            }
        }
        thread = null;
    }

    public void start() {
        if (thread != null) {
            thread.startOutput();
        }
    }

    public void stop() {
        if (thread != null) {
            thread.stopOutput();
        }
    }

    private class TimingDiagramThread extends Thread {
        private boolean running;

        private int width = 1;
        private int height = 1;
        private Paint paint;
        private Path path;

        private float speed;
        private long time;
        private boolean activated = false;

        private final Object runLock = new Object();

        public TimingDiagramThread() {
            super();

            float density = getResources().getDisplayMetrics().density;

            paint = new Paint();
            paint.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
            paint.setStrokeWidth(4.0f * density);
            paint.setStrokeCap(Paint.Cap.BUTT);
            paint.setStyle(Paint.Style.STROKE);

            path = new Path();
            path.moveTo(width, height / 2.0f);
            time = SystemClock.elapsedRealtime();

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String prefSpeed = sharedPref.getString(
                    SettingsActivity.KEY_PREF_DIAGRAM_SPEED,
                    context.getResources().getString(R.string.pref_diagram_speed_default));
            speed = Integer.parseInt(prefSpeed) * density;
        }

        public void setRunning(boolean running) {
            synchronized (runLock) {
                this.running = running;
            }
        }

        @Override
        public void run() {
            int priority = android.os.Process.THREAD_PRIORITY_BACKGROUND;
            android.os.Process.setThreadPriority(priority);
            while (running) {
                Canvas c = null;
                try {
                    c = holder.lockCanvas(null);
                    synchronized (holder) {
                        if (running) {
                            update();
                            synchronized (runLock) {
                                if (running) doDraw(c);
                            }
                        }
                    }
                } finally {
                    if (c != null) {
                        holder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        public void setDimensions(int width, int height) {
            int widthDiff = width - this.width;
            int heightDiff = height - this.height;
            path.offset(widthDiff, heightDiff / 2.0f);

            this.width = width;
            this.height = height;
        }

        public void startOutput() {
            synchronized (holder) {
                update();
                activated = true;
            }
        }

        public void stopOutput() {
            synchronized (holder) {
                update();
                activated = false;
            }
        }

        private void doDraw(Canvas canvas) {
            canvas.drawColor(Color.WHITE); // Titanium HWITE
            canvas.drawPath(path, paint);
        }

        private void update() {
            long currentTime = SystemClock.elapsedRealtime();
            float timeDiff = (currentTime - time) / 1000.0f;
            path.offset(-timeDiff*speed, 0);
            if (activated) {
                path.rLineTo(timeDiff * speed, 0);
            } else {
                path.rMoveTo(timeDiff * speed, 0);
            }
            time = currentTime;
        }
    }
}
