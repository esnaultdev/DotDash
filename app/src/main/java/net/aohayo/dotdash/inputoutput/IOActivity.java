package net.aohayo.dotdash.inputoutput;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.aohayo.dotdash.R;

import java.util.List;

public class IOActivity extends AppCompatActivity implements OutputSelectionFragment.DialogListener, InputSelectionFragment.DialogListener {
    private static final String STATE_IO_MANAGER = "ioManager";

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
        ioManager.setInputView(MorseInput.TEXT, findViewById(R.id.morse_input_text_card));

        ioManager.addOutput(MorseOutputs.AUDIO, new AudioOutput());
        ioManager.addOutput(MorseOutputs.SCREEN, new ScreenOutput(this, findViewById(R.id.content_layout)));
        ioManager.addOutput(MorseOutputs.DIAGRAM, new DiagramOutput(
                (DiagramOutputView) findViewById(R.id.morse_output_timing_diagram),
                findViewById(R.id.morse_output_timing_diagram_card)));
        if (VibratorOutput.isAvailable(this)) {
            ioManager.addOutput(MorseOutputs.VIBRATOR, new VibratorOutput(this));
        }

        if (!ioManager.hasInput()) {
            showInputSelectionDialog(!ioManager.hasOutputs());
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

        if (((InputSelectionFragment) dialog).hasNextDialog()) {
            showOutputSelectionDialog(true);
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
    public void onOutputDialogPreviousClick(DialogFragment dialog) {
        showInputSelectionDialog(true);
    }

    @Override
    public void onOutputDialogCancelClick(DialogFragment dialog) {
        if (!ioManager.hasOutputs()) {
            finish();
        }
    }

    private void showInputSelectionDialog(boolean hasNextDialog) {
        InputSelectionFragment inputSelection = InputSelectionFragment.newInstance(hasNextDialog);
        inputSelection.show(getFragmentManager(), "inputSelection");
    }

    private void showOutputSelectionDialog(boolean hasPreviousDialog) {
        OutputSelectionFragment outputSelection;
        outputSelection = OutputSelectionFragment.newInstance(hasPreviousDialog, ioManager.getOutputs());
        outputSelection.show(getFragmentManager(), "outputSelection");
    }
}
