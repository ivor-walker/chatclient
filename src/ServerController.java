import java.util.HashMap;

public class ServerController {
	private ServerView view;
	
	private HashMap<String, ServerModel> serverModels = new HashMap<String, ServerModel>();	
	private ServerModel activeServerModel;

	private String serverString;

	public ServerController(width, height) {
		view = new ServerView(width, height);
		setupListeners();
	}
	
	private void setupListeners() {
		view.viewNewServerListener(e -> viewNewServer());
		view.commitNewServerListener(e -> commitNewServer());
		view.commitExistingServerListener(e -> commitExistingServer());

		view.toggleEditingListener(e -> toggleEditing());
		view.removeExistingServerListener(e -> removeExistingServer());
	}
	
        private void viewNewServer() {
                view.setupServerForm();
        }

	private void commitNewServer() {
		ServerModel newServerModel = connect();

		if(newServerModel) {
			String serverString = newServerModel.getString();
			view.addServer(serverString);
			view.updateExistingServerListener(serverString, e -> viewExistingServer(serverString));
			view.setConnectionResult("Server added successfully!");
		}
	}

	private boolean editingEnabled = false;
	private string editingServerString;
	private ServerModel editingServerModel;
	
	private setEditingServer(String editingServerString) {
		this.editingServerString = editingServerString;
		editingServerModel = serverModels.get(editingServerString);	
	}

	private void viewExistingServer(String serverString) {
		ServerModel serverModel = serverModels.get(serverString);
		if(editingEnabled) {
			setEditingServer(serverString);	
			view.setupServerForm(serverModel.getHost(), toString(serverModel.getPort()), serverModel.getNickname());
		} else {
			setActive(serverModel);	
		}	
	}

	private void commitExistingServer(String oldServerString) {
		ServerModel oldServerModel = serverModels.get(oldServerString);	
		
		ServerModel newServerModel = connect();

		if(newServerModel) {
			view.updateServer(oldServerString, newServerModel.toString());
			oldServerModel.disconnect();	
			view.setConnectionResult("Server updated successfully!");
		}
	}

	private void toggleEditing() {
		editingEnabled = !editingEnabled;
		view.setEditingEnabled(editingEnabled);	
	}
	
	private void removeExistingServer() {
		view.removeServer(editingServerString);
		editingServerModel.disconnect();	
	}
	
	private boolean connect() {
		String host = view.getHost();
		int port = Integer.parseInt(view.getPort());
		String nickname = view.getNickname();

		return connect(host, port, nickname);
	}

	private ServerModel connect(String host, int port, String nickname) {
		try {
               		model = new ServerModel(host, port, nickname;
			serverModels.put(model.toString(), model);			
			return model;
		}

                catch (Exception e) {
			view.setConnectionResult(e.getMessage());
			return null;
		}
        }

	private void setActive(ServerModel serverModel) {
		view.changeActive(activeServerModel.toString(), serverModel.toString());
		activeServerModel = serverModel;
	}
	
	public boolean getActive() {
		return this.activeServerModel;
	}
}        
