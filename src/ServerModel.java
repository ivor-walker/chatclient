public class ServerModel {
	private Socket connection;
	private BufferedReader reader;
	private PrintWriter writer;
	
	private String nickname;
	
	private boolean isActive

	public void setActivity(boolean activityState) {
		this.isActive = activityState;	
	}

	public boolean getActivity() {
		return isActive;
	}		
		
	private throughSocket(Consumer<T> action) {
		try {
			action
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

	public ServerModel(String host, int port, String nickname) {
		throughSocket(e -> {
			connection = new Socket(host, port);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new PrintWriter(connection.getOutputStream(), true);
			setNickname(nickname);
		});
	}

	//Public methods representing server commands	
	public void setNickname(String nickname) {
		throughSocket(e -> {
			writer.println("NICK "+nickname);
		});
		this.nickname = nickname;
	}
	
	public void disconnect() {
		throughSocket(e -> {
			writer.println("QUIT");	
		});
	}
	
	public void joinChannel(String channel) {
		throughSocket(e -> {
			writer.println("JOIN "+channel);	
		});
	}

	public void leaveChannel(String channel) {
		throughSocket(e -> {
			writer.println("PART "+channel);
		});
	}

	public void getNamesInChannel(String channel) {
		throughSocket(e -> {
			writer.println("NAMES "+channel);
		});
	}

	public void getChannels() {
		throughSocket(e -> {
			writer.println("LIST");
		});
	}

	public void privateMessage(String target, String message) {
		throughSocket(e -> {
			writer.println("PRIVMSG "+target+":"+message);
		});
	}

	public void getTime() {
		throughSocket(e -> {
			writer.println("TIME");
		});
	}

	public void getInfo() {
		throughSocket(e -> {
			writer.println("INFO");
		});
	}

	public void ping(String message) {
		throughSocket(e -> {
			writer.println("PING");
		});
	}

	//Getters of connection info
	public String getHost() {
		return this.host;
	}

	public int getPort() {
		return this.port;	
	}
	
	public String getNickname() {
		return this.nickname;
	}
	private String toString() {
		return host + ":" + toString(port); 
	}
}
