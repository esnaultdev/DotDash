package net.aohayo.dotdash;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import inputoutput.IOActivity;

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
