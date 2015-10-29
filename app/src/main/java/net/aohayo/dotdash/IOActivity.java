package net.aohayo.dotdash;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;

public class IOActivity extends AppCompatActivity {
    private ToneManager toneManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toneManager = new ToneManager();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        toneManager.startTone();
                        return true;
                    case MotionEvent.ACTION_UP:
                        toneManager.stopTone();
                        return true;
                }
                return false;
            }
        });
    }

}
