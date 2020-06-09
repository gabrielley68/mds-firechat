package mds.gabriel.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FireBaseManager {
	public FireBaseManager() {
		FileInputStream serviceAccount = null;
		try {
			serviceAccount = new FileInputStream("mds-firechat.json");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		FirebaseOptions options = null;
		try {
			options = new FirebaseOptions.Builder()
			  .setCredentials(GoogleCredentials.fromStream(serviceAccount))
			  .setDatabaseUrl("https://mds-firechat.firebaseio.com")
			  .build();
		} catch (IOException e) {
			e.printStackTrace();
		}

		FirebaseApp.initializeApp(options);
	}
	
	public DatabaseReference getDbReference() {
		final FirebaseDatabase database = FirebaseDatabase.getInstance();
		return database.getReference("");
	}
	
	public String addUser(User user) {
		DatabaseReference usersRef = this.getDbReference().child("currentUsers");
		DatabaseReference pushedUserRef = usersRef.push();
		pushedUserRef.setValueAsync(user);
		return pushedUserRef.getKey();
	}
	
}
