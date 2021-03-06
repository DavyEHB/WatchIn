package be.ehb.watchin.activities;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import be.ehb.watchin.R;
import be.ehb.watchin.fragments.PersonalDetail;
import be.ehb.watchin.model.Event;

public class PersonDetailActivity extends AppCompatActivity implements PersonalDetail.OnPersonDetailInteractionListener {

    public static final String USER_ID = "be.ehb.watchin.USERID";
    private static final String TAG = "PersonDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_detail);

        int userID = getIntent().getIntExtra(USER_ID,0);
        Log.d(TAG,"User id: " + userID);

        Fragment fragment = PersonalDetail.newInstance(userID,"");
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_placeholder,fragment);
        ft.commit();
    }

    @Override
    public void onEventListClick(Event event) {
        Log.d(TAG, "Event click" + event.toString());
        Intent intent = new Intent(this,EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EVENT_ID,event.getID());
        startActivity(intent);
    }
}
