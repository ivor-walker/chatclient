public class ServerController {
        private ServerModel model;
	private ServerView view;

	private boolean active;
	
	public ServerController(ServerView view) {
		this.view = view;	
	}
	
	private boolean connect(String host, int port, String nickname) {
		try {
               		model = new ServerModel(host, port, nickname;
			setupListeners();
			return true;
		}

                catch (Exception e) {
			view.setConnectionResult(e.getMessage());
			return false;
		}
        }

	private void setupFormListeners() {
		view.updateNewServerListener(() -> viewNewServer());
		view.commitNewServerListener((host, port, nickname) -> commitNewServer(host, port, nickname));
		view.commitExistingServerListener((host, port, nickname) -> commitExistingServer(host, port, nickname));
		view.removeExistingServerListener((host, port) -> removeExistingServer(host, port));
	}

	private void setupActiveListeners() {
		view.	
	}

        public void viewNewServer() {
                view.setupServerForm();
        }

	public void commitNewServer(String host, int port, String nickname) {
		if(connect(host, port, nickname)) {
			view.addServer(host, port);
			view.updateExistingServerListener(host, port, (host, port) -> viewExistingServer);
			view.setConnectionResult("Server added successfully!");
		}
	}

	public void viewExistingServer() {
		view.setupServerForm(model.getHost(), model.getPort(), model.getNickname());	
	}

	public void commitExistingServer(String host, int port, String nickname) {
		String oldHost = model.getHost();
		int oldPort = model.getPort();
		if(connect(host, port, nickname)) {
			view.updateServer(oldHost, oldPort, model.getHost(), model.getPort());
			view.setConnectionResult("Server updated successfully!");
		}
	}

	public void removeExistingServer() {
		model.disconnect();
		view.removeServer(model.getHost(), model.getPort());
	}

	public boolean getActive() {
		return this.active();
	}

	public void setActive(boolean activityStatus) {
		this.active = activityStatus;
		view.setActive(model.getHost(), model.getPort());
	}
}        

