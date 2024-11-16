public class ServerModel {
	private Socket connection;
	private BufferedReader reader;
	private PrintWriter writer;
	private String nickname;

	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;	
	}
	
	public String getNickname() {
		return this.nickname;
	}

	public ServerModel(String host, int port, String nickname) {
		this.host = host;
		this.port = port;

		connection = new Socket(host, port);
		reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		writer = new PrintWriter(connection.getOutputStream(), true);

		setNickname(nickname);
	}

	public void setNickname(String nickname) {
		//TODO nickname setting protocol
		//
		this.nickname = nickname;
	}

		
}
