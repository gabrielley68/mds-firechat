package mds.gabriel.firebase;

import java.util.ArrayList;
import java.util.Scanner;

public class UserInteraction {
	private User currentUser;
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
				default:
					this.help();
					break;
			}
		} catch(IllegalArgumentException e){
			this.help();
		}
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
	
	private void help() {
		System.out.println("Voici les commandes disponibles :");
		
		System.out.println("  /log [username] : se connecte avec le pseudo [username]");
		System.out.println("  /show_users : liste les utilisateurs connectés");
	}
}
