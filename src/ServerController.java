import java.awt.event.ActionListener;

import java.util.HashMap;

public class ServerController {
	private ServerView view;
	
	private HashMap<String, ServerModel> serverModels = new HashMap<String, ServerModel>();	
	private ServerModel activeServerModel;

	private String serverString;

	public ServerController(int width, int height) {
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
		if(newServerModel == null) {
			return;
		}
		
		String serverString = newServerModel.toString();
		view.addServer(serverString);
		view.updateExistingServerListener(serverString, e -> viewExistingServer(serverString));
		view.setConnectionResult("Server added successfully!");
	}

	private boolean editingEnabled = false;
	private String editingServerString;
	private ServerModel editingServerModel;
	
	private void setEditingServer(String editingServerString) {
		this.editingServerString = editingServerString;
		editingServerModel = serverModels.get(editingServerString);	
	}

	private void viewExistingServer(String serverString) {
		ServerModel serverModel = serverModels.get(serverString);
		if(editingEnabled) {
			setEditingServer(serverString);	
			serverModel.getNicknameFuture().thenAccept(nickname -> {
				view.setupServerForm(serverModel.getHost(), serverModel.getPort(), nickname);
			});
		} else {
			setActive(serverModel);	
		}	
	}

	private void commitExistingServer() {
		ServerModel oldServerModel = serverModels.get(editingServerString);	
		
		ServerModel newServerModel = connect();
		if(newServerModel == null) {
			return;
		}

		oldServerModel.disconnect().thenRun(() -> {
			String newServerString = newServerModel.toString();

			setEditingServer(newServerString);	

			view.updateServer(oldServerModel.toString(), newServerString);
			view.setConnectionResult("Server updated successfully!");
		}).exceptionally(e -> {
			handleError(e.getMessage());	
			return null;	
		});
	}

	private void toggleEditing() {
		editingEnabled = !editingEnabled;
		view.setEditingEnabled(editingEnabled);	
	}
	
	private void removeExistingServer() {
		editingServerModel.disconnect().thenRun(() -> {
			view.removeServer(editingServerString);
		}).exceptionally(e -> {
			handleError(e.getMessage());	
			return null;
		});
	}

	private ServerModel connect() {
		String host = view.getHost();
		int port = Integer.parseInt(view.getPort());
		String nickname = view.getNickname();

		return connect(host, port, nickname);
	}

	private ServerModel connect(String host, int port, String nickname) {
		ServerModel model = new ServerModel(host, port, nickname);
		model.getConnectFuture().thenApply(() -> {
			serverModels.put(model.toString(), model);			
			return model;	
		}).exceptionally(e -> {
			handleError(e.getMessage());	
			return null;
		});
	}

	private void handleError(String errorMessage) {
		view.setConnectionResult(errorMessage);
	}

	private void setActive(ServerModel serverModel) {
		view.changeActive(activeServerModel.toString(), serverModel.toString());
		activeServerModel = serverModel;
	}
	
	public boolean getActive() {
		return this.activeServerModel;
	}
}	 
