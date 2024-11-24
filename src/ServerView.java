import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class ServerView extends JFrame {
	private int width;
	private int height;

	private JPanel optionsPanel;	
	private JButton addServerButton;
	private JButton editConnectionButton;

	private JPanel serverListPanel;
	private HashMap<String, JButton> serverButtons = new HashMap<String, JButton>(); 

	private JPanel formPanel;
	private JTextField hostField;
	private JTextField portField;
	private JTextField nicknameField;
	private JButton commitButton;
	private JButton removeButton;
	private JLabel connectionResultLabel;

	//Initialisations
	public ServerView(int width, int height) {
		this.width = width;
		this.height = height;

		initialiseUI();
	}

	private void initialiseUI() {
		initialiseMainFrame();
		initialiseOptions();
		initialiseServerList();
		initialiseForm();
		
		toggleMainFrameVisibility(true);
	}

	private void toggleMainFrameVisibility(boolean mainFrameVisibility) {
		setvisible(mainFrameVisibility);
		setFormVisibility(false);	
	}	

	private void initialiseMainFrame() {
		setTitle("Manage connections");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);
		setLayout(new BorderLayout());
	}

	private void initialiseOptions() {
		optionsPanel = new JPanel();
		
		addServerButton = addButton("Add new server", optionsPanel);
		editConnectionButton = addButton("Edit connection", optionsPanel);
		
		add(optionsPanel, BorderLayout.NORTH);
	}

	private static final double WIDTH_PROPORTION_SERVER_LIST = 0.2;	
	private void initialiseServerList() {
		serverListPanel = new JPanel();
		serverListPanel.setLayout(new BoxLayout(serverListPanel, BoxLayout.Y_AXIS));
		
		JScrollPane scrollPane = new JScrollPane(serverListPanel);
		int finalWidth = (int) (WIDTH_PROPORTION_SERVER_LIST * width);
		scrollPane.setPreferredSize(new Dimension(finalWidth, height));
		
		add(scrollPane, BorderLayout.WEST);	
	}

	private void initialiseForm() {
		formPanel = new JPanel();
		formPanel.setLayout(new GridLayout(0, 1, 10, 10));
		
		hostField = addField("Host", formPanel);	
		portField = addField("Port", formPanel);
		nicknameField = addField("Nickname", formPanel);

		commitButton = addButton("[Add|Update]", formPanel);
		removeButton = addButton("Delete server", formPanel);

		formPanel.add(new JLabel("Status: ")); 
		connectionResultLabel = new JLabel("[connectionResult]", formPanel);
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

	private void toggleFormVisibility(boolean isVisible) {
		formPanel.setVisible(isVisible);
		revalidate();
		repaint();
	}

	//Public methods for connection managing
	public void setupServerForm() {
		setupServerForm("", "", "", true);	
	}
		
	public void setupServerForm(String host, String port, String nickname) {
		setupServerForm(host, port, nickname, false);	
	}

	public void setupServerForm(String host, String port, String nickname, boolean addingServer) {
		hostField.setText(host);
		portField.setText(port);
		nicknameField.setText(nickname);
		
		setConnectionResult("");	
		
		if(addingServer) {	
			commitButton.setText("Add server");
		else {
			commitButton.setText("Update server");
		}

		
		toggleFormVisiblity(true);
	}

	public void setConnectionResult(String result) {
		connectionResultLabel.setText(result);
	}

	//Adding, removing and updating the server list
	public void addServer(String serverString) {
		addServer(serverString, true);
	}	

	public void addServer(String serverKey, boolean refresh) {
		if(serverButtons.contains(serverKey)) {
			return;
		}
	
		JButton serverValue = new JButton(serverKey);
		serverButtons.put(serverKey, serverValue);
		serverListPanel.add(serverValue);

		if(refresh == true) {
			redrawServerList();	
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
			redrawServerList();
		}
	}
	
	public void updateServer(String oldServerString, String newServerString) {
		removeServer(oldServerString, false);
		addServer(newServerString);
	}
	
	public void changeActive(String oldActiveServerKey, String activeServerKey) {
		JButton oldActiveButton = serverButtons.get(oldActiveButton);
                oldActiveButton.setBorder(BorderFactory.createEmptyBorder());
		
		JButton activeButton = serverButtons.get(activeServerKey);
		activeButton.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
		redrawServerList();
	}
	
	private void redrawServerList() {
		serverListPanel.revalidate();
		serverListPanel.repaint();
	}

	public void setEditingEnabled(boolean editingEnabled) {
		if(editingEnabled) {
			editConnectionButton.setForeground(Color.BLACK);	
		} else {
			editConnectionButton.setForeground(Color.GREY);	
		}
	}

	//Getters
	public void getHost() {
		return hostField.getText(); 
	}

	public void getPort() {
		return portField.getText(); 
	}

	public void getNickname() {
		return nickname.getText(); 
	}

	//Listeners
	public void viewNewServerListener(ActionListener listener) {
		addServerButton.addActionListener(listener);
	}
	
	public void commitNewServerListener(ActionListener listener) {
		commitButton.addActionListener(listener);
	}
	
	public void viewExistingServerListener(String serverString, ActionListener listener) {
		JButton selectedButton = serverButtons.get(serverString);
		selectedButton.addActionListener(listener);	
	}

	public void commitExistingServerListener(ActionListener listener) {
		commitButton.addActionListener(listener)	
	}

	public void toggleEditingListener(ActionListener listener) {
		editConnectionButton.addActionListener(listener);	
	}
}
