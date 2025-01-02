import java.awt.event.ActionListener;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


public class ServerController implements ServerListener {

    private HashMap<String, ServerModel> serverModels = new HashMap<>();


    public ServerModel activeServerModel;


    private String activeServerString;


    private ServerPanel view;


    public ServerController(ServerPanel view) {
        this.view = view;
        setupListeners(); 
    }


    private void setupListeners() {

        view.viewNewServerListener(e -> viewNewServer());


        view.commitNewServerListener(e -> commitNewServer());


        addShutdownHook();
    }


    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            for (String serverKey : serverModels.keySet()) {
                serverModels.get(serverKey).disconnect();
            }
        }));
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
            serverModels.put(serverString, newServerModel);


            view.viewExistingServerListener(serverString, e -> viewExistingServer(serverString));


            view.setConnectionResult("Server added successfully!");
            System.out.println("ServerController: new server added " + serverString);


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

        activeServerString = serverModel != null ? serverModel.toString() : null;

        if (activeServerModel == null) {

            view.setActive(activeServerString);
        } else {

            String oldServerString = activeServerModel.toString();
            view.changeActive(oldServerString, activeServerString);
        }


        activeServerModel = serverModel;


        for (ActiveServerListener activeListener : activeListeners) {
            activeListener.onSetActiveServer(serverModel);
        }


        System.out.println("ServerController: active server changed to " + activeServerString);
    }


    private void unsetActive() {
        setActive(null);
    }


    public ServerModel getActive() {
        return this.activeServerModel;
    }


    public void onError(String errorMessage) {

        view.setConnectionResult(errorMessage);
    }


    private List<ActiveServerListener> activeListeners = new ArrayList<>();


    public void addActiveListener(ActiveServerListener activeListener) {

        activeListeners.add(activeListener);
    }
}	 
