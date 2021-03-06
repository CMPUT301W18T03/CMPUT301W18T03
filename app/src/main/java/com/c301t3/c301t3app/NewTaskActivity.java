package com.c301t3.c301t3app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Jonah on 2018-03-09.
 * Class that handles that creation of a new task, with appropriate fields and buttons
 */

public class NewTaskActivity extends AppCompatActivity {
    public static final int GET_FROM_GALLERY = 3;
    private NewTaskActivity activity = this;

    private ImageView userPicture;

    private TaskPasser passer;
    private Task newTask;
    private Bitmap picture;

    /**
     * Sets up an initializes the Activity, its mainly in charge of having its "save" button
     * detect if there is any interaction (clicks) so that it can gather all relevant information to
     * create a Task when it detects so.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_task_activity);
        final TaskPasser passer = new TaskPasser();
        newTask = new Task();

        final EditText nameText = findViewById(R.id.newTaskName);
        final EditText descText = findViewById(R.id.newTaskDescription);
        final EditText priceText = findViewById(R.id.newTaskPrice);

        Button saveButton = findViewById(R.id.createButton);
        Button addImageButton = findViewById(R.id.addImageBtn);
        userPicture = findViewById(R.id.userPic);

        if (picture == null) {
             picture = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.logo_big);
             picture = processImage(picture);
        }
        userPicture.setImageBitmap(picture);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ApplicationController.getCurrUser() == null) {
                    Intent loginIntent = new Intent(activity, SimpleLoginActivity.class);
                    activity.startActivity(loginIntent);
                }

                boolean end = false;
                String price;

                try {
                    newTask.setName(nameText.getText().toString());
                    newTask.setDescription(descText.getText().toString());
                    price = priceText.getText().toString();
                    newTask.setPicture(picture);
                    end = true;
                } catch (java.lang.IllegalArgumentException e) {
                    Snackbar errorMsg = Snackbar.make(findViewById(R.id.mainConstraint),
                            R.string.name_error,
                            Snackbar.LENGTH_SHORT);
                    price = "0";
                    errorMsg.show();
                    picture = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.logo_big);
                    picture = processImage(picture);
                }
                newTask.setPrice(Float.valueOf(price));
                newTask.setStatus(TaskStatus.REQUESTED);

                ArrayList<Task> t = new ArrayList<>();
                t.add(newTask);
                passer.setTasks(t);

                ElasticsearchController.taskToServer(newTask);

                if(end) {
                    finish();
                }

            }
        });

    }

    /**
     * The method mainly catches data that is sent back from a recent activity startup,
     * the data the it mainly catches are Bitmap images from the Gallery Activity.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Catching image from gallery taken from StackOverFlow post: https://stackoverflow.com/questions/9107900/how-to-upload-image-from-gallery-in-android
        // Detects request codes
        if(requestCode==GET_FROM_GALLERY && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            if (bitmap != null) {
                picture = processImage(bitmap);
                userPicture.setImageBitmap(picture);
            }

        }

    }

    /**
     * The method is used to launch an activity to pick out coordinates
     * from google maps by the click of the "map" icon
     *
     * @param view
     */
    public void goToMap(View view) {
        Intent mapIntent = new Intent(this, addingTaskLocal.class);
        startActivity(mapIntent);
    }

    /**
     * The method is used to launch an activity to select an image
     * from the Gallery by the click of the Add Image button.
     *
     * @param view
     */
    public void addImage(View view) {
        Toast.makeText(getApplicationContext(), "Add Image", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY);
    }

    /**
     * The method autocompresses a given bitmap to take up much smaller space than before in
     * order to consistently satisfy the condition that all bitmaps stored are within 65536 bytes
     *
     * @param picture: Represents the uncompressed bitmap image
     * @return newPicture: Represents the compressed bitmap image
     */
    private Bitmap processImage(Bitmap picture) {
        Bitmap newPicture = picture;
        float span = Math.max(newPicture.getHeight(), newPicture.getWidth());

        while (true) {
            if (newPicture.getByteCount() > ApplicationController.MAX_PHOTO_BYTESIZE) {
                span = (span / 4) * 3;
                newPicture = scaleDown(newPicture, span, true);
            } else if (span > 4096) {
                span = 4096;
                newPicture = scaleDown(newPicture, span, true);
            } else {
                break;
            }
        }

        return newPicture;
    }

    /**
     * The method that scales down an image's resolution whilst keeping its aspect ratio.
     *
     * @param realImage: The original Bitmap image to be modified
     * @param maxImageSize: The length of the longest dimension (length, width) of the newBitmap
     * @param filter: Boolean value for Bitmap.createScaledBitmap(...)
     * @return newBitmap: Represents the modified bitmap
     */
    private static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

}


