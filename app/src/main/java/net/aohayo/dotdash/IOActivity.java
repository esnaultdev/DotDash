package net.aohayo.dotdash;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class IOActivity extends AppCompatActivity {
    private List<MorseOutput> outputs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        OutputSelectionFragment outputSelection = new OutputSelectionFragment();
        outputSelection.show(getFragmentManager(), "outputSelection");

        outputs = new ArrayList<>();
        outputs.add(new AudioOutput());
        outputs.add(new ScreenOutput(this, findViewById(R.id.content_layout)));

        for (int i = 0; i < outputs.size(); i++) {
            outputs.get(i).init();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.morse_input_fab);
        fab.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        for (int i = 0; i < outputs.size(); i++) {
                            outputs.get(i).start();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        for (int i = 0; i < outputs.size(); i++) {
                            outputs.get(i).stop();
                        }
                        return true;
                }
                return false;
            }
        });
    }
}
