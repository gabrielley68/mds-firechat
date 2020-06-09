package mds.gabriel.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import com.google.api.core.SettableApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FireBaseManager {

	ArrayList<User> users = new ArrayList<User>();
	
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
		
		this.attachEvents();
	}
	
	public DatabaseReference getDbReference() {
		final FirebaseDatabase database = FirebaseDatabase.getInstance();
		return database.getReference("");
	}
	
	private void attachEvents() {
		this.attachUsersEvent();
	}
	
	private void attachUsersEvent() {
		DatabaseReference usersRef = this.getDbReference().child("currentUsers");
		
		// Get initial value
		final SettableApiFuture <ArrayList<User>> future = SettableApiFuture.create();
		usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				ArrayList<User> users = new ArrayList<>();
				for(DataSnapshot userSnapshot : snapshot.getChildren()) {					
					User user = userSnapshot.getValue(User.class);
					user.setKey(userSnapshot.getKey());
					users.add(user);
				}
				
				future.set(users);
			}

			@Override
			public void onCancelled(DatabaseError error) {
				System.err.println(error.getMessage());
				future.set(new ArrayList<User>());
			}
		});
		
		try {
			this.users = future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		
		// Update users on change
		usersRef.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
				User newUser = snapshot.getValue(User.class);
				newUser.setKey(snapshot.getKey());
				
				if(!users.contains(newUser)) {
					users.add(newUser);
				}
			}

			@Override
			public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onChildRemoved(DataSnapshot snapshot) {
				User removedUser = null;
				for (User user : users) {
					if(user.getKey().equals(snapshot.getKey())) {
						removedUser = user;
					}
				}
				
				if(removedUser != null) {
					users.remove(removedUser);
				}
			}

			@Override
			public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onCancelled(DatabaseError error) {
				System.err.println(error.getMessage());
			}
		});
	}
	
	public String addUser(User user) {
		DatabaseReference usersRef = this.getDbReference().child("currentUsers");
		DatabaseReference pushedUserRef = usersRef.push();
		pushedUserRef.setValueAsync(user);
		return pushedUserRef.getKey();
	}
	
	public ArrayList<User> getUsers() {
		return this.users;
	}
	
}
