package com.example.julian.bluetoothandroid;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    //private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    public TextView statusUpdate;
    public Button connect;
    public Button disconnect;
    public ImageView logo;
    public ListView list;
    public List<String> myList =  new ArrayList<>();
    static BluetoothAdapter btAdapter;
    public static BluetoothDevice mDevice = null;
    public static List<BluetoothDevice> deviceList = new ArrayList<>();
    Set<BluetoothDevice> pairedDevices = null;

    //ConnectThread mConnectThread = null;
    //ConnectedThread mConnectedThread = null;

//    private StringBuilder recDataString = new StringBuilder();
//    final int handlerStateAir = 0;
//    final int handlerStateWater = 1;
//    final int handlerStateSpeed = 2;
//    final int handlerStateSalinity = 3;
//    final int handlerStateWave = 4;

    //Handler bluetoothIn;

    Gson numberCase = new Gson();


    BroadcastReceiver bluetoothState = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String prevStateExtra = BluetoothAdapter.EXTRA_PREVIOUS_CONNECTION_STATE;
            String stateExtra = BluetoothAdapter.EXTRA_STATE;
            int state = intent.getIntExtra(stateExtra, -1);
            String toastText = "";
            switch (state)
            {
                case(BluetoothAdapter.STATE_TURNING_ON) :
                {
                    toastText = "Bluetooth turning On!";
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                }

                case(BluetoothAdapter.STATE_ON) :
                {
                    toastText = "Bluetooth On!";
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    setupUI();
                    break;
                }

                case(BluetoothAdapter.STATE_TURNING_OFF) :
                {
                    toastText = "Bluetooth turning Off!";
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    break;
                }

                case(BluetoothAdapter.STATE_OFF) :
                {
                    toastText = "Bluetooth Off!";
                    Toast.makeText(MainActivity.this, toastText, Toast.LENGTH_SHORT).show();
                    setupUI();
                    break;
                }
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();

    }

    private void setupUI() {

        final TextView statusUpdate = (TextView) findViewById(R.id.statusUpdate);
        final Button connect = (Button) findViewById(R.id.connectButton);
        final Button disconnect = (Button) findViewById(R.id.disconnectButton);
        //final ImageView logo = (ImageView) findViewById(R.id.bluetoothLogo);
        //final ListView listDevices = (ListView) findViewById(R.id.listDevices);

        final ListView list = (ListView) findViewById(R.id.devicesList);


        //              new approach
        ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, myList);
        list.setAdapter(mArrayAdapter);
        list.setOnItemClickListener(new ItemList());

        disconnect.setVisibility(View.GONE);

        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (btAdapter.isEnabled())
        {
            String address = btAdapter.getAddress();
            String name = btAdapter.getName();
            String statusText = name + " : " + address;
            statusUpdate.setText(statusText);
            disconnect.setVisibility(View.VISIBLE);
            list.setVisibility(View.VISIBLE);
            connect.setVisibility(View.GONE);
            createList();
        }
        else
        {
            connect.setVisibility(View.VISIBLE);
            statusUpdate.setText("Bluetooth Is Not On");
        }

        connect.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String actionStateChanged = BluetoothAdapter.ACTION_STATE_CHANGED;
                String actionRequestEnable = BluetoothAdapter.ACTION_REQUEST_ENABLE;
                IntentFilter filter = new IntentFilter(actionStateChanged);
                registerReceiver(bluetoothState, filter);
                startActivityForResult(new Intent(actionRequestEnable), 0);


            }


        });

        disconnect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                btAdapter.disable();
                disconnect.setVisibility(View.GONE);
                list.setVisibility(View.GONE);
                connect.setVisibility(View.VISIBLE);
                statusUpdate.setText("Bluetooth Off");
            }
        });

    }

    private void createList()
    {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();


        if ( pairedDevices.size() >  0 )
        {
            for (BluetoothDevice device: pairedDevices)
            {
                String deviceName = device.getName()+ "\n" + device.getAddress();

                if(!deviceList.contains(device))
                {
                    myList.add(deviceName);
                    deviceList.add(device);
                }
            }
        }



    }

    String myJson;

    class ItemList implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id)
        {
            mDevice = deviceList.get(position);

            int passingPos = position;
            //Toast.makeText(MainActivity.this, "You have Selected" + myList.get(position), Toast.LENGTH_LONG).show();
            Toast.makeText(MainActivity.this, " You have Selected" + mDevice.getName(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, DisplaySensors.class);
            intent.putExtra("my Position", passingPos);
            startActivity(intent);

        }
    }





//    public void sendData(String caseNumber)
//    {
//        mConnectedThread.write(caseNumber.getBytes());
//    }











}

















