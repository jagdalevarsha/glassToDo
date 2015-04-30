package glassproject.ubicomp.com.todo.activity;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.glass.voice.VoiceCommand;
import com.google.glass.voice.VoiceConfig;
import com.google.glass.voice.VoiceInputHelper;
import com.google.glass.voice.VoiceListener;
import com.google.glass.logging.FormattingLogger;
import com.google.glass.logging.FormattingLoggers;

import glassproject.ubicomp.com.todo.R;
import glassproject.ubicomp.com.todo.db.TaskItemDb;
import glassproject.ubicomp.com.todo.model.TaskItem;

public class ToDoLiveCardService extends Service {
    private static final String LIVE_CARD_TAG = "ToDoLiveCard";

    private LiveCard mLiveCard;
    private RemoteViews mLiveCardView;

    private TaskItem taskItem;
    private TaskItemDb db;

    private final Handler mHandler = new Handler();
    private final UpdateLiveCardRunnable mUpdateLiveCardRunnable =
            new UpdateLiveCardRunnable();
    private static final long DELAY_MILLIS = 30000;
    private static final String TAG = "GlassToDo";
    private static final String KEY_TAG = "Let me wake up glass";

    private VoiceInputHelper mVoiceInputHelper;
    private VoiceConfig mVoiceConfig;
    public volatile boolean trigData = false;

    @Override
    public void onCreate() {
        super.onCreate();
        String[] items = {KEY_TAG};
        mVoiceConfig = new VoiceConfig();
        mVoiceConfig.setShouldAllowScreenOff(false);
        mVoiceConfig.setCustomPhrases(items);
        mVoiceConfig.setShouldAllowScreenOff(false);
        mVoiceInputHelper = new VoiceInputHelper(this,new ToDoVoiceListener(mVoiceConfig));
        mVoiceInputHelper.setWantAudioData(true);
        mVoiceInputHelper.setVoiceConfig(mVoiceConfig);
        com.google.glass.logging.Log.v(TAG, "Started");
    }

    private void populateTaskOnView() {
        mLiveCardView.setTextViewText(R.id.taskDescription,
                taskItem.getTaskDescription());
//        if(taskItem.isRework())
//        {
//            mLiveCardView.setTextColor(R.id.taskDescription,Color.parseColor("#77aa70"));
//        }
        // Always call setViews() to update the live card's RemoteViews.
        mLiveCard.setViews(mLiveCardView);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {

            // Get an instance of a live card
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            db = new TaskItemDb(this);
            taskItem = db.getLatestTaskItem();
            // Inflate a layout into a remote view
            mLiveCardView = new RemoteViews(getPackageName(),
                    R.layout.to_do_live_card_screen);

            // Set up initial RemoteViews values
            if(taskItem != null) populateTaskOnView();

            // Set up the live card's action with a pending intent
            // to show a menu when tapped
            Intent menuIntent = new Intent(this, ToDoLiveCardActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(
                    this, 0, menuIntent, 0));

            // Publish the live card
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);

            // Queue the update text runnable
            mHandler.post(mUpdateLiveCardRunnable);
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            //Stop the handler from queuing more Runnable jobs
            mUpdateLiveCardRunnable.setStop(true);

            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    /**
     * Runnable that updates live card contents
     */
    private class UpdateLiveCardRunnable implements Runnable{

        private boolean mIsStopped = false;

        /*
         * Updates the card with a fake score every 30 seconds as a demonstration.
         * You also probably want to display something useful in your live card.
         *
         * If you are executing a long running task to get data to update a
         * live card(e.g, making a web call), do this in another thread or
         * AsyncTask.
         */
        public void run(){
            if(!isStopped()){
                // Generate fake points.
                taskItem = db.getLatestTaskItem();

                // Update the remote view with the new scores.
                if(taskItem != null && trigData)
//                    populateTaskOnView();
                {
                    TaskItem createdTask = new TaskItem("Success");
                    trigData = false;
                    db.saveTaskItem(createdTask);
                    populateTaskOnView();
                }

                // Queue another score update in 30 seconds.
                mHandler.postDelayed(mUpdateLiveCardRunnable, DELAY_MILLIS);
            }
        }

        public boolean isStopped() {
            return mIsStopped;
        }

        public void setStop(boolean isStopped) {
            this.mIsStopped = isStopped;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class ToDoVoiceListener implements VoiceListener {
        protected final VoiceConfig voiceConfig;

        public ToDoVoiceListener(VoiceConfig voiceConfig) {
            com.google.glass.logging.Log.v(TAG, "Listener hit"); this.voiceConfig = voiceConfig;
        }

        @Override
        public VoiceConfig onVoiceCommand(VoiceCommand vc) {
//            android.os.Debug.waitForDebugger();
            String recognizedStr = vc.getLiteral();
//            if(recognizedStr.contentEquals(KEY_TAG))
                trigData = true;
//            com.google.glass.logging.Log.v(TAG, "Recognized text: " + recognizedStr);
            return null;
        }

        @Override
        public FormattingLogger getLogger() {
            return FormattingLoggers.getContextLogger();
        }

        @Override
        public boolean isRunning() {

            return true;
        }

        @Override
        public boolean onResampledAudioData(byte[] arg0, int arg1, int arg2) {
            return false;
        }


        @Override
        public void onVoiceConfigChanged(VoiceConfig arg0, boolean arg1) {
//            String[] cusKey = {KEY_TAG};
//            voiceConfig.setCustomPhrases(cusKey);
        }
    }
}
