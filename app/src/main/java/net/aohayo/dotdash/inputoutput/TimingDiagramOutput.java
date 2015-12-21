package net.aohayo.dotdash.inputoutput;

public class TimingDiagramOutput extends MorseOutput {

    private TimingDiagramView view;

    public TimingDiagramOutput(TimingDiagramView view) {
        this.view = view;
    }

    @Override
    public void start() {
        view.start();
    }

    @Override
    public void stop() {
        view.stop();
    }
}
