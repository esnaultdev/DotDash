package net.aohayo.dotdash.inputoutput;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;

import net.aohayo.dotdash.R;

import java.util.ArrayList;
import java.util.List;

public class OutputSelectionFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String HAS_PREVIOUS_DIALOG = "previousDialog";
    private static final String SELECTED_OUTPUTS = "selectedOutputs";

    public interface DialogListener {
        void onOutputDialogPositiveClick(DialogFragment dialog);
        void onOutputDialogPreviousClick(DialogFragment dialog);
        void onOutputDialogCancelClick(DialogFragment dialog);
    }

    private DialogListener dialogListener;
    private boolean[] selectedOutputs; //TODO : better management of outputs
    private boolean hasPrevious;
    private boolean vibratorAvailable;

    static OutputSelectionFragment newInstance(boolean hasPrevious) {
        return newInstance(hasPrevious, null);
    }

    static OutputSelectionFragment newInstance(boolean hasPrevious, List<MorseOutputs> selectedOutputs) {
        OutputSelectionFragment output = new OutputSelectionFragment();

        Bundle args = new Bundle();
        args.putBoolean(HAS_PREVIOUS_DIALOG, hasPrevious);
        args.putSerializable(SELECTED_OUTPUTS, new ArrayList<>(selectedOutputs));
        output.setArguments(args);

        return output;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            dialogListener = (DialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DialogListener");
        }
        vibratorAvailable = VibratorOutput.isAvailable(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<MorseOutputs> selectedOutputsArgs = (List<MorseOutputs>) getArguments().getSerializable(SELECTED_OUTPUTS);
        int nbOutputs = vibratorAvailable ? 3 : 2;
        selectedOutputs = new boolean[nbOutputs];
        if (selectedOutputsArgs != null) {
            selectedOutputs[0] = selectedOutputsArgs.contains(MorseOutputs.AUDIO);
            selectedOutputs[1] = selectedOutputsArgs.contains(MorseOutputs.SCREEN);
            if (vibratorAvailable) {
                selectedOutputs[2] = selectedOutputsArgs.contains(MorseOutputs.VIBRATOR);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.output_selection_dialog, null))
                .setPositiveButton(R.string.menu_select, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialogListener.onOutputDialogPositiveClick(OutputSelectionFragment.this);
                    }
                })
                .setNegativeButton(R.string.menu_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialogListener.onOutputDialogCancelClick(OutputSelectionFragment.this);
                    }
                });

        hasPrevious = getArguments().getBoolean(HAS_PREVIOUS_DIALOG);
        if (hasPrevious) {
            builder.setNeutralButton(R.string.menu_previous, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialogListener.onOutputDialogPreviousClick(OutputSelectionFragment.this);
                }
            });
        }

        Dialog dialog = builder.create();
        if (hasPrevious) {
            dialog.setCanceledOnTouchOutside(false);
        }
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                AlertDialog alertDialog = (AlertDialog) dialog;

                updateSelectButton();

                if (!vibratorAvailable) {
                    alertDialog.findViewById(R.id.vibrator_output_layout).setVisibility(View.GONE);
                    alertDialog.findViewById(R.id.vibrator_output_separator).setVisibility(View.GONE);
                }

                CheckBox soundCB = (CheckBox) alertDialog.findViewById(R.id.sound_output_checkbox);
                CheckBox screenCB = (CheckBox) alertDialog.findViewById(R.id.screen_output_checkbox);
                CheckBox vibratorCB = (CheckBox) alertDialog.findViewById(R.id.vibrator_output_checkbox);
                soundCB.setOnCheckedChangeListener(OutputSelectionFragment.this);
                screenCB.setOnCheckedChangeListener(OutputSelectionFragment.this);
                vibratorCB.setOnCheckedChangeListener(OutputSelectionFragment.this);
                if (selectedOutputs[0]) {
                    soundCB.setChecked(true);
                }
                if (selectedOutputs[1]) {
                    screenCB.setChecked(true);
                }
                if (selectedOutputs[2]) {
                    vibratorCB.setChecked(true);
                }

                RelativeLayout soundOutputLayout = (RelativeLayout) alertDialog.findViewById(R.id.sound_output_layout);
                RelativeLayout screenOutputLayout = (RelativeLayout) alertDialog.findViewById(R.id.screen_output_layout);
                RelativeLayout vibratorOutputLayout = (RelativeLayout) alertDialog.findViewById(R.id.vibrator_output_layout);
                soundOutputLayout.setOnClickListener(OutputSelectionFragment.this);
                screenOutputLayout.setOnClickListener(OutputSelectionFragment.this);
                vibratorOutputLayout.setOnClickListener(OutputSelectionFragment.this);
            }
        });
        return dialog;
    }

    public void onCancel(DialogInterface dialog) {
        if (hasPrevious) {
            dialogListener.onOutputDialogPreviousClick(this);
        } else {
            dialogListener.onOutputDialogCancelClick(this);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.sound_output_checkbox:
                selectedOutputs[0] = isChecked;
                break;
            case R.id.screen_output_checkbox:
                selectedOutputs[1] = isChecked;
                break;
            case R.id.vibrator_output_checkbox:
                selectedOutputs[2] = isChecked;
                break;
            default:
                break;
        }
        updateSelectButton();
    }

    private void updateSelectButton() {
        AlertDialog dialog = (AlertDialog) getDialog();
        Button selectButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        boolean enabled = false;
        for (boolean selectedOutput : selectedOutputs) {
            if (selectedOutput) {
                enabled = true;
                break;
            }
        }
        selectButton.setEnabled(enabled);
    }

    @Override
    public void onClick(View v) {
        CheckBox checkBox;
        switch (v.getId()) {
            case R.id.sound_output_layout:
                checkBox = (CheckBox) v.findViewById(R.id.sound_output_checkbox);
                checkBox.toggle();
                break;
            case R.id.screen_output_layout:
                checkBox = (CheckBox) v.findViewById(R.id.screen_output_checkbox);
                checkBox.toggle();
                break;
            case R.id.vibrator_output_layout:
                checkBox = (CheckBox) v.findViewById(R.id.vibrator_output_checkbox);
                checkBox.toggle();
                break;
            default:
                break;
        }
    }

    public List<MorseOutputs> getSelectedOutputs() {
        ArrayList<MorseOutputs> outputs = new ArrayList<>();
        for (int i = 0; i < selectedOutputs.length; i++) {
            if (selectedOutputs[i]) {
                if (i == 0) {
                    outputs.add(MorseOutputs.AUDIO);
                } else if (i == 1) {
                    outputs.add(MorseOutputs.SCREEN);
                } else if (i == 2) {
                    outputs.add(MorseOutputs.VIBRATOR);
                }
            }
        }
        return outputs;
    }

    public List<MorseOutputs> getNotSelectedOutputs() {
        ArrayList<MorseOutputs> outputs = new ArrayList<>();
        for (int i = 0; i < selectedOutputs.length; i++) {
            if (!selectedOutputs[i]) {
                if (i == 0) {
                    outputs.add(MorseOutputs.AUDIO);
                } else if (i == 1) {
                    outputs.add(MorseOutputs.SCREEN);
                } else if (i == 2) {
                    outputs.add(MorseOutputs.VIBRATOR);
                }
            }
        }
        return outputs;
    }
}
