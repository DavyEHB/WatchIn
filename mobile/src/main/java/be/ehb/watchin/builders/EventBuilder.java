package be.ehb.watchin.builders;

import java.sql.Time;
import java.util.Date;
import java.util.UUID;

import be.ehb.watchin.model.Event;

/**
 * Created by davy.van.belle on 16/06/2016.
 */
public class EventBuilder {

    /*
        private int ID;
    private String name;
    private Date date;
    private Time startTime;
    private Time endTime;
    private String location;
    private UUID uuid;
    private List<Person> attendees = new ArrayList<>();
     */

    Event mEvent = new Event();

    public EventBuilder() {

    }

    public EventBuilder ID(int id){
        mEvent.setID(id);
        return this;
    }

    public EventBuilder Name(String name){
        mEvent.setName(name);
        return this;
    }

    public EventBuilder Date(Date date){
        mEvent.setDate(date);
        return this;
    }

    public EventBuilder StartTime(Time time){
        mEvent.setStartTime(time);
        return this;
    }

    public EventBuilder EndTime(Time time){
        mEvent.setEndTime(time);
        return this;
    }

    public EventBuilder Location(String location){
        mEvent.setLocation(location);
        return this;
    }

    public EventBuilder UUID(UUID uuid){
        mEvent.setUuid(uuid);
        return this;
    }

    public Event create(){
        return mEvent;
    }


}
