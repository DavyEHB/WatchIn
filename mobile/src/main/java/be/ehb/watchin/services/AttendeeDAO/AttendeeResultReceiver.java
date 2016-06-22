package be.ehb.watchin.services.AttendeeDAO;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import be.ehb.watchin.activities.TempLauncher;

/**
 * Created by davy.van.belle on 14/06/2016.
 */
public class AttendeeResultReceiver extends ResultReceiver{

    public static final int ERROR_RECEIVING = 0;
    public static int RESULT_ALL = 2;
    public static int RESULT_ONE = 1;

    private static Creator CREATOR;

    private ReceiveAttendee mReceiver;


    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     */
    private AttendeeResultReceiver() {
        super(new Handler());
    }

    public AttendeeResultReceiver(ReceiveAttendee receiver) {
        super(new Handler());
        mReceiver = receiver;
    }

    public interface ReceiveAttendee {

        void onReceiveAttendee(Bundle contact);
        void onError();

    }





    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver!=null) {
            if (resultCode == RESULT_ONE) {
                mReceiver.onReceiveAttendee(resultData);
            } else if (resultCode == ERROR_RECEIVING) {
                mReceiver.onError();
            }
        }

    }

}
