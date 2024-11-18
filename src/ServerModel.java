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
	
		try {
			connection = new Socket(host, port);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new PrintWriter(connection.getOutputStream(), true);

			setNickname(nickname);
		} 

	       //Client input errors 
		catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid port number. Port must be between between 0 and 65535, inclusive.");
		} catch (NullPointerException e) {
			throw new NullPointerException("Host cannot be null.");

		//Client connection errors (400)	
		} catch (UnknownHostException e) {
			throw new UnknownHostException("Host not found. Check the spelling of the host, your internet connection, and your network configuration.");
		} catch (NoRouteToHostException e) {
			throw new NoRouteToHostException("Host found, but is unreachable. Check your firewall. If entering an IP address, double check its spelling. Otherwise, please contact the server's administrator.");
		} catch (ConnectException e) {
			throw new ConnectionException("Host found, but connection refused by server. Double check your port number is correct. Otherwise, please contact the server's administrator and ask them to open this port.");

		//Server connection errors (500)
		} catch (SecurityException e) {
			throw new SecurityException("Permission to connect denied. If you believe this is in error, please contact the server's administrator and ask them to update their security manager.");
		} catch (BindException e) {
			throw new BindException("Port is already in use at this server. Please try a different port.");

		//Unknown errors
		} catch (IOException e) {
			throw new IOException("Unkown IO exception. " + e.getMessage());
		} catch (e) {
			throw new Exception("Unknown exception. " + e.getMessage());
		}

	}

	public void setNickname(String nickname) {
		//TODO nickname setting protocol
		//
		this.nickname = nickname;
	}
	
	//TODO
	public void disconnect() {
		()
	}

		
}
