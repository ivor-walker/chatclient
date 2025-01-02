/**
 * Main class for the ChatClient application
 */
public class ChatClient {
    // Default resolution for the client view 800x600
    private static int WIDTH = 800;
    private static int HEIGHT = 600;
   
    /**
     * Main method for the ChatClient application
     * @param args command line arguments (no arguments should be passed)
     */ 
    public static void main(String[] args) {
        // Create the main view, which contains the server, target, and message panels
        ClientView clientView = new ClientView(WIDTH, HEIGHT);  

        // Create the controllers for the server, target, and message panels
        ServerController serverController = new ServerController(clientView.getServerPanel());  
        TargetController targetController = new TargetController(clientView.getTargetPanel());
        MessageController messageController = new MessageController(clientView.getMessagePanel());

        // Add listeners to the controllers
        setupListeners(serverController, targetController, messageController);

    }   
    
    /**
     * Adds listeners to the controllers 
     * @param serverController the server controller
     * @param targetController the target controller
     * @param messageController the message controller
     */
    private static void setupListeners(ServerController serverController, TargetController targetController, MessageController messageController) {
        // targetController listens to serverController for changes to the active server
        serverController.addActiveListener(targetController); 
        // messageController listens to targetController for changes to the active target 
        targetController.addActiveListener(messageController); 
    }
}
