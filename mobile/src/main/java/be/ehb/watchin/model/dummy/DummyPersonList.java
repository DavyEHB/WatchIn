package be.ehb.watchin.model.dummy;

import java.util.ArrayList;
import java.util.List;

import be.ehb.watchin.model.Person;

/**
 * Created by davy.van.belle on 3/06/2016.
 */
public class DummyPersonList {

    public static final List<Person> ITEMS = new ArrayList<>();


    private static final String TAG = "DummyPersonList";

    static {

        // Add some sample items.
        /*for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
        */

        Person p1 = new Person();
        p1.setID(1);
        p1.setAge(30);
        p1.setBeaconID("666");
        p1.setCompany("EhB");
        p1.setEmail("davy@ehb.be");
        p1.setFirstName("Davy");
        p1.setLastName("Van Belle");

        Person p2 = Person.makePerson()
                .age(14)
                .beaconID("36")
                .company("Thuis")
                .email("ding@ske.be")
                .ID(15)
                .firstName("Piet")
                .lastName("Uytebroeck")
                .skill("Electronics development")
                .skill("Networking")
                .skill("Doinig everything")
                .create();

        Person p3 = Person.makePerson()
                .age(19)
                .beaconID("20")
                .company("VUB")
                .email("Piet@ske.be")
                .ID(15)
                .firstName("Steven")
                .lastName("Typels")
                .create();

        addItem(p1);
        addItem(p2);
        addItem(p3);

    }

    public static void addItem(Person item) {
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
