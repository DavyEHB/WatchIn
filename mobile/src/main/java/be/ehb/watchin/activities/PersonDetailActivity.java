package be.ehb.watchin.activities;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import be.ehb.watchin.R;
import be.ehb.watchin.fragments.PersonalDetail;

public class PersonDetailActivity extends AppCompatActivity {

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
}
