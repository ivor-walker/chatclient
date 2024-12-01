import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

import java.util.HashMap;

public class ServerPanel extends JPanel {
	private int width;
	private int height;

	private JPanel optionsPanel;	
	private JButton addServerButton;

	private JPanel serverListPanel;
    private JScrollPane scrollPane;	
    private HashMap<String, JButton> serverButtons = new HashMap<String, JButton>(); 

	private JPanel formPanel;
	private JTextField hostField;
	private JTextField portField;
	private JTextField nicknameField;
	private JButton commitButton;
	private JLabel connectionResultLabel;

	public ServerPanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));

        setLayout(new BorderLayout());

		initialiseServerList();
		initialiseForm();

        redrawServerPanel();
	}
    
	private static final double WIDTH_PROPORTION_SERVER_LIST = 0.5;	
	private void initialiseServerList() {
		serverListPanel = new JPanel();
		serverListPanel.setLayout(new BoxLayout(serverListPanel, BoxLayout.Y_AXIS));
		
		scrollPane = new JScrollPane(serverListPanel);
		int finalWidth = (int) Math.round(WIDTH_PROPORTION_SERVER_LIST * width);
		scrollPane.setPreferredSize(new Dimension(finalWidth, height));
	    updateServerListSize(finalWidth, height);
	
		addServerButton = addButton("Add server", serverListPanel);
		add(scrollPane, BorderLayout.WEST);	
	}

    public void updateServerListSize(int width, int height) {
        int listWidth = (int) Math.round(WIDTH_PROPORTION_SERVER_LIST * width);
        scrollPane.setPreferredSize(new Dimension(listWidth, height));
        serverListPanel.setPreferredSize(new Dimension(listWidth, height));
        redrawServerPanel();
    }

	private void initialiseForm() {
		formPanel = new JPanel();
		formPanel.setLayout(new GridLayout(0, 1, 10, 10));
		
		hostField = addField("Host", formPanel);	
		portField = addField("Port", formPanel);
		nicknameField = addField("Nickname", formPanel);

		commitButton = addButton("Add server", formPanel);

		connectionResultLabel = new JLabel("");
		formPanel.add(connectionResultLabel);

		add(formPanel, BorderLayout.CENTER);		
	}

	private JTextField addField(String label, JPanel panel) {
		panel.add(new JLabel(label));
		JTextField newField = new JTextField();
		panel.add(newField);
		return newField;
	}

	private JButton addButton(String label, JPanel panel) {
		JButton newButton = new JButton(label);
		panel.add(newButton);
		return newButton;
	}

	

	//Public methods for connection managing
	public void setupServerForm() {
		setupServerForm("", "", "", true);	
	}
		
	public void setupServerForm(String host, int port, String nickname) {
		String portString = Integer.toString(port);
		setupServerForm(host, portString, nickname, false);	
	}

	public void setupServerForm(String host, String port, String nickname, boolean addingServer) {
		hostField.setText(host);
		portField.setText(port);
		nicknameField.setText(nickname);
		
		setConnectionResult("");	
		
		if(addingServer) {	
		    addingServerForm();	
        } else {
            editingServerForm();
		}

	}

    private void addingServerForm() {
        setCommitButton(true);
        
        hostField.setEnabled(true);
        portField.setEnabled(true);
        nicknameField.setEnabled(true);
    }

    private void editingServerForm() {
        setCommitButton(false);
        
        hostField.setEnabled(false);
        portField.setEnabled(false);
        nicknameField.setEnabled(false);
    }

    private void setCommitButton(boolean addServer) {
        if (addServer) {
            commitButton.setText("Add server");
        } else {
            commitButton.setText("Remove server");
        } 
    }

	public void setConnectionResult(String result) {
		connectionResultLabel.setText(result);
	}
	
	//Adding, removing and updating the server list
	public void addServer(String serverString) {
		addServer(serverString, true);
	}	

	public void addServer(String serverKey, boolean refresh) {
		if(serverButtons.containsKey(serverKey)) {
			return;
		}
	
		JButton serverValue = new JButton(serverKey);
		serverButtons.put(serverKey, serverValue);
		serverListPanel.add(serverValue);

		if(refresh == true) {
			redrawServerPanel();	
		}
	}
	
	public void removeServer(String serverKey) {
		removeServer(serverKey, true);
	}

	public void removeServer(String serverKey, boolean refresh) {
		JButton buttonToRemove = serverButtons.get(serverKey);
	
		serverListPanel.remove(buttonToRemove);
		serverButtons.remove(serverKey);
	
		if(refresh == true) {	
			redrawServerPanel();
		}
	}
	
	public void changeActive(String oldActiveServerKey, String newServerKey) {
        JButton oldActiveButton = serverButtons.get(oldActiveServerKey);
        oldActiveButton.setBorder(BorderFactory.createEmptyBorder());
		
		setActive(newServerKey);	
	}
	
	public void setActive(String activeServerKey) {
		JButton activeButton = serverButtons.get(activeServerKey);
		activeButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
		redrawServerPanel();
	}

	private void redrawServerPanel() {
		serverListPanel.revalidate();
		serverListPanel.repaint();
	}

	//Getters
	public String getHost() {
		return hostField.getText(); 
	}

	public String getPort() {
		return portField.getText(); 
	}

	public String getNickname() {
		return nicknameField.getText(); 
	}

	//Listeners
	public void viewNewServerListener(ActionListener listener) {
		addServerButton.addActionListener(listener);
	}
	
	public void commitNewServerListener(ActionListener listener) {
        removeCommitButtonListeners(); 
        commitButton.addActionListener(listener);
	}
	
	public void viewExistingServerListener(String serverString, ActionListener listener) {
		JButton selectedButton = serverButtons.get(serverString);
		selectedButton.addActionListener(listener);	
	}

	public void removeExistingServerListener(ActionListener listener) {
        removeCommitButtonListeners(); 
		commitButton.addActionListener(listener);
	}

    private void removeCommitButtonListeners() {
        for (ActionListener listener : commitButton.getActionListeners()) {
            commitButton.removeActionListener(listener);
        }
    }
}
