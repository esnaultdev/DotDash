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
    private static final String STATE_OUTPUTS = "outputs";
    private static final String STATE_INPUT = "input";
    private static final String STATE_TEXT_INPUT = "textInput";

    private List<MorseOutput> outputs;
    private MorseInput selectedInput = MorseInput.NONE;
    boolean[] selectedOutputs;
    private TextInput textInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        outputs = new ArrayList<>();
        selectedOutputs = new boolean[nbAvailableOutputs()];

        if (savedInstanceState != null) {
            MorseInput savedInput = (MorseInput) savedInstanceState.getSerializable(STATE_INPUT);
            if (savedInput != null) {
                selectedInput = savedInput;
            }
            boolean[] savedOutputs = savedInstanceState.getBooleanArray(STATE_OUTPUTS);
            if (savedOutputs != null) {
                selectedOutputs = savedOutputs;
            }
            Bundle textInputState = savedInstanceState.getBundle(STATE_TEXT_INPUT);
            textInput = new TextInput(this, this, textInputState);
        } else {
            textInput = new TextInput(this, this);
        }

        boolean hasOutput = false;
        for (boolean selectedOutput : selectedOutputs) {
            if (selectedOutput) {
                hasOutput = true;
                break;
            }
        }

        if (selectedInput == MorseInput.NONE) {
            showInputSelectionDialog(!hasOutput);
        } else {
            initInput();
        }
        if (hasOutput) {
            initOutputs();
        }

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
                showInputSelectionDialog(false);
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

        textInput.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        textInput.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        textInput.cancel();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(STATE_INPUT, selectedInput);
        savedInstanceState.putBooleanArray(STATE_OUTPUTS, selectedOutputs);
        Bundle textInputState = textInput.getInstanceState();
        savedInstanceState.putBundle(STATE_TEXT_INPUT, textInputState);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onInputDialogCancelClick(DialogFragment dialog) {
        if (selectedInput == MorseInput.NONE) {
            finish();
        }
    }

    @Override
    public void onInputDialogPositiveClick(DialogFragment dialog) {
        selectedInput = ((InputSelectionFragment) dialog).getSelectedInput();

        findViewById(R.id.morse_input_fab).setVisibility(View.GONE);
        findViewById(R.id.morse_input_large_button).setVisibility(View.GONE);
        findViewById(R.id.morse_input_text_card).setVisibility(View.GONE);

        initInput();

        if (((InputSelectionFragment) dialog).hasNextDialog()) {
            showOutputSelectionDialog(true);
        }
    }

    @Override
    public void onOutputDialogPositiveClick(DialogFragment dialog) {
        selectedOutputs = ((OutputSelectionFragment) dialog).getSelectedOutputs();

        for (int i = 0; i < outputs.size(); i++) {
            outputs.get(i).finish();
        }
        outputs.clear();

        initOutputs();
    }

    @Override
    public void onOutputDialogPreviousClick(DialogFragment dialog) {
        showInputSelectionDialog(true);
    }

    @Override
    public void onOuptutDialogCancelClick(DialogFragment dialog) {
        if (outputs.size() == 0) {
            finish();
        }
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

    private void showInputSelectionDialog(boolean hasNextDialog) {
        InputSelectionFragment inputSelection = InputSelectionFragment.newInstance(hasNextDialog);
        inputSelection.show(getFragmentManager(), "inputSelection");
    }

    private void showOutputSelectionDialog(boolean hasPreviousDialog) {
        OutputSelectionFragment outputSelection = OutputSelectionFragment.newInstance(hasPreviousDialog);
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

    private int nbAvailableOutputs() {
        return VibratorOutput.isAvailable(this) ? 3 : 2;
    }

    private void initOutputs() {
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

    private void initInput() {
        switch (selectedInput) {
            case FAB_BUTTON:
                findViewById(R.id.morse_input_fab).setVisibility(View.VISIBLE);
                break;
            case LARGE_BUTTON:
                findViewById(R.id.morse_input_large_button).setVisibility(View.VISIBLE);
                break;
            case TEXT:
                findViewById(R.id.morse_input_text_card).setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }
}
