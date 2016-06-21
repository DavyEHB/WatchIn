package be.ehb.watchin;

import android.app.Application;

import java.util.HashMap;
import java.util.Map;

import be.ehb.watchin.model.Person;

/**
 * Created by davy.van.belle on 2/06/2016.
 */
public class WatchInApp extends Application {
    private static final String TAG = "WatchinApp";
    private int myID;
    private String myEmail = "";
    private boolean loggedIn = false;

    public static String server = "192.168.56.1:8080";


    public static class path{
        private static final String base_path = "/WatchInServer/data/";
        public static final String events = base_path + "/events/";
        public static final String persons = base_path + "/persons/";
        public static final String contacts = base_path + "/contacts/";
    }

    //private final List<Person> myPersons = new ArrayList<>();

    private final Map<Integer,Person> myPersons = new HashMap<>();

    public int MyID() {
        return myID;
    }

    public void MyID(int id) {
        this.myID = id;
    }

    public String MyEmail()
    {
        return this.myEmail;
    }

    public void MyEmail(String email)
    {
        this.myEmail = email;
    }


    public Person Me()
    {
        return Persons().get(myID);
    }

    public void Me(Person me)
    {
        this.MyID(me.getID());
    }

    public boolean isLoggedIn()
    {
        return loggedIn;
    }

    public void Login() {
        loggedIn = true;
    }

    public void Logout() {
        loggedIn = false;
        myID = 0;
    }

    public Map<Integer,Person> Persons()
    {
        return myPersons;
    }
}
