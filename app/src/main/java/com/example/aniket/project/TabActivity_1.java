package com.example.aniket.project;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.y.pecservice.platform.PecService;
import com.example.y.pecservice.platform.Descriptor;

import java.util.ArrayList;
import java.util.HashMap;


public class TabActivity_1 extends Activity {

    private static final int APP_ID = 28396;
    private ServiceConnection serviceConnection;
    private Messenger serviceMessenger;
    private Messenger rMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_1);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        final TextView textView = (TextView) findViewById(R.id.text1);
        final EditText editText = (EditText) findViewById(R.id.edit1);
        final Button btn_send = (Button) findViewById(R.id.btn1);
        Button btn_request = (Button) findViewById(R.id.btn3);
        Button button = (Button) findViewById(R.id.btn2);

        //startService(new Intent(this, PecService.class));
        //Intent intent = new Intent(this, PecService.class);
        //startService(intent);


        button.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent  =  new Intent(TabActivity_1.this, PecService.class);
                ComponentName ch = startService(intent);
                if (ch==null){
                    Toast.makeText(TabActivity_1.this, "Failed to start Service", Toast.LENGTH_LONG).show();
                    return;
                }
                boolean ret = bindService(intent, serviceConnection, Context.BIND_IMPORTANT);
                if(ret){

                    Toast.makeText(TabActivity_1.this, "Bound to Service", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(TabActivity_1.this, "Failed to Bind", Toast.LENGTH_SHORT).show();
                }
            }
        });
                // creating a messenger for the pecService to communicate with app.
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Toast.makeText(TabActivity_1.this, "Service Connected", Toast.LENGTH_SHORT).show();

                serviceMessenger = new Messenger(service);

                // Register as soon as bound to PecService. This gives PecService the messenger to
                // talk to the application.
                Message msg = Message.obtain(null, PecService.APP_MSG_REGISTER_APP, APP_ID, 0, rMessenger);
                try {
                    Toast.makeText(TabActivity_1.this, "Register to PecService", Toast.LENGTH_SHORT).show();
                    serviceMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();

                }
            }
                @Override
                public void onServiceDisconnected (ComponentName name){
                    Toast.makeText(TabActivity_1.this,"Service Disconnected", Toast.LENGTH_SHORT).show();
                    serviceMessenger = null;

                }
            };

        rMessenger = new Messenger(new Handler(){

            @Override
            public void handleMessage(Message msg){
                Bundle bundle;
                switch (msg.what){

                    case PecService.SRV_MSG_PROVIDE_DATA:
                        bundle = msg.getData();
                        bundle.setClassLoader(Descriptor.class.getClassLoader());
                        HashMap<Descriptor, byte[]> map = (HashMap<Descriptor, byte[]>) bundle.getSerializable("data");
                        for (Descriptor descriptor : map.keySet()) {
                            byte[] data = map.get(descriptor);
                            textView.append("Receive data from PecService: Descriptor=" + descriptor.toString() + " Data=" + new String(data) + "\n");
                        }
                        break;
                    // PecService requests data from the application.
                    case PecService.SRV_MSG_REQUEST_DATA:
                        // Get the descriptors of data requested by PecService.

                        bundle = msg.getData();

                        bundle.setClassLoader(Descriptor.class.getClassLoader());
                        ArrayList<Descriptor> descriptors = (ArrayList<Descriptor>) bundle.getSerializable("descriptors");
                        for (Descriptor descriptor : descriptors) {
                            textView.append("PecService requesting data: Descriptor=" + descriptor.toString() + "\n");
                        }

                        // Provide data to PecService.

                        if (serviceMessenger == null) {
                            Toast.makeText(TabActivity_1.this, "Service is not connected", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        HashMap<Descriptor, byte[]> data = new HashMap<>();
                        String testdata = "TESTDATA";
                        for (Descriptor descriptor : descriptors) {
                            data.put(descriptor, (testdata + descriptor.getDataType()).getBytes());
                        }

                                Bundle replyBundle = new Bundle();
                                replyBundle.putSerializable("data", editText.getText().toString());

                                Message replyMsg = Message.obtain(null, PecService.APP_MSG_PROVIDE_DATA, APP_ID, 0);
                                replyMsg.setData(replyBundle);

                                try{
                                    serviceMessenger.send(replyMsg);
                                    Toast.makeText(TabActivity_1.this, "Provide requested data to PecService", Toast.LENGTH_SHORT).show();
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                    Toast.makeText(TabActivity_1.this, "Failed to provide data", Toast.LENGTH_SHORT).show();
                                }

                        break;

                        // PecService provide metadata to application.
                    case PecService.SRV_MSG_PROVIDE_METADATA:

                        bundle = msg.getData();
                        bundle.setClassLoader(Descriptor.class.getClassLoader());
                        ArrayList<Descriptor> metadata = (ArrayList<Descriptor>) bundle.getSerializable("metadata");
                        for (Descriptor descriptor : metadata) {
                            textView.append("Receive metadata from PecService: Descriptor=" + descriptor.toString() + "\n");
                        }
                        break;
                }
            }
        });
                // Adding data to the PecService

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceMessenger == null) {
                    Toast.makeText(TabActivity_1.this, "Service is not connected", Toast.LENGTH_SHORT).show();
                    return;
                }
                HashMap<Descriptor, byte[]> map = new HashMap<>();
                String testdata = editText.getText().toString();
                for (int i = 0; i < 5; ++i) {
                    Descriptor descriptor = new Descriptor(i, 1, 1);
                    map.put(descriptor, (testdata + i).getBytes());
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", map);

                Message msg = Message.obtain(null, PecService.APP_MSG_PROVIDE_DATA, APP_ID, 0);
                msg.setData(bundle);
                try {
                    serviceMessenger.send(msg);
                    textView.append(testdata);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    textView.append("Failed to add test data to PecService.\n");
                }
            }
        });
        btn_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serviceMessenger == null) {
                    Toast.makeText(TabActivity_1.this, "Service is not connected", Toast.LENGTH_SHORT).show();
                    return;
                }

                ArrayList<Descriptor> descriptors = new ArrayList<>();
                for (int i = 0; i < 3; ++i) {
                    Descriptor descriptor = new Descriptor(i, 1, 1);
                    descriptors.add(descriptor);
                }
                Bundle bundle = new Bundle();
                bundle.putSerializable("descriptors", descriptors);

                Message msg = Message.obtain(null, PecService.APP_MSG_REQUEST_DATA, APP_ID, 0);
                msg.setData(bundle);

                Message msg_rcv = Message.obtain(null, PecService.APP_MSG_REQUEST_DATA, APP_ID, 0);
                msg.setData(bundle);
                try {
                    serviceMessenger.send(msg_rcv);
                    Toast.makeText(TabActivity_1.this,"Requesting data from PecService",Toast.LENGTH_LONG).show();
                    textView.append(descriptors.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    textView.append("Failed to request data to PecService.\n");
                }
            }
        });


        }


    }

