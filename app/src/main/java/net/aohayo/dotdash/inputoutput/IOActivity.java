package net.aohayo.dotdash.inputoutput;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import net.aohayo.dotdash.R;

import java.util.ArrayList;
import java.util.List;

public class IOActivity extends AppCompatActivity implements OutputSelectionFragment.DialogListener, InputSelectionFragment.DialogListener, TextInput.InputListener {
    private List<MorseOutput> outputs;
    private TextInput textInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        InputSelectionFragment inputSelection = new InputSelectionFragment();
        inputSelection.show(getFragmentManager(), "inputSelection");

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

        CardView card = (CardView) findViewById(R.id.morse_input_large_button);
        card.setMaxCardElevation(getResources().getDimensionPixelSize(R.dimen.button_elevation_pressed));
        card.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                CardView card = (CardView) view;
                float elevation;
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        elevation = getResources().getDimensionPixelSize(R.dimen.button_elevation_pressed);
                        card.setCardElevation(elevation);
                        for (int i = 0; i < outputs.size(); i++) {
                            outputs.get(i).start();
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        elevation = getResources().getDimensionPixelSize(R.dimen.button_elevation_resting);
                        card.setCardElevation(elevation);
                        for (int i = 0; i < outputs.size(); i++) {
                            outputs.get(i).stop();
                        }
                        return true;
                }
                return false;
            }
        });

        EditText editText = (EditText) findViewById(R.id.morse_input_text);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    String text = v.getText().toString();
                    if (text.length() > 0) {
                        textInput.sendText(v.getText().toString());
                        v.setText("");
                    }
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    public void onInputDialogPositiveClick(DialogFragment dialog) {
        InputSelectionFragment.Input selectedInput;
        selectedInput = ((InputSelectionFragment) dialog).getSelectedInput();
        switch (selectedInput) {
            case FAB_BUTTON:
                findViewById(R.id.morse_input_fab).setVisibility(View.VISIBLE);
                findViewById(R.id.morse_input_large_button).setVisibility(View.GONE);
                findViewById(R.id.morse_input_text).setVisibility(View.GONE);
                break;
            case LARGE_BUTTON:
                findViewById(R.id.morse_input_large_button).setVisibility(View.VISIBLE);
                findViewById(R.id.morse_input_fab).setVisibility(View.GONE);
                findViewById(R.id.morse_input_text).setVisibility(View.GONE);
                break;
            case TEXT:
                findViewById(R.id.morse_input_text).setVisibility(View.VISIBLE);
                findViewById(R.id.morse_input_fab).setVisibility(View.GONE);
                findViewById(R.id.morse_input_large_button).setVisibility(View.GONE);
                textInput = new TextInput(this, this);
                break;
            default:
                break;
        }
        OutputSelectionFragment outputSelection = new OutputSelectionFragment();
        outputSelection.show(getFragmentManager(), "outputSelection");
    }

    @Override
    public void onOutputDialogPositiveClick(DialogFragment dialog) {
        boolean[] selectedOutputs = ((OutputSelectionFragment) dialog).getSelectedOutputs();
        outputs = new ArrayList<>();
        if (selectedOutputs[0]) {
            outputs.add(new AudioOutput());
        }
        if (selectedOutputs[1]) {
            outputs.add(new ScreenOutput(this, findViewById(R.id.content_layout)));
        }
        if (selectedOutputs[2]) {
            outputs.add(new VibratorOutput(this));
        }

        for (int i = 0; i < outputs.size(); i++) {
            outputs.get(i).init();
        }
    }

    @Override
    public void onOutputDialogPreviousClick(DialogFragment dialog) {
        findViewById(R.id.morse_input_fab).setVisibility(View.GONE);
        findViewById(R.id.morse_input_large_button).setVisibility(View.GONE);
        InputSelectionFragment inputSelection = new InputSelectionFragment();
        inputSelection.show(getFragmentManager(), "inputSelection");
    }

    @Override
    public void onOutputStart() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < outputs.size(); i++) {
                    outputs.get(i).start();
                }
            }
        });
    }

    @Override
    public void onOutputStop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < outputs.size(); i++) {
                    outputs.get(i).stop();
                }
            }
        });
    }
}
