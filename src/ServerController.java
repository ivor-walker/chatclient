public class ServerController {
        private ServerModel model;
	private ServerView view;
	
	public ServerController(ClientController clientController) {
		this.view = view;
	}
	
	private boolean connect(String host, int port, String nickname) {
		try {
               		model = new ServerModel(host, port, nickname;
			setupListeners();
			return true;
		}

                //Client input errors 
                catch (IllegalArgumentException e) {
                        view.setConnectionResult("Invalid port number. Port must be between between 0 and 65535, inclusive.");
                } catch (NullPointerException e) {
                        view.setConnectionResult("Host cannot be null.");

                //Client connection errors (400)        
                } catch (UnknownHostException e) { 
                        view.setConnectionResult("Host not found. Check the spelling of the host, your internet connection, and your network configuration.");
                } catch (NoRouteToHostException e) {
                        view.setConnectionResult("Host found, but is unreachable. Check your firewall. If entering an IP address, double check its spelling. Otherwise, please contact the server's administrator.");
                } catch (ConnectException e) {
                        view.setConnectionResult("Host found, but connection refused by server. Double check your port number is correct. Otherwise, please contact the server's administrator and ask them to open this port.");

                //Server connection errors (500)
                } catch (SecurityException e) {
                        view.setConnectionResult("Permission to connect denied. If you believe this is in error, please contact the server's administrator and ask them to update their security manager.");
                } catch (BindException e) {
                        view.setConnectionResult("Port is already in use at this server. Please try a different port.");

                //Unknown errors
                } catch (IOexception e) {
                        view.setConnectionResult("Unkown IO exception. " + e.getMessage());
                } catch (e) {
                        view.setConnectionResult("Unknown exception. " + e.getMessage());
                }

		return false;
        }

	private void setupListeners() {
		view.updateNewServerListener(() -> updateNewServer());
		view.commitNewServerListener((host, port, nickname) -> commitNewServer(host, port, nickname));
		view.commitExistingServerListener((host, port, nickname) -> commitExistingServer(host, port, nickname));
	}

        public void updateNewServer() {
                view.setupServerForm();
        }

	//Problem function
	public void commitNewServer(String host, int port, String nickname) {
		if(connect(host, port, nickname)) {
			//One button needs to be created	
			view.addServer(host, port);
			//Then this new button updated with an action listener to trigger updateExistingServer
			view.updateExistingServerListener(host, port, (host, port) -> updateExistingServer);
			view.setConnectionResult("Server added successfully!");
		}
	}

	public void updateExistingServer() {
		view.setupServerForm(model.getHost(), model.getPort(), model.getNickname());	
	}

	public void commitExistingServer(String host, int port, String nickname) {
		if(connect(host, port, nickname)) {
			view.updateServer(model.getHost(), model.getPort());
			view.setConnectionResult("Server updated successfully!");
		}
	}
}        

