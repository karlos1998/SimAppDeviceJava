package it.letscode.simappdevice;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SmsSender smsSender = new SmsSender();
        smsSender.sendSms("+48884167733", "Start Sim App Device");
    }
}