package mds.gabriel.firebase;

import java.util.ArrayList;
import java.util.HashMap;

import com.google.firebase.database.DataSnapshot;

public class Room {
	private String name;
	private String key;
	public ArrayList<String> users = new ArrayList<String>();
	public ArrayList<String> messages = new ArrayList<String>();
	
	public Room() {}
	
	public Room(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public static Room convertSnapshotToRoom(DataSnapshot snapshot) {
		String room_name = ((HashMap<String, String>) snapshot.getValue()).get("name");
		
		Room room = new Room(room_name);
		
		for(DataSnapshot userSnapshot : snapshot.child("users").getChildren()) {
			room.users.add(userSnapshot.getValue(String.class));
		}
		
		return room;
	}
	
	public String toString() {
		return this.name;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Room other = (Room) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	
}
