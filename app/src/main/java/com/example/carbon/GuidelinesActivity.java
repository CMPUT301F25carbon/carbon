package com.example.carbon;

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
