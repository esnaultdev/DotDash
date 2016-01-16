package net.aohayo.dotdash.inputoutput;

public class AudioOutput extends MorseOutput {
    private ToneGenerator generator;

    public AudioOutput() {
        generator = new ToneGenerator(400);
    }

    @Override
    public void start() {
        generator.startTone();
    }

    @Override
    public void stop() {
        generator.stopTone();
    }
}
