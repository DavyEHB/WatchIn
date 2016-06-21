package be.ehb.watchin.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by davy.van.belle on 19/05/2016.
 */
public class EventList extends ArrayList<Event>{

    private Person person;

    public EventList(int capacity, Person person) {
        super(capacity);
        this.person = person;
    }

    public EventList(Collection<? extends Event> collection, Person person) {
        super(collection);
        this.person = person;
    }

    public EventList(Person person){
        super();
        this.person = person;
    }

    public EventList(){
        super();
        this.person = null;
    }

    public boolean add(Event event){
        if ((!super.contains(event))&&(event != null)) {
            event.Attendees().add(person);
            return super.add(event);
        }
        return false;
    }

    @Override
    public void add(int index, Event event) {
        if ((!super.contains(event))&&(event != null)) {
            event.Attendees().add(person);
            super.add(index, event);
        }
    }

    @Override
    public Event remove(int index) {
        Event event = super.get(index);
        if(super.contains(event)){
            event.Attendees().remove(person);
            return super.remove(index);
        }
        return event;
    }

    @Override
    public boolean remove(Object event) {
        if((super.contains(event))&&(event != null)){
            ((Event) event).Attendees().remove(person);
            return super.remove(event);
        }
        return false;
    }

}
