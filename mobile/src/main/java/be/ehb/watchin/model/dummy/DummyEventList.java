package be.ehb.watchin.model.dummy;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import be.ehb.watchin.model.Event;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 */
public class DummyEventList {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Event> ITEMS = new ArrayList<>();


    private static final String TAG = "DummyEventList";

    static {

        // Add some sample items.
        /*for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
        */
        SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy");

        Event e1 = new Event();
        e1.setID(1);
        e1.setName("DVB Meetup");
        e1.setLocation("Ninove");
        e1.setUuid(UUID.fromString("8c546262-20bf-11e6-9e3b-2cd05a8ad3b9"));
        try {
            Date date = ft.parse("24-03-1986");
            e1.setStartTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Event e2 = new Event();
        e2.setID(2);
        e2.setName("Ehb Networkz");
        e2.setLocation("Anderlecht");
        e2.setUuid(UUID.fromString("8c546262-20bf-11e6-9e3b-2cd05a8ad3b9"));
        try {
            Date date = ft.parse("26-02-2016");
            e2.setStartTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Event e3 = new Event();
        e3.setID(3);
        e3.setName("Future event");
        e3.setLocation("Mars");
        e3.setUuid(UUID.fromString("8c546262-20bf-11e6-9e3b-2cd05a8ad3b9"));
        try {
            Date date = ft.parse("01-02-2050");
            e3.setStartTime(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        addItem(e2);
        addItem(e3);
        addItem(e1);

    }

    public static Map<Integer,Event> EVENT_MAP(){
        Map<Integer,Event> map = new HashMap<>();
        for (Event e : ITEMS){
            map.put(e.getID(),e);
        }
        return map;
    }

    public static void addItem(Event item) {
        ITEMS.add(item);
    }


    /*
    private static DummyItem createDummyItem(int position) {
        return new DummyItem(String.valueOf(position+95), "Item " + position, makeDetails(position));
    }
    */

    /*
    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
   */
}
