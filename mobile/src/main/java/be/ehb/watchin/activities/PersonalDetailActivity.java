package be.ehb.watchin.activities;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.UUID;

import be.ehb.watchin.R;
import be.ehb.watchin.model.Event;

public class PersonalDetailActivity extends AppCompatActivity {

    private static final String TAG = "MainAct";
    ImageView qrCodeImageview;

    private Event myTestEvent = new Event();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_detail);
        getID();

        myTestEvent.setID(4);
        myTestEvent.setName("DVB Meetup");
        myTestEvent.setLocation("Ninove");
        myTestEvent.setUuid(UUID.fromString("8c546262-20bf-11e6-9e3b-2cd05a8ad3b9"));
        Log.d(TAG,myTestEvent.getUuid().toString());
    }

    private void getID() {
        qrCodeImageview=(ImageView) findViewById(R.id.qrCode);
    }




    public void onQRClick(View view) {
        Bitmap bitmap = null;
        bitmap = myTestEvent.generateQR();
        qrCodeImageview.setImageBitmap(bitmap);
        Log.d(TAG,myTestEvent.getUuid().toString());

    }
}

