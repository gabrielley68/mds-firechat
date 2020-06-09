package mds.gabriel.firebase;

import java.util.ArrayList;
import java.util.Scanner;

public class UserInteraction {
	private User currentUser;
	private Room currentRoom;
	private Scanner scanner;
	private FireBaseManager dbManager;
	
	public UserInteraction(FireBaseManager dbManager) {
		this.scanner = new Scanner(System.in);
		this.dbManager = dbManager;
	}
	
	public void run() {
		while(true) {
			readCommand();
		}
	}
	
	private void readCommand(){
		String input = this.scanner.nextLine();
		
		try {
			switch(input.split(" ")[0]) {
				case "/log":
					this.connectUser(input);
					break;
				case "/show_users":
					this.showUsers();
					break;
				case "/create_room":
					this.createRoom(input);
					break;
				case "/log_room":
					this.joinRoom(input);
					break;
				case "/show_rooms":
					this.showRooms();
					break;
				case "/logout":
					this.disconnectUser();
					break;
				case "/msg":
					this.sendMsg(input);
					break;
				default:
					this.help();
					break;
			}
		} catch(IllegalArgumentException e){
			this.help();
		}
	}


	private void sendMsg(String input) {
		if(this.currentRoom == null) {
			System.out.println("Il faut d'abord rejoindre une salle");
		}
		
		String content = input.split(" ", 2)[1];
		
		String message = this.currentUser + " : " + content;
		
		this.dbManager.sendMsg(message, this.currentRoom);
	}

	private void disconnectUser() {
		if(this.currentUser == null) {
			System.out.println("Veuillez d'abord vous connecter");
		}
		
		if(this.currentRoom != null) {
			this.disconnectFromRoom();
		}
		this.dbManager.removeUser(this.currentUser);
		this.currentUser = null;
		System.out.println("Déconnecté");
		
	}

	private void connectUser(String input) {
		String[] args = input.split(" ");
		if(args.length < 2) {
			throw new IllegalArgumentException();
		}
		
		if(this.currentUser != null) {
			System.out.println("Déjà connecté en tant que " + this.currentUser);
			return;
		}
		
		String username = args[1];
		
		this.currentUser = new User(username);
		
		String newKey = this.dbManager.addUser(this.currentUser);
		this.currentUser.setKey(newKey);
		
		System.out.println("Bienvenue " + this.currentUser + " !");
	}
	
	private void showUsers() {
		ArrayList<User> users = this.dbManager.getUsers();
		
		if(users.isEmpty()) {
			System.out.println("Aucun utilisateur connecté");
		}
		else {
			System.out.println("Liste des utilisateurs connectés");
			for(User user : users) {
				System.out.println(" - " + user);
			}
		}
	}
	
	private void showRooms() {
		ArrayList<Room> rooms = this.dbManager.getRooms();
		
		if(rooms.isEmpty()) {
			System.out.println("Pas de salons");
		}
		else {
			System.out.println("Liste des salons");
			for(Room room : rooms) {
				System.out.println(" - " + room);
			}
		}
		
	}
	
	private void joinRoom(String input) {
		String [] args = input.split(" ");
		if (args.length < 2) {
			throw new IllegalArgumentException();
		}
		
		if(this.currentUser == null) {
			System.out.println("Veuillez d'abord vous connecter");
			return;
		}
		
		String room_name = args[1];
		Room room = dbManager.getRoom(room_name);
		if(room == null) {
			System.out.println("Le salon n'existe pas !");
			return;
		}
		
		this.connectToRoom(room);
	}
	
	private void createRoom(String input) {
		String[] args = input.split(" ");
		if(args.length < 2) {
			throw new IllegalArgumentException();
		}
		
		if(this.currentUser == null) {
			System.out.println("Veuillez d'abord vous connecter");
			return;
		}
		
		String room_name = args[1];
		
		if(dbManager.getRoom(room_name) != null) {
			System.out.println("Le salon existe déjà !");
			return;
		}
		
		Room newRoom = new Room(room_name);
		
		String newKey = this.dbManager.addRoom(newRoom);
		newRoom.setKey(newKey);
		
		System.out.println("Salon " + newRoom + " créé");

		this.connectToRoom(newRoom);
	}
	
	private void connectToRoom(Room room) {
		if(this.currentRoom != null) {
			this.disconnectFromRoom();
		}
		
		this.dbManager.addUserToRoom(room, this.currentUser);
		this.dbManager.addMsgListener(room);
		this.currentRoom = room;
		
		System.out.println("Connecté au salon " + this.currentRoom);
	}
	
	private void disconnectFromRoom() {
		this.dbManager.removeUserFromRoom(this.currentRoom, this.currentUser);
		this.dbManager.removeMsgListener(this.currentRoom);
		System.out.println("Déconnecté du salon " + this.currentRoom);
		this.currentRoom = null;
	}
	
	private void help() {
		System.out.println("Voici les commandes disponibles :");
		
		System.out.println("  /log [username] : se connecte avec le pseudo [username]");
		System.out.println("  /show_users : liste les utilisateurs connectés");
		System.out.println("  /create_room [name]: créer un salon");
		System.out.println("  /show_rooms : liste les salons ");
		System.out.println("  /log_room [name] : se connecte à un salon");
		System.out.println("  /logout : se déconnecte");
	}
}
