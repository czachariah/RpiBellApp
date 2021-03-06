package com.example.rpibell;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the GuestManagement Class where the admin can view the current guests that are
 * associated with them.
 */
public class GuestManagement extends AppCompatActivity {
    // Global variables
    public String userName;                     // current user
    public String IP;                           // IP address of the user's Raspberry Pi device
    public String token;                        // user's current token
    public String email;                        // user's email
    public String password;                     // user's password

    public Button addGuest;                     // Button takes admin to another page to add new guests
    public Button getAdminInfo;                 // Button allows for admin to obtain admin info to share to future guest
    public FloatingActionButton backButton;     // back Button

    public FirebaseAuth mAuth;                  // access to the Firebase Authentication
    public FirebaseFirestore db;                // access to the Firebase Firestore
    public FirebaseUser user;                   // the current user logged in on Firebase Authentication

    public List<Guest> guests;                  // list of guest associated to the admin
    public LinearLayout linearView;             // linear layout area where guests info is displayed

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.guest_management);

        // Initialize Firebase user and Firestore database
        mAuth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        user = mAuth.getCurrentUser();

        Log.e("current user",mAuth.getCurrentUser().getUid());

        // get info from past Activity
        IP = getIntent().getExtras().getString("IP");
        userName = getIntent().getExtras().getString("user");
        token = getIntent().getExtras().getString("token");
        email = getIntent().getExtras().getString("email");
        password = getIntent().getExtras().getString("password");


        // get list of the all the guests of the current user and display them
        guests = new ArrayList<>();
        if (user != null) {
            db.collection("guests").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // add all the associated guests (will have the admin ID in DB)
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.get("adminID").toString().equals(user.getUid())) {
                            guests.add(new Guest(document.get("name").toString(),document.get("email").toString(), document.getId()));
                        }
                    }

                    // display the guests one by one
                    linearView = findViewById(R.id.GuestList);
                    if (guests.size() < 1) {
                        Toast.makeText(getApplicationContext(), "NO ADDED GUESTS", Toast.LENGTH_LONG).show();
                    } else {
                        for (Guest guest : guests) {
                            String label = guest.name + "\n" + guest.email;
                            TextView text = new TextView(this);
                            text.setTextSize(18);
                            text.setPadding(0,50,0,50);
                            text.setText(label);
                            text.setTextColor(Color.WHITE);
                            linearView.addView(text);
                        }
                    }
                } else {
                    Log.e("TAG", "Error getting documents: ", task.getException());
                    Toast.makeText(this, "ERROR OBTAINING USER INFO. PLEASE TRY AGAIN...", Toast.LENGTH_LONG).show();
                }
            });
        }




        // Adding a guest
        addGuest = findViewById(R.id.addGuest);
        addGuest.setOnClickListener(view -> {
            Intent intent = new Intent(GuestManagement.this, AddingGuest.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });


        // get admin info to give to a future trusted guest
        getAdminInfo = findViewById(R.id.adminInfoButton);
        getAdminInfo.setOnClickListener(get -> {
            Intent intent = new Intent(GuestManagement.this, AdminInfoPage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });


        // go back to the admin home page
        backButton = findViewById(R.id.backButtonManageGuestPage);
        backButton.setOnClickListener(task -> {
            Intent intent = new Intent(GuestManagement.this, UserHomePage.class);
            intent.putExtra("user", userName);
            intent.putExtra("IP",IP);
            intent.putExtra("token", token);
            intent.putExtra("email",email);
            intent.putExtra("password",password);
            startActivity(intent);
            finish();
        });
    }
} // ends the class
