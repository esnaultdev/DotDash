package net.aohayo.dotdash.inputoutput;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import net.aohayo.dotdash.R;

import java.util.ArrayList;
import java.util.List;

public class IOActivity extends AppCompatActivity implements OutputSelectionFragment.DialogListener, InputSelectionFragment.DialogListener, TextInput.InputListener {
    static final String STATE_OUTPUTS = "outputs";
    static final String STATE_INPUT = "input";

    private List<MorseOutput> outputs;
    private MorseInput currentInput = MorseInput.NONE;
    boolean[] selectedOutputs;
    private TextInput textInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        showInputSelectionDialog();

        setUIListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.io_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.input:
                showInputSelectionDialog();
                return true;
            case R.id.outputs:
                showOutputSelectionDialog(false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (textInput != null) {
            textInput.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (textInput != null) {
            textInput.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (textInput != null) {
            textInput.cancel();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(STATE_INPUT, currentInput);
        savedInstanceState.putBooleanArray(STATE_OUTPUTS, selectedOutputs);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onInputDialogPositiveClick(DialogFragment dialog) {
        MorseInput selectedInput;
        selectedInput = ((InputSelectionFragment) dialog).getSelectedInput();
        switch (selectedInput) {
            case FAB_BUTTON:
                findViewById(R.id.morse_input_fab).setVisibility(View.VISIBLE);
                findViewById(R.id.morse_input_large_button).setVisibility(View.GONE);
                findViewById(R.id.morse_input_text_card).setVisibility(View.GONE);
                break;
            case LARGE_BUTTON:
                findViewById(R.id.morse_input_large_button).setVisibility(View.VISIBLE);
                findViewById(R.id.morse_input_fab).setVisibility(View.GONE);
                findViewById(R.id.morse_input_text_card).setVisibility(View.GONE);
                break;
            case TEXT:
                findViewById(R.id.morse_input_text_card).setVisibility(View.VISIBLE);
                findViewById(R.id.morse_input_fab).setVisibility(View.GONE);
                findViewById(R.id.morse_input_large_button).setVisibility(View.GONE);
                textInput = new TextInput(this, this);
                break;
            default:
            case NONE:
                break;
        }
        currentInput = selectedInput;
        showOutputSelectionDialog(true);
    }

    @Override
    public void onOutputDialogPositiveClick(DialogFragment dialog) {
        selectedOutputs = ((OutputSelectionFragment) dialog).getSelectedOutputs();
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
        showInputSelectionDialog();
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

    private void showInputSelectionDialog() {
        InputSelectionFragment inputSelection = new InputSelectionFragment();
        inputSelection.show(getFragmentManager(), "inputSelection");
    }

    private void showOutputSelectionDialog(boolean hasPreviousDialog) {
        OutputSelectionFragment outputSelection = new OutputSelectionFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(OutputSelectionFragment.hasPreviousDialog, hasPreviousDialog);
        outputSelection.setArguments(bundle);
        outputSelection.show(getFragmentManager(), "outputSelection");
    }

    private void setUIListeners() {
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
}
