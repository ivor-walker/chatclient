import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

import java.util.HashMap;

import java.util.Random;

public class ServerPanel extends JPanel {
	private int width;
	private int height;

	private JPanel optionsPanel;	
	private JButton addServerButton;

	private JPanel serverListPanel;
    private JPanel serverList;	
    private JScrollPane serverListScrollPane;	
    private HashMap<String, JButton> serverButtons = new HashMap<String, JButton>(); 

	private JPanel formPanel;
	private JTextField hostField;
	private JTextField portField;
	private JTextField nicknameField;
	private JButton commitButton;
	private JLabel connectionResultLabel;

	public ServerPanel(int width, int height) {
        this.width = width;
        this.height = height;

        setPreferredSize(new Dimension(width, height));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		initialiseServerList();
		initialiseForm();

        redrawServerPanel();
	}
    
	private static final double WIDTH_PROPORTION_SERVER_LIST = 1;	
	private static final double HEIGHT_PROPORTION_SERVER_LIST = 0.5;	

	private void initialiseServerList() {
        // Main panel for the server list
        serverListPanel = new JPanel();
        serverListPanel.setLayout(new BorderLayout());
        serverListPanel.setBorder(BorderFactory.createTitledBorder("Server list"));
  
        // Create a sub-panel for the scrollable content
        serverList = new JPanel();
        serverList.setLayout(new BoxLayout(serverList, BoxLayout.Y_AXIS));         
        serverList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    
        // Wrap the scrollable content panel in a JScrollPane
        serverListScrollPane = new JScrollPane(serverList);
        int finalWidth = (int) Math.round(WIDTH_PROPORTION_SERVER_LIST * width);
        int finalHeight = (int) Math.round(HEIGHT_PROPORTION_SERVER_LIST * height);
        serverListScrollPane.setPreferredSize(new Dimension(finalWidth, finalHeight));
    
        // Add the serverListScrollPane to the center of the serverListPanel
        serverListPanel.add(serverListScrollPane, BorderLayout.CENTER);
    
        // Add the "Add server" button to the bottom of the serverListPanel
        addServerButton = new JButton("Add server");
        serverListPanel.add(addServerButton, BorderLayout.SOUTH);
    
        // Add the serverListPanel to the main container
        add(serverListPanel, BorderLayout.NORTH);
    }


    public void updateServerListSize(int width, int height) {
        int listWidth = (int) Math.round(WIDTH_PROPORTION_SERVER_LIST * width);
        serverListScrollPane.setPreferredSize(new Dimension(listWidth, height));
        serverListPanel.setPreferredSize(new Dimension(listWidth, height));
        redrawServerPanel();
    }

    private void initialiseForm() {
        formPanel = new JPanel(new GridBagLayout());
    	formPanel.setBorder(BorderFactory.createTitledBorder("New server"));
    
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
    
        // Host Field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Host"), gbc);
        gbc.gridy = 1; // Field position
        hostField = new JTextField();
        hostField.setText("mct25.teaching.cs.st-andrews.ac.uk");
        hostField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(hostField, gbc);
    
        // Port Field
        gbc.gridx = 0; gbc.gridy = 2;         
        formPanel.add(new JLabel("Port"), gbc);
        gbc.gridy = 3;        
        portField = new JTextField();
        portField.setText("21801");
        portField.setPreferredSize(new Dimension(200, 25));
        formPanel.add(portField, gbc);
        
     
        // Nickname Field
        gbc.gridx = 0; gbc.gridy = 4; 
        formPanel.add(new JLabel("Nickname"), gbc);
        gbc.gridy = 5; 
        nicknameField = new JTextField();
        nicknameField.setText(generateNewName());
        nicknameField.setPreferredSize(new Dimension(200, 25)); 
        formPanel.add(nicknameField, gbc);
    
        // Add Server Button
        gbc.gridx = 0; gbc.gridy = 6;
        commitButton = new JButton("Add server");
        formPanel.add(commitButton, gbc);
    
        // Connection Result Label
        gbc.gridx = 0; gbc.gridy = 7;
        connectionResultLabel = new JLabel("");
        formPanel.add(connectionResultLabel, gbc);
    
        add(formPanel);
    }

    private String generateNewName() {
        Random rand = new Random();
        int randomNumber = rand.nextInt(9000) + 1000;
        return "Test" + String.valueOf(randomNumber);
    }

	private JTextField addField(String label, JPanel panel) {
		panel.add(new JLabel(label));
		JTextField newField = new JTextField();
		panel.add(newField);
		return newField;
	}

	private JButton addButton(String label, JPanel panel, String direction) {
		JButton newButton = new JButton(label);
		panel.add(newButton, direction);
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
		connectionResultLabel.setText("<html>" + result + "</html>");
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
		serverList.add(serverValue);
		serverButtons.put(serverKey, serverValue);

		if(refresh == true) {
			redrawServerPanel();	
		}
	}
	
	public void removeServer(String serverKey) {
		removeServer(serverKey, true);
	}

	public void removeServer(String serverKey, boolean refresh) {
		JButton buttonToRemove = serverButtons.get(serverKey);
	
		serverList.remove(buttonToRemove);
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
