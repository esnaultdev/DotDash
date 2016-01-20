package net.aohayo.dotdash.inputoutput;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import net.aohayo.dotdash.R;
import net.aohayo.dotdash.main.SettingsActivity;
import net.aohayo.dotdash.morse.CodeSheetFragment;

import java.util.List;

public class IOActivity extends AppCompatActivity implements OutputSelectionFragment.DialogListener, InputSelectionFragment.DialogListener {
    private static final String STATE_IO_MANAGER = "ioManager";
    private static final String INPUT_SELECTION_FRAGMENT_ID = "inputSelection";
    private static final String OUTPUT_SELECTION_FRAGMENT_ID = "outputSelection";
    private static final String CODE_SHEET_FRAGMENT_ID = "codeSheet";

    private IOManager ioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_io);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            Bundle managerState = savedInstanceState.getBundle(STATE_IO_MANAGER);
            ioManager = new IOManager(this, managerState);
        } else {
            ioManager = new IOManager(this);
        }

        ioManager.setInputView(MorseInput.FAB_BUTTON, findViewById(R.id.morse_input_fab));
        ioManager.setInputView(MorseInput.LARGE_BUTTON, findViewById(R.id.morse_input_large_button));
        ioManager.setInputView(MorseInput.TEXT, findViewById(R.id.morse_input_text_container));

        ioManager.addOutput(MorseOutputs.AUDIO, new AudioOutput(this));
        ioManager.addOutput(MorseOutputs.SCREEN, new ScreenOutput(this, findViewById(R.id.content_layout)));
        ioManager.addOutput(MorseOutputs.DIAGRAM, new DiagramOutput(
                (DiagramOutputView) findViewById(R.id.morse_output_timing_diagram),
                findViewById(R.id.morse_output_timing_diagram_card)));
        if (VibrationOutput.isAvailable(this)) {
            ioManager.addOutput(MorseOutputs.VIBRATION, new VibrationOutput(this));
        }

        if (!ioManager.hasInput()) {
            Fragment inputSelectionFragment = getFragmentManager().findFragmentByTag(INPUT_SELECTION_FRAGMENT_ID);
            if (inputSelectionFragment == null) {
                showInputSelectionDialog();
            }
        }
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
                showOutputSelectionDialog();
                return true;
            case R.id.code_sheet:
                showCodeSheetDialog();
                return true;
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ioManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ioManager.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        ioManager.finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Bundle managerState = ioManager.getInstanceState();
        savedInstanceState.putBundle(STATE_IO_MANAGER, managerState);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onInputDialogCancelClick(DialogFragment dialog) {
        if (!ioManager.hasInput()) {
            finish();
        }
    }

    @Override
    public void onInputDialogPositiveClick(DialogFragment dialog) {
        MorseInput selectedInput = ((InputSelectionFragment) dialog).getSelectedInput();
        ioManager.setCurrentInput(selectedInput);

        if (!ioManager.hasOutputs()) {
            showOutputSelectionDialog();
        }
    }

    @Override
    public void onOutputDialogPositiveClick(DialogFragment dialog) {
        List<MorseOutputs> selectedOutputs = ((OutputSelectionFragment) dialog).getSelectedOutputs();
        List<MorseOutputs> notSelectedOutputs = ((OutputSelectionFragment) dialog).getNotSelectedOutputs();

        for (MorseOutputs output : selectedOutputs) {
            ioManager.setEnabledOutput(output, true);
        }
        for (MorseOutputs output : notSelectedOutputs) {
            ioManager.setEnabledOutput(output, false);
        }
    }

    @Override
    public void onOutputDialogCancelClick(DialogFragment dialog) {
        if (!ioManager.hasOutputs()) {
            finish();
        }
    }

    private void showInputSelectionDialog() {
        InputSelectionFragment inputSelection = new InputSelectionFragment();
        inputSelection.show(getFragmentManager(), INPUT_SELECTION_FRAGMENT_ID);
    }

    private void showOutputSelectionDialog() {
        OutputSelectionFragment outputSelection;
        outputSelection = OutputSelectionFragment.newInstance(ioManager.getOutputs());
        outputSelection.show(getFragmentManager(), OUTPUT_SELECTION_FRAGMENT_ID);
    }

    private void showCodeSheetDialog() {
        CodeSheetFragment codeSheet = new CodeSheetFragment();
        codeSheet.show(getFragmentManager(), CODE_SHEET_FRAGMENT_ID);
    }

    public void onStopTextInput(View view) {
        ioManager.stopTextInput();
    }

    public void onFocusTextInput(View view) {
        View textInput = findViewById(R.id.morse_input_text);
        if (textInput.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(textInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
