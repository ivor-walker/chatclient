import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MessagePanel extends JPanel {
    private int width;
    private int height;

    // Declare components for MessagePanel
    private JPanel messageHistoryPanel;
    private JPanel messageFormPanel;
    private JTextArea messageHistoryArea;
    private JTextArea messageInputArea;
    private JButton sendButton;
    private JButton leaveChannelButton;

    public MessagePanel(int width, int height) {
        this.width = width;
        this.height = height;
        
        // Set layout for the main panel (vertical stacking)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create and configure Message History Panel
        messageHistoryPanel = new JPanel();
        messageHistoryPanel.setLayout(new BorderLayout());
        messageHistoryPanel.setBorder(BorderFactory.createTitledBorder("Message history"));
        
        messageHistoryArea = new JTextArea(20, 30);
        messageHistoryArea.setEditable(false);  // Make it read-only
        JScrollPane messageHistoryScrollPane = new JScrollPane(messageHistoryArea);
        messageHistoryPanel.add(messageHistoryScrollPane, BorderLayout.CENTER);
        
        // Create a panel to hold the connection status and the leave button
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
        
        // Connection status label
        
                
        // Add the status panel below the message history
        messageHistoryPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Create and configure Message Form Panel
        messageFormPanel = new JPanel();
        messageFormPanel.setLayout(new BorderLayout());
        messageFormPanel.setBorder(BorderFactory.createTitledBorder("Send new message"));

        messageInputArea = new JTextArea(5, 30);
        messageInputArea.setWrapStyleWord(true);
        messageInputArea.setLineWrap(true);
        JScrollPane messageInputScrollPane = new JScrollPane(messageInputArea);
        messageFormPanel.add(messageInputScrollPane, BorderLayout.CENTER);
        
        // Create the Send button
        sendButton = new JButton("Send");
        messageFormPanel.add(sendButton, BorderLayout.SOUTH);

        // Add the two panels to the main panel
        add(messageHistoryPanel);
        add(messageFormPanel);

        // Set preferred size for the MessagePanel
        setPreferredSize(new Dimension(width, height));
    }

    // Method to set up an action listener for the Send button
    public void addSendButtonListener(ActionListener listener) {
        sendButton.addActionListener(listener);
    }

    // Method to append message to the message history
    public void appendMessageToHistory(String message) {
        messageHistoryArea.append(message + "\n");
    }

    // Method to clear the input area after message is sent
    public void clearMessageInput() {
        messageInputArea.setText("");
    }

    // Method to get the current input message
    public String getMessageInput() {
        return messageInputArea.getText();
    }

    public void channelSetup(boolean isTargetChannel) {
        if(isTargetChannel) {
            leaveChannelButton.setVisible(true); 
        } else {
            leaveChannelButton.setVisible(false);
        }
    }
}

