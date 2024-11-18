public class ServerView extends JFrame {
	//private HashMap<String, JButton> serverButtons = new HashMap<String, JButton>();
	
	private JButton addServerButton;
	private JButton editConnectionButton;

	private JPanel serverListPanel;

	private JPanel formPanel;
	private JTextField hostField;
	private JTextField portField;
	private JTextField nicknameField;
	private JButton commitButton;
	
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

		commitButton = new JButton("[Add/update]");
		formPanel.add(commitButton);

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

	private void toggleFormVisibility(boolean isVisible) {
		formPanel.setVisible(isVisible);
		revalidate();
		repaint();
	}

	//Connection managing
	/
	public void setupServerForm() {
		commitButton.setText("Add server");

		toggleFormVisibility(true);	
	}
		

	public void setupServerForm(String host, int port, String nickname) {
		commitButton.setText("Update server");
		hostField.setText(host);
		portField.setText(port);
		nicknameField.setText(nickname);
		
		toggleFormVisiblity(true);
	}

	public void setConnectionResult(String result) {
		connectionResultLabel.setText(result);
	}

	public void addServer(String host, int port) {
		//TODO creates server button
	}

	public void updateServer(host, port) {

	}

	public void updateNewServerListener(ActionListener listener) {

	}

	public void updateExistingServerListener(String host, int port, ActionListener listener) {
		//TODO update specified button
	}

	public void commitNewServerListener(ActionListener listener) {

	}

	public void commitExistingServerListener(ActionListener listener) {

	}

	public String formatServerString(String host, int port) {
		return host + ":" + port;
	}
}
