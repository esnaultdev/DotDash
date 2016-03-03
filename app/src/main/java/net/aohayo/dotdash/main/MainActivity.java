package net.aohayo.dotdash.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;

import net.aohayo.dotdash.BuildConfig;
import net.aohayo.dotdash.R;

import net.aohayo.dotdash.inputoutput.IOActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());

        setContentView(R.layout.activity_main);
        TransitionHelper.setExitWindowAnimations(this, R.transition.slide_start);
    }

    public void startIOActivity(View view) {
        Intent intent = new Intent(this, IOActivity.class);
        TransitionHelper.startActivityTransition(this, intent);
    }

    public void startSettingsActivity(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        TransitionHelper.startActivityTransition(this, intent);
    }
}
