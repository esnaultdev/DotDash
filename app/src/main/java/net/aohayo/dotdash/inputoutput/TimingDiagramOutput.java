package net.aohayo.dotdash.inputoutput;

import android.view.View;

public class TimingDiagramOutput extends MorseOutput {

    private TimingDiagramView diagramView;
    private View container;

    public TimingDiagramOutput(TimingDiagramView view, View container) {
        diagramView = view;
        this.container = container;
    }

    @Override
    public void start() {
        diagramView.start();
    }

    @Override
    public void stop() {
        diagramView.stop();
    }

    @Override
    public void init() {
        container.setVisibility(View.VISIBLE);
    }

    @Override
    public void finish() {
        container.setVisibility(View.GONE);
    }
}
