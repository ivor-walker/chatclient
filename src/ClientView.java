import javax.swing.*;
import java.awt.*;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * The main frame of the client application
 * Contains server, target and message panels
 */
public class ClientView extends JFrame {
    private ServerPanel serverPanel;
    private TargetPanel targetPanel;
    private MessagePanel messagePanel;
  
    // Proportions of each panel in the frame  
    private static final double SERVER_WIDTH = 0.2;
    private static final double TARGET_WIDTH = 0.3;
    private static final double MESSAGE_WIDTH = 1 - SERVER_WIDTH - TARGET_WIDTH;
   
    /**
     * Constructor
     * @param width width of the frame 
     * @param height height of the frame
     */
    public ClientView(int width, int height) {
        // Set the size of the mainframe
        setSize(width, height);
       
        // Set the proportional width of each panel and create them
        int serverWidth = (int) Math.round(SERVER_WIDTH * width);
        serverPanel = new ServerPanel(serverWidth, height);
        
        int targetWidth = (int) Math.round(TARGET_WIDTH * width);
        targetPanel = new TargetPanel(targetWidth, height);
        
        int messageWidth = (int) Math.round(MESSAGE_WIDTH * width);
        messagePanel = new MessagePanel(messageWidth, height); 
      
        // Initialise the main frame 
        initMainFrame();

        // Add a listener to update the panel sizes when the frame is resized
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updatePanelSizes();
            }
        });
    }

    /**
     * Initialise the main frame
     */
    private void initMainFrame() {
        // Set mainframe title 
        setTitle("Chat client");

        // When the mainframe is closed, the application will exit
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
        // Set the layout of the mainframe 
        setLayout(new BorderLayout()); 
        add(serverPanel, BorderLayout.WEST);
        add(targetPanel, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.EAST);
       
        // Set the mainframe to be visible 
        setVisible(true);
    }
   
    /**
     * Update sizes of panels when the frame is resized
     */ 
    private void updatePanelSizes() {
        // Get new dimensions of the frame
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        // Update the size of each panel based on the new dimensions and proportions
        int serverWidth = (int) Math.round(SERVER_WIDTH * currentWidth);
        serverPanel.setPreferredSize(new Dimension(serverWidth, currentHeight));
        // TODO why is there a special method for this?
        serverPanel.updateServerListSize(serverWidth, currentHeight); 
        
        int targetWidth = (int) Math.round(TARGET_WIDTH * currentWidth);
        targetPanel.setPreferredSize(new Dimension(targetWidth, currentHeight));
        
        int messageWidth = (int) Math.round(MESSAGE_WIDTH * currentWidth);
        messagePanel.setPreferredSize(new Dimension(messageWidth, currentHeight));
       
        // Refresh the frame to apply the changes
        refreshFrame();
    }
   
    /**
     * Refresh the frame
     * Combine revalidate and repaint methods needed for updating the frame
     */ 
    private void refreshFrame() {
        revalidate();
        repaint();
    }

    /**
     * Getter for the server panel
     * @return the server panel
     */
    public ServerPanel getServerPanel() {
        return serverPanel;
    }
   
    /**
     * Getter for the target panel 
     * @return the target panel
     */
    public TargetPanel getTargetPanel() {
        return targetPanel;
    }
   
    /**
     * Getter for the message panel 
     * @return the message panel
     */
    public MessagePanel getMessagePanel() {
        return messagePanel;
    }
}
