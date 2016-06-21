package be.ehb.watchin.services.EventDAO;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;

import be.ehb.watchin.model.Event;

/**
 * Created by davy.van.belle on 14/06/2016.
 */
public class EventResultReceiver extends ResultReceiver {

    public static final int ERROR_RECEIVING = 0;
    public static int RESULT_ALL = 2;
    public static int RESULT_ONE = 1;

    private static Parcelable.Creator CREATOR;

    private ReceiveEvent mReceiver;


    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     */
    public EventResultReceiver() {
        super(new Handler());
    }

    public interface ReceiveEvent {

        void onReceiveEvent(Event event);
        void onError();

    }

    public void setReceiver(ReceiveEvent receiver) {
        mReceiver = receiver;
    }



    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver!=null) {
            if (resultCode == RESULT_ONE) {
                Event event = (Event) resultData.getSerializable(EventRestService.EVENT);
                mReceiver.onReceiveEvent(event);
            } else if (resultCode == ERROR_RECEIVING) {
                mReceiver.onError();
            }
        }

    }
}
