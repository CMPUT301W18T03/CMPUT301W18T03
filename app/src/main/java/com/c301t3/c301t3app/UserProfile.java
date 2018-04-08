package com.c301t3.c301t3app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Kvongaza on 2018-04-07.
 */

public class UserProfile extends AppCompatActivity {
    private UserAccount user = new UserAccount();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ElasticsearchController.GetUserByUsername getUserByName =
                new ElasticsearchController.GetUserByUsername();
        getUserByName.execute(ApplicationController.getCurrUser().getUsername());

        try {
            UserAccount user = getUserByName.get();
            final TextView tvUsername = (TextView) findViewById(R.id.tvUsername);
            final EditText etFirstName = (EditText) findViewById(R.id.etFirstName);
            final EditText etLastName = (EditText) findViewById(R.id.etLastName);
            final EditText etEmail = (EditText) findViewById(R.id.etEmail);
            final EditText etPhone = (EditText) findViewById(R.id.etPhone);
            final EditText etAddress = (EditText) findViewById(R.id.etAddress);
            final Button bSave = (Button) findViewById(R.id.bSave);
            final Button bDelete = (Button) findViewById(R.id.bDelete);
            final JsonHandler j = new JsonHandler(this);


            tvUsername.setText(user.getUsername());
            etFirstName.setText(user.getFirstName(), TextView.BufferType.EDITABLE);
            etLastName.setText(user.getLastName(), TextView.BufferType.EDITABLE);
            etEmail.setText(user.getEmailAdd(), TextView.BufferType.EDITABLE);
            etPhone.setText(user.getPhoneNum(), TextView.BufferType.EDITABLE);
            etAddress.setText(user.getAddress(), TextView.BufferType.EDITABLE);

            bSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    UserAccount account = ApplicationController.getCurrUser();
                    ElasticsearchController.UpdateUser updateUser =
                            new ElasticsearchController.UpdateUser();

                    account.setFirstName(etFirstName.getText().toString());
                    account.setLastName(etLastName.getText().toString());
                    account.setEmailAdd(etEmail.getText().toString());
                    account.setPhoneNum(etPhone.getText().toString());
                    account.setAddress(etAddress.getText().toString());

                    updateUser.execute(account);
                    j.dumpUser(account);

                    Intent mainIntent = new Intent(UserProfile.this, MainMenuActivity.class);
                    UserProfile.this.startActivity(mainIntent);
                }
            });

            bDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    //source: https://stackoverflow.com/questions/36747369/how-to-show-a-pop-up-in-android-studio-to-confirm-an-order;
                    AlertDialog.Builder builder = new AlertDialog.Builder(UserProfile.this);
                    //builder.setCancelable(true);
                    builder.setTitle("Delete User");
                    builder.setMessage("Are you sure you want to permanently delete user?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ElasticsearchController.DeleteUser delUser =
                                            new ElasticsearchController.DeleteUser();
                                    ElasticsearchController.GetTaskByOwner getTaskByOwner =
                                            new ElasticsearchController.GetTaskByOwner();
                                    ElasticsearchController.DeleteTask delTask =
                                            new ElasticsearchController.DeleteTask();

                                    getTaskByOwner.execute(ApplicationController.getCurrUser().getID());
                                    try {
                                        ArrayList<Task> currUserTasks = getTaskByOwner.get();
                                        Log.i("currUserTasks content",currUserTasks.toString());
                                        for (Task t : currUserTasks) {
                                            Log.i("T",t.getId());
                                            delTask.execute(t.getId());
                                            delTask = new ElasticsearchController.DeleteTask();
                                            Log.i("Runs","Executes once");
                                        }
                                        delUser.execute(ApplicationController.getCurrUser().getUsername());
                                        dialog.dismiss();
                                        ApplicationController.clearUser();
                                        Intent welcomeIntent =
                                                new Intent(UserProfile.this,
                                                        WelcomeActivity.class);
                                        UserProfile.this.startActivity(welcomeIntent);
                                    }
                                    catch (Exception e) {
                                        Log.i("E","No tasks found to delete for user");
                                    }
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    AppCompatDialog dialog = builder.create();
                    dialog.show();
                }
            });
        }
        catch(Exception e) {Log.i("E","Failed to find user");}
    }
}
