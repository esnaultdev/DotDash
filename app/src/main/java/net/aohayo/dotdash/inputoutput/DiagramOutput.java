package net.aohayo.dotdash.inputoutput;

import android.view.View;

public class DiagramOutput extends MorseOutput {

    private DiagramOutputView diagramView;
    private View container;

    public DiagramOutput(DiagramOutputView view, View container) {
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
    public void resume() {
        container.setVisibility(View.VISIBLE);
    }

    @Override
    public void finish() {
        container.setVisibility(View.GONE);
    }
}
