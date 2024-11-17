public class ServerView {
	private HashSet<String, JButton> serverButtons;
	private HashMap<String, ActionListener> buttonListeners;



	//Connection managing
	public void setConnectionResult(String result) {

	}

	public void setupServerForm() {

	}

	public void setupServerForm(String host, int port, String nickname) {

	}

	public void addServer(host, port) {
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
