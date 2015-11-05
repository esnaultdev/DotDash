package net.aohayo.dotdash;

public interface MorseOutput {
    boolean isAvailable();
    void init();
    void finish();

    void start();
    void start(int duration);
    void stop();
}
