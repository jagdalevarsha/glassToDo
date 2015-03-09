package glassproject.ubicomp.com.todo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import glassproject.ubicomp.com.todo.R;
import glassproject.ubicomp.com.todo.db.TaskItemDb;
import glassproject.ubicomp.com.todo.model.TaskItem;

/**
 * Created by Deep on 3/9/2015.
 */
public class ToDoTaskListActivity extends Activity {
    private TaskItem createdTask = null;
    private TaskItemDb db;

    private final int SPEECH = 10284;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new TaskItemDb(this);

        setContentView(R.layout.todo_task_screen);
        recordTask();
    }

    private void recordTask() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, SPEECH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            createdTask = new TaskItem(-1,spokenText,false,-1,true);

            showTaskOnView(createdTask);

            saveTask();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    finish();
                } }, 1000);
        } else {
            ((TextView) findViewById(R.id.newTaskLabel)).setVisibility(View.INVISIBLE);
            ((TextView) findViewById(R.id.taskDescription)).setVisibility(View.INVISIBLE);

            ((TextView) findViewById(R.id.messageTextView)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.messageTextView)).setText("An error occurred! Try again later.");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    private void showTaskOnView(TaskItem task) {
        ((TextView) findViewById(R.id.newTaskLabel)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.taskDescription)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.taskDescription)).setText(task.getTaskDescription());
    }


    public void saveTask() {
        db.saveTaskItem(createdTask);
        ((TextView) findViewById(R.id.newTaskLabel)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.taskDescription)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.messageTextView)).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.messageTextView)).setText("Saved.");
    }
}
