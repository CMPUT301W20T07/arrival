package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.android.arrival.Dialogs.ScanQRDialog;
import com.example.android.arrival.R;

public class MainActivity extends AppCompatActivity implements ScanQRDialog.OnFragmentInteractionListener{

    private static final String TAG = "main-activity";
    Button openScannerBTN;
    TextView QRText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button map_Button;

        map_Button = findViewById(R.id.map_button);
        openScannerBTN = findViewById(R.id.scanner);
        QRText = findViewById(R.id.barcodeText);

        openScannerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openScanner();
            }
        });

        map_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMaps();
            }
        });
    }

    public void openMaps(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    public void openScanner() {
        ScanQRDialog scanQRDialog = new ScanQRDialog();
        scanQRDialog.show(getSupportFragmentManager(), "scan");
    }

    @Override
    public void onDonePressed(String s) {
        QRText.setText(s);
    }
}
