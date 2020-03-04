package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.arrival.Dialogs.DisplayQRDialog;
import com.example.android.arrival.Dialogs.ScanQRDialog;
import com.example.android.arrival.R;

public class MainActivity extends AppCompatActivity implements ScanQRDialog.OnFragmentInteractionListener {

    private static final String TAG = "main-activity";
    private Button openScannerBTN;
    private Button genQRBTN;
    private EditText inputText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openScannerBTN = findViewById(R.id.scanner);
        genQRBTN = findViewById(R.id.genQR);
        inputText = findViewById(R.id.text_to_convert);


        genQRBTN.setOnClickListener(view -> {
            if (inputText.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Input Text to convert", Toast.LENGTH_SHORT).show();
            }
            else {
                Log.d("generateClicked", "onClick: failed");
                FragmentTransaction fm = getSupportFragmentManager().beginTransaction();
                DisplayQRDialog displayQRDialog = DisplayQRDialog.newInstance(inputText.getText().toString());
                displayQRDialog.show(fm, "generate");
            }
        });

        openScannerBTN.setOnClickListener(view -> openScanner());


    }


    public void openScanner() {
        ScanQRDialog scanQRDialog = new ScanQRDialog();
        scanQRDialog.show(getSupportFragmentManager(), "scan");
    }

    @Override
    public void onDonePressed(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}
