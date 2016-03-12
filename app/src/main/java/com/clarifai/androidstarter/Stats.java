package com.clarifai.androidstarter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

public class Stats extends Activity {

    private ProgressBar firstBar;
    private ProgressBar secondBar;
    private ProgressBar thirdBar;
    private ProgressBar forthBar;
    private TextView aPVal;
    private TextView bPVal;
    private TextView cPVal;
    private TextView dPVal;
    private int mProgressStatus = 0;
    private String hello;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        firstBar = (ProgressBar) findViewById(R.id.progressBar);
        secondBar = (ProgressBar) findViewById(R.id.progressBar2);
        thirdBar = (ProgressBar) findViewById(R.id.progressBar3);
        forthBar = (ProgressBar) findViewById(R.id.progressBar4);
        firstBar.setMax(100);
        secondBar.setMax(100);
        thirdBar.setMax(100);
        forthBar.setMax(100);

        aPVal = (TextView)findViewById(R.id.aProgVal);
        bPVal = (TextView)findViewById(R.id.bProgVal);
        cPVal = (TextView)findViewById(R.id.cProgVal);
        dPVal = (TextView)findViewById(R.id.dProgVal);
        aPVal.setText((getString(R.string.aVal) + "/100"));
        bPVal.setText((getString(R.string.bVal) + "/100"));
        cPVal.setText((getString(R.string.cVal) + "/100"));
        dPVal.setText((getString(R.string.dVal) + "/100"));
        Log.e("Tag",getString(R.string.dVal));
        firstBar.setProgress(Integer.parseInt(getString(R.string.aVal)));
        secondBar.setProgress(Integer.parseInt(getString(R.string.bVal)));
        thirdBar.setProgress(Integer.parseInt(getString(R.string.cVal)));
        forthBar.setProgress(Integer.parseInt(getString(R.string.dVal)));


        //firstBar.setProgress(20);
        //updateViews();
    }
    protected void updateViews() {

        firstBar.setMax(100);
        firstBar.setProgress(20);

    }

}
