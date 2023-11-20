package com.example.firsttry_btterminal;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    BluetoothSocket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    // here we can create a thread to send and receive data
    Thread thread = new Thread(new Runnable() {
        public void run() {
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                // Send data
                byte[] data = createDataPacket();
                try {
                    outputStream.write(data);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Receive data
                try {
                    bytes = inputStream.read(buffer);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                byte[] receivedData = new byte[bytes];
                System.arraycopy(buffer, 0, receivedData, 0, bytes);

                // Process received data
                processData(receivedData);
            }
        }
    });
thread.start();

    //Here we create our dataPacket
    private byte[] createDataPacket() {
        byte start = (byte) 0xAA;
        byte length = 0x05;
        byte opCode = (byte) 0xA0;
        byte parameter = 0x00;
        byte chksum = calculateChecksum(length, opCode, parameter);

        return new byte[]{start, length, opCode, parameter, chksum};
    }

    //calculate the checksum according to our specifications:
    private byte calculateChecksum(byte length, byte opCode, byte parameter) {
        // Calculate checksum
        byte chksum = (byte) (length + opCode + parameter);

        return chksum;
    }

    //In the processData method, we can process the received data:
    private void processData(byte[] data) {
        byte start = data[0];
        byte length = data[1];
        byte opCode = data[2];
        byte parameter = data[3];
        byte chksum = data[4];

        // Check if the received data is a response
        if (opCode == 0) {
            // Display message
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Ready to start measurement.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    //method of my "Ready" button, you can start the cyclic sending of the command:
    public void onClick(View v) {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    byte[] data = createDataPacket();
                    outputStream.write(data);

                    // Sleep for 1 second
                    Thread.sleep(1000);
                }
            }
        }).start();
    }


}