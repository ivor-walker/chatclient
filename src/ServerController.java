public class ServerController {
        private ServerModel model;
	private ServerView view;
	
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

	private void setupListeners() {
		view.updateNewServerListener(() -> updateNewServer());
		view.commitNewServerListener((host, port, nickname) -> commitNewServer(host, port, nickname));
		view.commitExistingServerListener((host, port, nickname) -> commitExistingServer(host, port, nickname));
	}

        public void updateNewServer() {
                view.setupServerForm();
        }

	public void commitNewServer(String host, int port, String nickname) {
		if(connect(host, port, nickname)) {
			view.addServer(host, port);
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

