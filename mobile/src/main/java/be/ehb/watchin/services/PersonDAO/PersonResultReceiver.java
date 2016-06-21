package be.ehb.watchin.services.PersonDAO;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import be.ehb.watchin.model.Person;


/**
 * Created by davy.van.belle on 2/05/2016.
 */
public class PersonResultReceiver extends ResultReceiver {

    public static final int ERROR_RECEIVING = 0;
    public static int RESULT_ALL = 2;
    public static int RESULT_ONE = 1;

    private static Creator CREATOR;

    private ReceivePerson mPersonReceiver;

    public PersonResultReceiver() {
        super(new Handler());
    }

    public interface ReceivePerson {

        void onReceive(Person person);
        void onReceiveAll(Map<Integer,Person> persons);
        void onError();

    }

    public void setPersonReceiver(ReceivePerson receiver) {
        mPersonReceiver = receiver;
    }



    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mPersonReceiver!=null) {
            if (resultCode == RESULT_ONE) {
                mPersonReceiver.onReceive((Person) resultData.getSerializable("person"));
            } else if (resultCode == RESULT_ALL) {
                List<Person> result = (List<Person>) resultData.getSerializable("persons");
                Map<Integer,Person> personMap = new LinkedHashMap<>();
                for (Person p : result)
                {
                    personMap.put(p.getID(),p);
                }
                mPersonReceiver.onReceiveAll(personMap);
            } else if (resultCode == ERROR_RECEIVING) {
                mPersonReceiver.onError();
            }
        }

    }
}
