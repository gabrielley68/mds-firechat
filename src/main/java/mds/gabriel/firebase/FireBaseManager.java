package mds.gabriel.firebase;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
	ArrayList<Room> rooms = new ArrayList<Room>();
	
	private ChildEventListener printMsgListener = new ChildEventListener() {

		@Override
		public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
			String message = snapshot.getValue(String.class);
			System.out.println(message);
		}

		@Override
		public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChildRemoved(DataSnapshot snapshot) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCancelled(DatabaseError error) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
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
		this.attachRoomsEvent();
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
	
	private void attachRoomsEvent() {
		DatabaseReference roomsRef = this.getDbReference().child("currentRooms");
		
		roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {

			@Override
			public void onDataChange(DataSnapshot snapshot) {
				for(DataSnapshot roomSnapshot : snapshot.getChildren()) {
					Room room = roomSnapshot.getValue(Room.class);
					room.setKey(roomSnapshot.getKey());
					rooms.add(room);
				}
			}

			@Override
			public void onCancelled(DatabaseError error) {
				System.err.println(error.getMessage());
			}
			
		});
		
		roomsRef.addChildEventListener(new ChildEventListener() {

			@Override
			public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
				Room newRoom = Room.convertSnapshotToRoom(snapshot);
				newRoom.setKey(snapshot.getKey());
				if(!rooms.contains(newRoom)) {
					rooms.add(newRoom);
				}
			}

			@Override
			public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
				Room room = Room.convertSnapshotToRoom(snapshot);
				rooms.remove(room);
				rooms.add(room);
			}

			@Override
			public void onChildRemoved(DataSnapshot snapshot) {
				Room room = Room.convertSnapshotToRoom(snapshot);
				rooms.remove(room);
			}

			@Override
			public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onCancelled(DatabaseError error) {
				System.err.print(error.getMessage());
			}
		});
	}
	
	public String addUser(User user) {
		DatabaseReference usersRef = this.getDbReference().child("currentUsers");
		DatabaseReference pushedUserRef = usersRef.push();
		pushedUserRef.setValueAsync(user);
		return pushedUserRef.getKey();
	}
	
	public void removeUser(User user) {
		DatabaseReference usersRef = this.getDbReference().child("currentUsers");
		Map<String, Object> update = new HashMap<>();
		update.put(user.getKey(), null);
		usersRef.updateChildrenAsync(update);
	}
	
	public ArrayList<User> getUsers() {
		return this.users;
	}

	public String addRoom(Room room) {
		DatabaseReference roomsRef = this.getDbReference().child("currentRooms");
		DatabaseReference pushedRoomRef = roomsRef.push();
		pushedRoomRef.setValueAsync(room);
		return pushedRoomRef.getKey();
	}
	
	public Room getRoom(String name) {
		for(Room room : rooms) {
			if(room.getName().equals(name)) {
				return room;
			}
		}
		return null;
	}

	public void addUserToRoom(Room room, User user) {
		DatabaseReference roomsRef = this.getDbReference().child("currentRooms");
		room.users.add(user.getKey());
		
		Map<String, Object> update = new HashMap<>();
		update.put(room.getKey(), room);
		roomsRef.updateChildrenAsync(update);
		
		
	}
	
	public void sendMsg(String message, Room room) {
		DatabaseReference roomsRef = this.getDbReference().child("currentRooms");
		room.messages.add(message);
		
		Map<String, Object> update = new HashMap<>();
		update.put(room.getKey(), room);
		roomsRef.updateChildrenAsync(update);
	}

	public void removeUserFromRoom(Room room, User user) {
		room.users.remove(user.getKey());
		DatabaseReference roomsRef = this.getDbReference().child("currentRooms");

		Map<String, Object> update = new HashMap<>();
		String key = room.getKey();
		if(room.users.isEmpty()) {
			room = null;
		}
		update.put(key, room);
		roomsRef.updateChildrenAsync(update);
	}
	
	public void addMsgListener(Room room) {
		DatabaseReference roomsRef = this.getDbReference().child("currentRooms");
		roomsRef.child(room.getKey() + "/messages").addChildEventListener(this.printMsgListener);
	}
	
	public void removeMsgListener(Room room) {
		DatabaseReference roomsRef = this.getDbReference().child("currentRooms");
		roomsRef.child(room.getKey() + "/messages").removeEventListener(this.printMsgListener);

	}
	
	public ArrayList<Room> getRooms(){
		return this.rooms;
	}
	
}
