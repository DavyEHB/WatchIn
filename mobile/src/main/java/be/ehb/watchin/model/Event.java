package be.ehb.watchin.model;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * Created by davy.van.belle on 19/05/2016.
 */
public class Event implements Serializable {
    private int ID;
    private String name;
    private Date startTime;
    private Date endTime;
    private String location;
    private UUID uuid;
    private Map<Integer,Person> attendees = new HashMap<>();

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Event() {
    }

    public Map<Integer,Person> Attendees() {
        return attendees;
    }

    public Bitmap generateQR(){
        try {
            return encodeAsBitmap(this.uuid.toString());
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap encodeAsBitmap(String str) throws WriterException {


        BitMatrix result;
        try {
            result = new MultiFormatWriter().encode(str,
                    BarcodeFormat.QR_CODE, 1000 , 1000, null);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[w * h];
        for (int y = 0; y < h; y++) {
            int offset = y * w;
            for (int x = 0; x < w; x++) {
                pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, 1000, 0, 0, w, h);
        return bitmap;
    } /// end of this method

    @Override
    public String toString() {
        return "Event: ID: "  + this.ID + "\n"
                + "\tName: " + this.name + "\n"
                + "\tLocation: " + this.location + "\n"
                + "\tStart: " + this.startTime + "\n"
                + "\tEnd: " + this.endTime + "\n"
                + "\tUUID: " + this.uuid;
    }
}
