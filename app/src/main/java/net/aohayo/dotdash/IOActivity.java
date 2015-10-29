package net.aohayo.dotdash;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class IOActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private ToneManager toneManager;
    private FrequencyList frequencyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        frequencyList = new FrequencyList(this);
        String[] frequenciesInteger = new String[frequencyList.getSize()];
        for (int i = 0; i < frequenciesInteger.length; i++) {
            frequenciesInteger[i] = frequencyList.getFrequency(i) + "Hz";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, frequenciesInteger);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(frequencyList.getDefaultFrequencyIndex());
        spinner.setOnItemSelectedListener(this);

        toneManager = new ToneManager(frequencyList.getDefaultFrequency());
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int frequency = frequencyList.getFrequency(position);
        toneManager.setFrequency(frequency);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
