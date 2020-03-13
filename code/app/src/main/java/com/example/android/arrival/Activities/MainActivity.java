package com.example.android.arrival.Activities;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.android.arrival.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void openScanner() {
        ScanQRDialog scanQRDialog = new ScanQRDialog();
        scanQRDialog.show(getSupportFragmentManager(), "scan");
    }

//    @Override
//    public void onDonePressed(String s) {
//        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
//    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkPermissions(Context context){

        if (context.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST);
        }

    }

    public void openRiderMaps(){
        Intent intent = new Intent(this, RiderMapActivity.class);
        startActivity(intent);
    }

    public void openDriverMaps(){
        Intent intent = new Intent(this, DriverMapActivity.class);
        startActivity(intent);
    }


}
