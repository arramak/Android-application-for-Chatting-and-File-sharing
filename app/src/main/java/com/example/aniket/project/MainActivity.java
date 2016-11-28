package com.example.aniket.project;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.app.TabActivity;
import android.widget.Toast;

public class MainActivity extends TabActivity {


    //TabHost TabHostWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        //creating tab menu
        TabSpec Tab1 = tabHost.newTabSpec("Messenger");
        TabSpec Tab2 = tabHost.newTabSpec("Files Sharing");

        //setting tab1 name
        Tab1.setIndicator("Messenger");
        //set activity
        Tab1.setContent(new Intent(this, TabActivity_1.class));

        //setting tab2 name
        Tab2.setIndicator("File Sharing");
        //set activity
        Tab2.setContent(new Intent(this, TabActivity_2.class));

        //adding tabs
        tabHost.addTab(Tab1);
        tabHost.addTab(Tab2);


    }
}
