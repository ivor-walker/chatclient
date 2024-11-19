public class ServerController {
	private ServerView view;
	
	private HashMap<String, ServerModel> serverModels = new HashMap<String, ServerModel>();	
	private ServerModel activeServerModel;

	private String serverString;

	public ServerController(width, height) {
		view = new ServerView(width, height);
		setupListeners();
	}
	
	private void setupFormListeners() {
		view.viewNewServerListener(() -> viewNewServer());
		view.commitNewServerListener((host, port, nickname) -> commitNewServer(host, port, nickname));
		view.commitExistingServerListener(oldHost, oldPort, ((oldHost, oldPort, port, nickname) -> commitExistingServer(serverString, host, port, nickname)));
		view.removeExistingServerListener(serverString -> removeExistingServer(serverString));
	}
	
        public void viewNewServer() {
                view.setupServerForm();
        }

	public void commitNewServer(String host, int port, String nickname) {
		ServerModel newServerModel = connect(host, port, nickname);
		if(newServerModel) {
			String serverString = newServerModel.getString();
			view.addServer(serverString);
			view.updateExistingServerListener(serverString, (serverString -> viewExistingServer(serverString)));
			view.setConnectionResult("Server added successfully!");
		}
	}

	public void viewExistingServer(String serverString, boolean editingEnabled) {
		ServerModel serverModel = serverModels.get(serverString);
		if(editingEnabled) {	
			view.setupServerForm(serverModel.getHost(), toString(serverModel.getPort()), serverModel.getNickname());
		} else {
			setActive(serverModel);	
		}	
	}

	public void commitExistingServer(String oldHost, int oldPort, String host, int port, String nickname) {
		ServerModel oldServerModel = getServerModel(oldHost, oldPort);	
		ServerModel newServerModel = connect(host, port, nickname);	
		if(newServerModel) {
			view.updateServer(oldServerModel.toString(), newServerModel.toString());
			oldServerModel.disconnect();	
			view.setConnectionResult("Server updated successfully!");
		}
	}
	
	public void removeExistingServer(String host, int port, String nickname) {
		ServerModel serverModel = getServerModel(host, port, nickname);
		serverModel.disconnect();	
		
		String serverString = serverModel.toString();
		view.removeServer(serverString);
	}

	private boolean connect(String host, int port, String nickname) {
		try {
               		model = new ServerModel(host, port, nickname;
			serverModels.put(model.toString(), model);			
			return model;
		}

                catch (Exception e) {
			view.setConnectionResult(e.getMessage());
			return false;
		}
        }

	private ServerModel getServerModel(String host, int port) {
		return serverModels.values()
			.find(e -> e.equals(host, port);

	}	
	
	private ServerModel getServerModel(String host, int port, String nickname) {
		return serverModels.values()
			.find(e -> e.equals(host, port, nickname);

	}

	public void setActive(ServerModel serverModel) {
		view.changeActive(active.toString(), serverModel.toString());
		active = serverModel;
	}
	
	public boolean getActive() {
		return this.active();
	}
}        
