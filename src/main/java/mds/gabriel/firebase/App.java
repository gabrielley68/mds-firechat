package mds.gabriel.firebase;

public class App {
    public static void main(String[] args) {
    	System.out.println("Bienvenue sur le firechat !");
    	
        FireBaseManager dbManager = new FireBaseManager();
        UserInteraction ui = new UserInteraction(dbManager);
        
        ui.run();
    }
}
