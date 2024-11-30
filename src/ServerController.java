import java.awt.event.ActionListener;

import java.util.HashMap;

public class ServerController implements ServerListener {
	private HashMap<String, ServerModel> serverModels = new HashMap<String, ServerModel>();	
	public ServerModel activeServerModel;
	private String activeServerString;
    private ServerPanel view;

	public ServerController(ServerPanel view) {
	    this.view = view;	
        setupListeners();
	}

	
	private void setupListeners() {
		view.viewNewServerListener(e -> viewNewServer());
	}
	
	private void viewNewServer() {
		view.setupServerForm();
		view.commitNewServerListener(e -> commitNewServer());
	}

	private void commitNewServer() {
		ServerModel newServerModel = modelFromView();
		
		newServerModel.connect().thenRun(() -> { 
			String serverString = newServerModel.toString();
			view.addServer(serverString);
			view.viewExistingServerListener(serverString, e -> viewExistingServer(serverString));
			view.setConnectionResult("Server added successfully!");
			
			setActive(newServerModel);
			addToController(newServerModel);
		});
	}
	
	private void viewExistingServer(String serverString) {
		ServerModel serverModel = serverModels.get(serverString);
		view.setupServerForm(serverModel.getHost(), serverModel.getPort(), serverModel.getNickname());
		setActive(serverModel);	
		view.removeExistingServerListener(e -> removeExistingServer());
	}

	private void removeExistingServer() {
		if(activeServerModel == null) {
			return;
		}	
		activeServerModel.disconnect().thenRun(() -> {
			view.removeServer(activeServerString);
			serverModels.remove(activeServerString);
			unsetActive();
		});	
	}

	private ServerModel modelFromView() {
		String host = view.getHost();
		String nickname = view.getNickname();
		
		String port = view.getPort();
		int portInt = 0;
		if (!port.equals("")) {
			portInt = Integer.parseInt(port);	
		}

		ServerModel model = new ServerModel(host, portInt, nickname);
		model.addServerListener(this);
		return model;
	}

	private void addToController(ServerModel model) {
		serverModels.put(model.toString(), model);
	}

	private void setActive(ServerModel serverModel) {
		activeServerString = serverModel.toString();	

		if(activeServerModel == null) {	
			view.setActive(activeServerString);
		} else {
			String oldServerString = activeServerModel.toString();	
			view.changeActive(oldServerString, activeServerString);
		}
		
		activeServerModel = serverModel;
	}

	private void unsetActive() {
		activeServerModel = null;
		activeServerString = null;
	}
	
	public ServerModel getActive() {
		return this.activeServerModel;
	}

	public void onError(String errorMessage) {
		view.setConnectionResult(errorMessage);
	}
}	 
