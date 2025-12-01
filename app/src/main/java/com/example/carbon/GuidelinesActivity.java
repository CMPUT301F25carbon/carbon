package com.example.carbon;

/**
 * Displays static community guidelines content within a simple activity shell.
 * Outstanding issues: content is static text; no localization yet.
 */

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class GuidelinesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guidelines);

        UIHelper.setupHeaderAndMenu(this);
    }
}
