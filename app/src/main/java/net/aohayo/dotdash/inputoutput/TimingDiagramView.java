package net.aohayo.dotdash.inputoutput;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import net.aohayo.dotdash.R;

public class TimingDiagramView extends SurfaceView implements SurfaceHolder.Callback {

    private final SurfaceHolder holder;
    private TimingDiagramThread thread;
    private Context context;

    public TimingDiagramView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        thread = new TimingDiagramThread(this);
        holder = getHolder();
        holder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("TDV", "Surface created");
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("TDV", "Surface changed");
        thread.setDimensions(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("TDV", "Surface destroyed");
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
        private static final long UPDATE_PERIOD = 20;
        private boolean running;

        private TimingDiagramView view;
        private int width = 1;
        private int height = 1;
        private Paint paint;
        private Path path;

        private float speed = 50;
        private long time;
        private boolean activated = false;

        private final Object runLock = new Object();

        public TimingDiagramThread(TimingDiagramView view) {
            super();
            this.view = view;

            paint = new Paint();
            paint.setColor(context.getResources().getColor(R.color.colorPrimaryDark));
            paint.setStrokeWidth(4.0f);
            paint.setStyle(Paint.Style.STROKE);

            path = new Path();
            path.moveTo(width, height / 2.0f);
            time = SystemClock.elapsedRealtime();
        }

        public void setRunning(boolean running) {
            synchronized (runLock) {
                this.running = running;
            }
        }

        @Override
        public void run() {
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
                if (running) {
                    try {
                        Thread.sleep(UPDATE_PERIOD);
                    } catch (InterruptedException e) {
                        Log.d("TimingDiagramThread", "interrupted while sleeping");
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
                path.rMoveTo(timeDiff*speed, 0);
            } else {
                path.rLineTo(timeDiff * speed, 0);
            }
            time = currentTime;
        }
    }
}
