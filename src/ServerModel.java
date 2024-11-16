public class ServerModel {
	//TODO	
	private Socket connection;
	private BufferedReader reader;
	private PrintWriter writer;
	private String nickname;

	public void connect(String host, int port, String nickname) {
		try {	
			connection = new Socket(host, port);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new PrintWriter(connection.getOutputStream(), true);
			nickname = nickname;	
			showConnectionResult("Connection successful!");
			addServerView();
		}

		//Client input errors 
		catch (IllegalArgumentException e) {
			showConnectionResult("Invalid port number. Port must be between between 0 and 65535, inclusive.");
		} catch (NullPointerException e) {
			showConnectionResult("Host cannot be null.");
		
		//Client connection errors (400)	
		} catch (UnknownHostException e) {
			showConnectionResult("Host not found. Check the spelling of the host, your internet connection, and your network configuration.");
		} catch (NoRouteToHostException e) {
			showConnectionResult("Host found, but is unreachable. Check your firewall. If entering an IP address, double check its spelling. Otherwise, please contact the server's administrator.");
		} catch (ConnectException e) {
			showConnectionResult("Host found, but connection refused by server. Double check your port number is correct. Otherwise, please contact the server's administrator and ask them to open this port.");
		
		//Server connection errors (500)
		} catch (SecurityException e) {
			showConnectionResult("Permission to connect denied. If you believe this is in error, please contact the server's administrator and ask them to update their security manager.");
		} catch (BindException e) {
			showConnectionResult("Port is already in use at this server. Please try a different port.");

		//Unknown errors
		} catch (IOexception e) {
			showConnectionResult("Unkown IO exception. " + e.getMessage());
		} catch (e) {
			showConnectionResult("Unknown error. " + e.getMessage());  
		}
	}

	private void showConnectionResult(String connectionResult) {
		//TODO send result to view	
	}

	private void addServer() {
		//TODO add this connection to servers list gui
	}
}
