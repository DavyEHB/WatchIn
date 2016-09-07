package be.ehb.watchin.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.ehb.watchin.builders.PersonBuilder;

/**
 * Created by davy.van.belle on 19/05/2016.
 */
public class Person implements Serializable {

    private static final String TAG = "PersonModel";
    private String firstName = "";
    private String lastName = "";
    private int age;
    private String company = "";
    private String email = "";
    private String beaconID = "";
    private Bitmap photo;
    private List<String> skills = new ArrayList<>();
    private Map<Integer,Event> events = new HashMap<>();
    private Map<Integer,Person> contacts = new HashMap<>();
    private Map<Integer,Person> meetings = new HashMap<>();
    private int ID;
    private Boolean visible;
    private int currentEventID;

    public Person() {
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBeaconID() {
        return beaconID;
    }

    public void setBeaconID(String beaconID) {
        this.beaconID = beaconID;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public void setPhoto(Bitmap photo) {
        this.photo = photo;
    }

    public List<String> Skills() {
        return skills;
    }

    public Map<Integer,Person> Contacts() {
        return contacts;
    }

    public Map<Integer,Event> Events() {
        return events;
    }

    public Map<Integer,Person> Meetings() {
        return meetings;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getID() {
        return ID;
    }

    public String getFullname()
    {
        return firstName + " " + lastName;
    }

    public void setVisible(Boolean visible)
    {
        this.visible = visible;
    }

    public Boolean isVisible()
    {
        return visible;
    }

    public void setCurrentEventID(int currentEventID) {
        this.currentEventID = currentEventID;
    }

    public int getCurrentEventID() {
        return currentEventID;
    }

    public static PersonBuilder makePerson()
    {
        return new PersonBuilder();
    }

    public Map<Integer,Person> findMutualContacts(Person person)
    {
        Map<Integer,Person> mutual = new HashMap<>();
        if (person != null){
            for (Person mC : this.Contacts().values()) {
                for (Person pC : person.Contacts().values()) {
                    if (mC.getID() == pC.getID()) {
                        if (!mutual.containsValue(mC)) {
                            mutual.put(mC.getID(),mC);
                        }
                    }
                }
            }
        }
        return mutual;
    }

    public Map<Integer,Event> findMutualEvents(Person person)
    {
        Map<Integer,Event> mutual = new HashMap<>();
        if (person != null){
            for (Event mEvent : this.Events().values()) {
                for (Event pEvent : person.Events().values()) {
                    if (mEvent.getID() == pEvent.getID()) {
                        if (!mutual.containsValue(mEvent)) {
                            mutual.put(mEvent.getID(),mEvent);
                        }
                    }
                }
            }
        }
        return mutual;
    }

    @Override
    public String toString() {
        return getFullname() + " - ID: " + getID() +
                "\n\t#Contacts: " + Contacts().size() +
                "\n\tSkills: " + Skills().toString();
    }


}
