package net.aohayo.dotdash.inputoutput;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.EnumMap;
import java.util.Map;

public class IOManager implements TextInput.InputListener {
    private static final String STATE_TEXT_INPUT = "textInput";
    private static final String STATE_INPUT = "input";
    private static final String STATE_OUTPUTS = "outputs";

    private MorseInput input = MorseInput.NONE;
    private EnumMap<MorseInput, View> inputViews;
    private EnumMap<MorseOutputs, Boolean> enabledOutputs;
    private EnumMap<MorseOutputs, MorseOutput> outputs;
    private TextInput textInput;

    private Activity activity;

    public IOManager(Activity activity) {
        this(activity, null);
    }

    public IOManager(Activity activity, Bundle savedInstanceState) {
        this.activity = activity;
        inputViews = new EnumMap<>(MorseInput.class);
        enabledOutputs = new EnumMap<>(MorseOutputs.class);
        outputs = new EnumMap<>(MorseOutputs.class);

        if (savedInstanceState != null) {
            MorseInput savedInput = (MorseInput) savedInstanceState.getSerializable(STATE_INPUT);
            if (savedInput != null) {
                setCurrentInput(savedInput);
            }

            EnumMap<MorseOutputs, Boolean> savedEnabledOutputs;
            savedEnabledOutputs = (EnumMap<MorseOutputs, Boolean>) savedInstanceState.getSerializable(STATE_OUTPUTS);
            if (savedEnabledOutputs != null) {
                enabledOutputs = savedEnabledOutputs;
            }

            Bundle textInputState = savedInstanceState.getBundle(STATE_TEXT_INPUT);
            textInput = new TextInput(activity, this, textInputState);
        } else {
            textInput = new TextInput(activity, this);
        }
    }

    public Bundle getInstanceState() {
        Bundle state = new Bundle();
        state.putSerializable(STATE_INPUT, input);
        state.putSerializable(STATE_OUTPUTS, enabledOutputs);

        Bundle textInputState = textInput.getInstanceState();
        state.putBundle(STATE_TEXT_INPUT, textInputState);
        return state;
    }

    public void setInputView(MorseInput input, View view) {
        inputViews.put(input, view);
        if (input != this.input) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
        setUIListener(input, view);
    }

    private void setUIListener(MorseInput input, View view) {
        switch (input) {
            case FAB_BUTTON:
            case LARGE_BUTTON:
                view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                startOutputs();
                                return true;
                            case MotionEvent.ACTION_UP:
                                stopOutputs();
                                return true;
                        }
                        return false;
                    }
                });
                break;
            case TEXT:
                EditText editText = (EditText) ((ViewGroup) view).getChildAt(0);
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
                break;
            default:
                break;
        }
    }

    public void setCurrentInput(MorseInput input) {
        if (input == this.input) {
            return;
        }
        View oldInputView = inputViews.get(this.input);
        View newInputView = inputViews.get(input);
        if (oldInputView != null) {
            oldInputView.setVisibility(View.GONE);
        }
        if (newInputView != null) {
            newInputView.setVisibility(View.VISIBLE);
        }

        this.input = input;
    }

    public boolean hasInput() {
        return input != MorseInput.NONE;
    }

    public void addOutput(MorseOutputs outputName, MorseOutput output) {
        outputs.put(outputName, output);
        Boolean wasEnabled = enabledOutputs.get(outputName);
        if (wasEnabled == null) {
            enabledOutputs.put(outputName, Boolean.FALSE);
        } else if (wasEnabled) {
            output.init();
        }
    }

    public void setEnabledOutput(MorseOutputs outputName, boolean enabled) {
        Boolean wasEnabled = enabledOutputs.get(outputName);
        if (wasEnabled == null || wasEnabled == enabled) {
            return;
        }
        MorseOutput output = outputs.get(outputName);
        if (enabled) {
            output.init();
        } else {
            output.stop();
            output.finish();
        }
        enabledOutputs.put(outputName, enabled);
    }

    public boolean hasOutputs() {
        boolean result = false;
        for (Boolean enabled : enabledOutputs.values()) {
            if (enabled) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public void onOutputStart() {
        startOutputs();
    }

    @Override
    public void onOutputStop() {
        stopOutputs();
    }

    private void startOutputs() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<MorseOutputs, Boolean> entry : enabledOutputs.entrySet()) {
                    MorseOutput output = outputs.get(entry.getKey());
                    if (entry.getValue() && output != null) {
                        output.start();
                    }
                }
            }
        });
    }

    private void stopOutputs() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<MorseOutputs, Boolean> entry : enabledOutputs.entrySet()) {
                    if (entry.getValue()) {
                        MorseOutput output = outputs.get(entry.getKey());
                        if (entry.getValue() && output != null) {
                            output.stop();
                        }
                    }
                }
            }
        });
    }

    public void pause() {
        textInput.pause();
    }

    public void resume() {
        textInput.resume();
    }

    public void finish() {
        textInput.cancel();
    }
}
