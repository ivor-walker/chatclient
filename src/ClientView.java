import javax.swing.*;
import java.awt.*;

public class ClientView extends JFrame {
    private int width;
    private int height;

    private ServerPanel serverPanel;
    private TargetPanel targetPanel;
    private MessagePanel messagePanel;

    public ClientView(int width, int height) {
        initMainFrame();
        serverPanel = new ServerPanel(width, height);
        targetPanel = new TargetPanel(width, height);
        messagePanel = new MessagePanel(width, height); 
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

    private void initMainFrame() {
        setTitle("Chat client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
        setLayout(new BorderLayout());
    }
}
