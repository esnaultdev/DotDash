package net.aohayo.dotdash;

public interface MorseOutput {
    void start();
    void start(int duration);
    void stop();
    boolean isAvailable();
}
