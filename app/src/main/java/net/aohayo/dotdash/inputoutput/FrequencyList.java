package net.aohayo.dotdash.inputoutput;

import android.content.Context;

import net.aohayo.dotdash.R;

public class FrequencyList {
    private int[] frequencies;
    private int defaultFrequencyIndex;

    public FrequencyList(Context context) {
        int freqMin = context.getResources().getInteger(R.integer.tone_freq_min);
        int freqMax = context.getResources().getInteger(R.integer.tone_freq_max);
        int freqStep = context.getResources().getInteger(R.integer.tone_freq_step);
        int defaultFreq = context.getResources().getInteger(R.integer.default_tone_freq);

        int nbFrequencies = ((freqMax - freqMin) / freqStep) + 1;
        frequencies = new int[nbFrequencies];
        for (int i = 0; i  < nbFrequencies; i++) {
            frequencies[i] = freqMin + i * freqStep;
            if (frequencies[i] == defaultFreq) {
                defaultFrequencyIndex = i;
            }
        }
    }

    public int getSize() {
        return frequencies.length;
    }

    public int getFrequency(int index) {
        return frequencies[index];
    }

    public int getDefaultFrequencyIndex () {
        return defaultFrequencyIndex;
    }

    public int getDefaultFrequency() {
        return frequencies[defaultFrequencyIndex];
    }
}
