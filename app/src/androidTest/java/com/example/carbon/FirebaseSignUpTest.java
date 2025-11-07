package com.example.carbon;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class FirebaseSignUpTest {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Before
    public void setup() {
        FirebaseApp.initializeApp(androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext());
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Test
    public void testCreateFirebaseUserAndSaveToFirestore() throws Exception {
        String email = "testuser" + System.currentTimeMillis() + "@example.com";
        String password = "abcdefgh";
        String firstName = "Test";
        String lastName = "User";
        String phoneNo = "5875556789";

        Tasks.await(mAuth.createUserWithEmailAndPassword(email, password));

        FirebaseUser user = mAuth.getCurrentUser();
        assertNotNull("Firebase user should not be null after creation", user);

        String userId = user.getUid();
        UserData newUser = new UserData(firstName, lastName, email, phoneNo, "entrant");
        Tasks.await(db.collection("users").document(userId).set(newUser));

        UserData fetchedUser = Tasks.await(
                db.collection("users").document(userId).get()
        ).toObject(UserData.class);

        assertNotNull("Fetched user should not be null", fetchedUser);
        assertEquals("Email should match", email, fetchedUser.email);
        assertEquals("Role should be entrant", "entrant", fetchedUser.role);
    }

    static class UserData {
        public String firstName, lastName, email, phoneNo, role;
        public UserData() {}
        public UserData(String f, String l, String e, String p, String r) {
            firstName = f; lastName = l; email = e; phoneNo = p; role = r;
        }
    }
}
