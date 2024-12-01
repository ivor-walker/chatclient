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
    }








    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
	private static final Random RANDOM = new Random();

	public static String generateRandomString(int length) {
	    StringBuilder sb = new StringBuilder(length);
	    for (int i = 0; i < length; i++) {
	        int randomIndex = RANDOM.nextInt(LOWERCASE.length());
	        sb.append(LOWERCASE.charAt(randomIndex));
	    }
	    return sb.toString();
	}	

	public static void testModel() {
		
		//Testing server model
		String host = "localhost";	
		int port = 25009;
		String nickname = generateRandomString(9); 
		ServerModel serverModel = new ServerModel(host, port, nickname); 

		

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");
		    serverModel.disconnect(); // Ensure clean disconnect
		}));
	
		serverModel.joinChannel("#good");
			
		serverModel.sendMessage("#good", "good");	
		serverModel.sendMessage(nickname, "good");	
		
		serverModel.getNamesInChannel("#good");	
		
		serverModel.partChannel("#good");	
			
		serverModel.getOfferedChannels();
		
		serverModel.getInfo();
		serverModel.ping("good");
		System.out.println(serverModel.getTimeFuture());
		
		while (true) {
	            // Keeps the program alive indefinitely
	            try {
	                Thread.sleep(1000); // Prevent CPU-intensive loop
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt(); // Restore interrupt flag
	                break;
	            }
	        }	
	}

	
}
