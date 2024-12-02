import java.util.Random;

public class ChatClient {
	private static int WIDTH = 800;
	private static int HEIGHT = 600;
	
    public static void main(String[] args) {
	    ClientView clientView = new ClientView(WIDTH, HEIGHT);	

        ServerController serverController = new ServerController(clientView.getServerPanel());	
        TargetController targetController = new TargetController(clientView.getTargetPanel());
        MessageController messageController = new MessageController(clientView.getMessagePanel());

        setupListeners(serverController, targetController, messageController);

	}	
    
    private static void setupListeners(ServerController serverController, TargetController targetController, MessageController messageController) {
        serverController.addActiveListener(targetController); 
        targetController.addActiveListener(messageController); 
    }
}
