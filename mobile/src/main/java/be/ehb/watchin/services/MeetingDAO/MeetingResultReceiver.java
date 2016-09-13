package be.ehb.watchin.services.MeetingDAO;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;

/**
 * Created by davy.van.belle on 7/09/2016.
 */
public class MeetingResultReceiver  extends ResultReceiver {

    public static final int ERROR_RECEIVING = 0;
    public static int RESULT_ALL = 2;
    public static int RESULT_ONE = 1;

    private static Parcelable.Creator CREATOR;

    private ReceiveMeeting mReceiver;


    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     */
    private MeetingResultReceiver() {
        super(new Handler());
    }

    public MeetingResultReceiver(ReceiveMeeting receiver) {
        super(new Handler());
        mReceiver = receiver;
    }

    public interface ReceiveMeeting {

        void onReceiveMeeting(Bundle contact);
        void onError();

    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver!=null) {
            if (resultCode == RESULT_ONE) {
                mReceiver.onReceiveMeeting(resultData);
            } else if (resultCode == ERROR_RECEIVING) {
                mReceiver.onError();
            }
        }

    }

}
