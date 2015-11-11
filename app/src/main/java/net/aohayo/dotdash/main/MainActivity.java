package net.aohayo.dotdash.main;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.aohayo.dotdash.R;

import net.aohayo.dotdash.inputoutput.IOActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startIOActivity(View view) {
        Intent intent = new Intent(this, IOActivity.class);
        startActivity(intent);
    }
}
