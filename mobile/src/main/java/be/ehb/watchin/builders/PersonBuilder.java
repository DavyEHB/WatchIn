package be.ehb.watchin.builders;

import android.graphics.Bitmap;

import be.ehb.watchin.model.Person;

/**
 * Created by davy.van.belle on 3/06/2016.
 */
public class PersonBuilder {
    Person mPerson = new Person();

    public PersonBuilder(){
    }

    public PersonBuilder ID(int ID){
        mPerson.setID(ID);
        return this;
    }

    public PersonBuilder firstName(String name)
    {
        mPerson.setFirstName(name);
        return this;
    }

    public PersonBuilder lastName(String name)
    {
        mPerson.setLastName(name);
        return this;
    }

    public PersonBuilder beaconID(String bid)
    {
        mPerson.setBeaconID(bid);
        return this;
    }

    public PersonBuilder age(int a)
    {
        mPerson.setAge(a);
        return this;
    }

    public PersonBuilder email(String mail)
    {
        mPerson.setEmail(mail);
        return this;
    }

    public PersonBuilder company(String c)
    {
        mPerson.setCompany(c);
        return this;
    }

    public PersonBuilder photo(Bitmap p)
    {
        mPerson.setPhoto(p);
        return this;
    }

    public PersonBuilder skill(String s)
    {
        if (!mPerson.Skills().contains(s))
        {
            mPerson.Skills().add(s);
        }
        return this;
    }

    public Person create()
    {
        return mPerson;
    }
}
