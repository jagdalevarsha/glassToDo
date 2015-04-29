package glassproject.ubicomp.com.todo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class StartLiveCardServiceAtBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Intent.ACTION_INSTALL_PACKAGE.equals(intent.getAction())) {
            Intent serviceIntent = new Intent(context, ToDoLiveCardService.class);
            context.startService(serviceIntent);
        }
    }
}
