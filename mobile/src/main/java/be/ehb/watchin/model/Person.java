package be.ehb.watchin.model;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
    private List<Event> events;
    private List<Person> contacts = new ArrayList<>();
    private List<Person> meetings = new ArrayList<>();
    private int ID;

    public Person() {
        events = new EventList(this);
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

    public List<Person> Contacts() {
        return contacts;
    }

    public List<Event> Events() {
        return events;
    }

    public List<Person> Meetings() {
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

    public static PersonBuilder makePerson()
    {
        return new PersonBuilder();
    }

    public List<Person> findMutualContacts(Person person)
    {
        List<Person> mutual = new ArrayList<>();
        if (person != null){
            for (Person mC : this.Contacts()) {
                for (Person pC : person.Contacts()) {
                    if (mC.getID() == pC.getID()) {
                        if (!mutual.contains(mC)) {
                            mutual.add(mC);
                        }
                    }
                }
            }
        }
        return mutual;
    }

    public List<Event> findMutualEvents(Person person)
    {
        List<Event> mutual = new ArrayList<>();
        if (person != null){
            for (Event mEvent : this.Events()) {
                for (Event pEvent : person.Events()) {
                    if (mEvent.getID() == pEvent.getID()) {
                        if (!mutual.contains(mEvent)) {
                            mutual.add(mEvent);
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
