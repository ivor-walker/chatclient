import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionListener;

import java.util.HashMap;


public class MessagePanel extends JPanel {
    private int width;
    private int height;
    
    public MessagePanel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
    }
}
