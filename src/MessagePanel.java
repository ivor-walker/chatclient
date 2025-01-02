import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

/**
 * Class for the message panel
 * Contains target's message history and a form to send new messages
 */
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

    // TODO seperate out history and form panel creation into seperate functions
    /**
     * Constructor for MessagePanel
     * @param width Width of the panel
     * @param height Height of the panel
     */
    public MessagePanel(int width, int height) {
        // Set the width and height of the panel
        this.width = width;
        this.height = height;
        
        // Set layout for the main panel (vertical stacking)
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Create message history panel 
        messageHistoryPanel = new JPanel();
        messageHistoryPanel.setLayout(new BorderLayout());
    
        // TODO remove magic strings, and create setters for default titles accessable by controller
        // Create border and title for the message history panel 
        messageHistoryPanel.setBorder(BorderFactory.createTitledBorder("Message history"));
        // Create a text area to display message history 
        messageHistoryArea = new JTextArea(20, 30);
        // Make message history read-only 
        messageHistoryArea.setEditable(false);
        // Add a scroll pane to the message history text area
        JScrollPane messageHistoryScrollPane = new JScrollPane(messageHistoryArea);

        // Add message history to the message history panel
        messageHistoryPanel.add(messageHistoryScrollPane, BorderLayout.CENTER);

        // TODO think statusPanel was removed, check if it is needed
        // Create a status panel
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BorderLayout());
                
        // Add the status panel below the message history
        messageHistoryPanel.add(statusPanel, BorderLayout.SOUTH);
        
        // Create a form panel to send new messages 
        messageFormPanel = new JPanel();
        messageFormPanel.setLayout(new BorderLayout());
        // Add a border and title to the message form panel 
        messageFormPanel.setBorder(BorderFactory.createTitledBorder("Send new message"));
        // Set up the input area for the message form panel
        messageInputArea = new JTextArea(5, 30);
        messageInputArea.setWrapStyleWord(true);
        messageInputArea.setLineWrap(true);
        // Add a scroll pane to the message input text area
        JScrollPane messageInputScrollPane = new JScrollPane(messageInputArea);
        // Add the message input area to the message form panel
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

    /**
     * Adds listener to the send button 
     * @param listener ActionListener to be added to the send button
     */
    public void addSendButtonListener(ActionListener listener) {
        sendButton.addActionListener(listener);
    }

    /**
     * Adds message to the message history area
     * @param message Message to be added to the message history area
     */
    public void addMessage(String message) {
        // Append the message to the message history area
        messageHistoryArea.append(message + "\n");
        
        // Update entire messagePanel
        // TODO refresh messageHistoryPanel only instead of entire panel
        refresh();
    }

    /**
     * Clears message input area (e.g after message is sent)
     */ 
    public void resetInputArea() {
        messageInputArea.setText("");
    }

    /**
     * Removes all messages from the message history area (e.g after change in active target)
     */
    public void resetMessageHistory() {
        // Clear the message input area
        resetInputArea(); 
        // Clear the message history area
        messageHistoryArea.setText(""); 
        // Refresh entire messagePanel
        refresh();
    }

    /**
     * Updates entire messagePanel by combining all the refresh methods
     */
    public void refresh() {
        revalidate();
        repaint();
    }

    /**
     * Getter for current message input in the message input area
     * @return String of contents of message input area
     */
    public String getMessageInput() {
        return messageInputArea.getText();
    }

    /**
     * Setter for message history name
     * @param targetName Name of the new target to set the message history name to
     */
    public void setHistoryName(String targetName) {
        messageHistoryPanel.setBorder(BorderFactory.createTitledBorder("Message history for: " + targetName));
    }

    // TODO channel config is in targetPanel now, investigate removing
    /**
     * Makes channel config (e.g leave channel) visible if current target is a channel
     * @param isTargetChannel Boolean to determine if current target is a channel
     */
    public void channelSetup(boolean isTargetChannel) {
        if(isTargetChannel) {
            leaveChannelButton.setVisible(true); 
        } else {
            leaveChannelButton.setVisible(false);
        }
    }
}

