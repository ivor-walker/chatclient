import javax.swing.*;
import java.awt.*;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class ClientView extends JFrame {
    private ServerPanel serverPanel;
    private TargetPanel targetPanel;
    private MessagePanel messagePanel;
    
    private static final double SERVER_WIDTH = 0.2;
    private static final double TARGET_WIDTH = 0.3;
    private static final double MESSAGE_WIDTH = 0.5;
    
    public ClientView(int width, int height) {
        setSize(width, height);
        
        int serverWidth = (int) Math.round(SERVER_WIDTH * width);
        serverPanel = new ServerPanel(serverWidth, height);
        
        int targetWidth = (int) Math.round(TARGET_WIDTH * width);
        targetPanel = new TargetPanel(targetWidth, height);
        
        int messageWidth = (int) Math.round(MESSAGE_WIDTH * width);
        messagePanel = new MessagePanel(messageWidth, height); 
       
        initMainFrame();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updatePanelSizes();
            }
        });
    }

    private void initMainFrame() {
        setTitle("Chat client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       
        setLayout(new BorderLayout()); 
        add(serverPanel, BorderLayout.WEST);
        add(targetPanel, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.EAST);
        
        setVisible(true);
    }
    
    private void updatePanelSizes() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        System.out.println(currentWidth + ", " + currentHeight);

        int serverWidth = (int) Math.round(SERVER_WIDTH * currentWidth);
        serverPanel.setPreferredSize(new Dimension(serverWidth, currentHeight));
        serverPanel.updateServerListSize(serverWidth, currentHeight); 
        System.out.println(serverWidth); 
        
        int targetWidth = (int) Math.round(TARGET_WIDTH * currentWidth);
        targetPanel.setPreferredSize(new Dimension(targetWidth, currentHeight));
        System.out.println(targetWidth); 
        
        int messageWidth = (int) Math.round(MESSAGE_WIDTH * currentWidth);
        messagePanel.setPreferredSize(new Dimension(messageWidth, currentHeight));
        System.out.println(messageWidth); 
        
        refreshFrame();
    }
    
    private void refreshFrame() {
        revalidate();
        repaint();
    }

    public ServerPanel getServerPanel() {
        return serverPanel;
    }
    
    public TargetPanel getTargetPanel() {
        return targetPanel;
    }
    
    public MessagePanel getMessagePanel() {
        return messagePanel;
    }
}
