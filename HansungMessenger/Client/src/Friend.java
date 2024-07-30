import java.awt.*;

import javax.swing.ImageIcon;
import javax.swing.JLabel;


public class Friend extends JLabel{
	private static final long serialVersionUID = 1L;
	private ImageIcon icon;
	private String id;
	private String statusMessage;
	private JLabel picture = new JLabel();
	private JLabel ID;
	private JLabel statusMessageLabel = new JLabel();
	
	public Friend(ImageIcon icon, String id, String statusMessage) {
		this.icon = icon;
		this.id = id;
		this.statusMessage = statusMessage;
		
		picture.setIcon(icon);
		ID = new JLabel(id);
		ID.setHorizontalAlignment(JLabel.CENTER);
		statusMessageLabel.setText(statusMessage);
		
		this.setBackground(Color.LIGHT_GRAY);
		this.setLayout(new BorderLayout(0,0));
		this.add(picture, BorderLayout.WEST);
		this.add(ID, BorderLayout.CENTER);
		this.add(statusMessageLabel, BorderLayout.EAST);
	}

	public ImageIcon getIcon() {
		return icon;
	}
	
	public String getID() {
		return id;
	}
	
	public String getStatusMessage() {
		return statusMessage;
	}
	
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
		picture.setIcon(icon);
		repaint();
	}
	
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
		statusMessageLabel.setText(statusMessage);
		repaint();
	}
}
