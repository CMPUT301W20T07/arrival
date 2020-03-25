package com.example.android.arrival.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android.arrival.R;

public class ContactDriverActivity extends AppCompatActivity {

    TextView phoneNum;
    //String phoneNum = "7801234567";
    TextView email;
    //String email = "email@gmail.com";
    String TAG = "my_activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //phoneNum = (TextView) findViewById(R.id.phone);
        //email = (TextView)findViewById(R.id.email);

        phoneNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", (String) phoneNum.getText(), null));
                startActivity(intent);

            }
        });

        final String email_text = (String) email.getText();

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String to = "email@ualberta.ca";
                Intent intent2 = new Intent(Intent.ACTION_SEND);

                //intent2.putExtra(Intent.EXTRA_EMAIL, email);
                //intent2.setData(Uri.parse("mailto:"));

                intent2.putExtra(Intent.EXTRA_EMAIL, email_text);
                //intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                //intent.putExtra(Intent.EXTRA_STREAM, attachment);
                if (intent2.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent2);
                }
            }
        });



    }

}
