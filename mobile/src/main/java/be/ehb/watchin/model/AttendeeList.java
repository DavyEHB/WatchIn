package be.ehb.watchin.model;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by davy.van.belle on 19/05/2016.
 */
public class AttendeeList extends ArrayList<Person> {

    private Event event;

    public AttendeeList(int capacity, Event event) {
        super(capacity);
        this.event = event;
    }

    public AttendeeList(Collection<? extends Person> collection, Event event) {
        super(collection);
        this.event = event;
    }

    public AttendeeList(Event event){
        super();
        this.event = event;
    }


    @Override
    public boolean add(Person person) {
        if ((!super.contains(person))&&(person != null)) {
            Boolean ret = super.add(person);
            person.Events().add(event);
            return ret;
        }
        return false;
    }

    @Override
    public void add(int index, Person person) {
        if ((!super.contains(person))&&(person != null)) {
            super.add(index, person);
            person.Events().add(event);
        }
    }

    @Override
    public boolean remove(Object person) {
        if((super.contains(person))&&(person != null)){
            Boolean ret = super.remove(person);
            ((Person) person).Events().remove(event);
            return ret;
        }
        return false;
    }

    @Override
    public Person remove(int index) {
        Person person = super.get(index);
        if(super.contains(person)){
            Person ret = super.remove(index);
            person.Events().remove(event);
            return ret;
        }
        return person;
    }
}
