public class ServerView extends JFrame {
	private JButton addServerButton;
	private JButton editConnectionButton;

	private HashMap<String, JButton> serverButtons = new HashMap<String, JButton>(); 
	private JPanel serverListPanel;

	private JPanel formPanel;
	private JTextField hostField;
	private JTextField portField;
	private JTextField nicknameField;
	private JButton commitButton;
	private JButton removeButton;
	
	private JLabel connectionResultLabel;

	private int width;
	private int height;

	//Initialisations
	public ServerView(int width, int height) {
		this.width = width;
		this.height = height;

		initialiseUI();
	}

	private void initialiseUI() {
		initialiseMainFrame();
		initialiseServerList();
		initialiseForm();
		
		toggleMainFrameVisibility(true);
	}

	private void toggleMainFrameVisibility(boolean mainFrameVisibility) {
		setVisibility(mainFrameVisibility);
		setFormVisibility(false);	
	}	

	private void initialiseMainFrame() {
		setTitle("Add/edit connections");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(width, height);
		setLayout(new BorderLayout());
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
		formPanel.setLayout(new GridLayout(5, 2, 10, 10));
		
		hostField = addNewField("Host");	
		portField = addNewField("Port");
		nicknameField = addNewField("Nickname");

		commitButton = addNewButton("[Add|Update]");
		removeButton = addNewButton("Delete server");
 
		connectionResultLabel = new JLabel("[connectionResult]");
		formPanel.add(connectionResultLabel);

		add(formPanel, BorderLayout.CENTER);		
	}

	private JTextField addNewField(String label) {
		formPanel.add(new JLabel(label));
		JTextField newField = new JTextField();
		formPanel.add(newField);
		return newField;
	}

	private JTextField addNewButton(String label) {
		JButton newButton = new JButton(label);
		formPanel.add(newField);
		return newButton;
	}
	private void toggleFormVisibility(boolean isVisible) {
		formPanel.setVisible(isVisible);
		revalidate();
		repaint();
	}

	//Public methods for connection managing
	public void setupServerForm() {
		commitButton.setText("Add server");

		toggleFormVisibility(true);	
	}
		
	private string updateTarget;
	public void setupServerForm(String host, String port, String nickname) {
		commitButton.setText("Update server");
		hostField.setText(host);
		portField.setText(port);
		nicknameField.setText(nickname);
		
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
