package mds.gabriel.firebase;

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
	
	private void help() {
		System.out.println("Voici les commandes disponibles :");
		
		System.out.println("  /log [username] : se connecte avec le pseudo [username]");
	}
}
