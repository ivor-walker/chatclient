import java.util.Random;

public class ChatClient {
	private static int WIDTH = 800;
	private static int HEIGHT = 600;
	
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

	public static void main(String[] args) {
		
		//Testing server model
		String host = "localhost";	
		int port = 25009;
		String nickname = generateRandomString(9); 
		ServerModel serverModel = new ServerModel(host, port, nickname); 

		

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
		            System.out.println("Shutting down...");
		            serverModel.disconnect(); // Ensure clean disconnect
		}));
	
		serverModel.joinChannel("bad");	
		serverModel.joinChannel("#good");
			
		serverModel.sendMessage("#good", "good");	
		serverModel.sendMessage(nickname, "good");	
		
		serverModel.getNamesInChannel("#good");	
		
		serverModel.partChannel("#good");	
			
		serverModel.getOfferedChannels();
		
		serverModel.getInfo();
		serverModel.ping("good");
		System.out.println(serverModel.getServerTime());
		//serverController = new ServerController(WIDTH, HEIGHT);
		
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
