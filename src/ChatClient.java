public class ChatClient {
	private ServerView serverView;
	private ArrayList<ServerController> serverControllers;
	private static int WIDTH = 800;
	private static int HEIGHT = 600;

	public static void main(String[] args) {
		this.serverView = new ServerView(WIDTH, HEIGHT);
		
	}
}
