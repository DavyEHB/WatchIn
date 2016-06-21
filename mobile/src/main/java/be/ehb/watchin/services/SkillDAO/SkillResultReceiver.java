package be.ehb.watchin.services.SkillDAO;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;


/**
 * Created by davy.van.belle on 2/05/2016.
 */
public class SkillResultReceiver extends ResultReceiver {

    public static final int ERROR_RECEIVING = 0;
    public static int RESULT_ALL = 2;
    public static int RESULT_ONE = 1;

    private static Creator CREATOR;

    private ReceiveSkill mReceiver;


    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     */
    public SkillResultReceiver() {
        super(new Handler());
    }

    public interface ReceiveSkill {

        void onReceive(int PID,String skill);
          void onError();

    }

    public void setReceiver(ReceiveSkill receiver) {
        mReceiver = receiver;
    }



    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver!=null) {
            if (resultCode == RESULT_ONE) {
                String skill = resultData.getString(SkillRestService.BUN_SKILL);
                int PID = resultData.getInt(SkillRestService.BUN_PID);

                mReceiver.onReceive(PID,skill);
            } else if (resultCode == ERROR_RECEIVING) {
                mReceiver.onError();
            }
        }

    }
}
