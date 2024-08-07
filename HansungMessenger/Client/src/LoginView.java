import java.awt.EventQueue;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import java.awt.Canvas;
import java.awt.Label;
import javax.swing.JTextPane;
import javax.swing.JTabbedPane;
import javax.swing.JPasswordField;
import javax.swing.JFormattedTextField;

public class LoginView extends JFrame{

	private JTextField txtHansungnavercom;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginView frame = new LoginView();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LoginView() {
		setBounds(100, 100, 389, 509);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(new Color(250, 225, 0));
		panel.setBounds(0, 0, 375, 472);
		getContentPane().add(panel);
		panel.setLayout(null);
		
		LoginAction login = new LoginAction();
		
		txtHansungnavercom = new JTextField("");
		txtHansungnavercom.setForeground(UIManager.getColor("Button.foreground"));
		txtHansungnavercom.setBounds(102, 237, 170, 28);
		txtHansungnavercom.addActionListener(login);
		panel.add(txtHansungnavercom);
		txtHansungnavercom.setColumns(10);
		
		JButton btnNewButton = new JButton("로그인");
		btnNewButton.addActionListener(login);
		btnNewButton.setBounds(102, 301, 170, 28);
		panel.add(btnNewButton);
			
		JLabel lblNewLabel = new JLabel("닉네임");
		lblNewLabel.setBounds(39, 243, 50, 15);
		panel.add(lblNewLabel);
		

		
		JLabel lblNewLabel_2 = new JLabel("");
		ImageIcon icon = new ImageIcon("img/Talk.png");
		Image img = icon.getImage();
		Image updateImg = img.getScaledInstance(165, 160, Image.SCALE_SMOOTH);
		ImageIcon updateIcon = new ImageIcon(updateImg);
		lblNewLabel_2.setIcon(updateIcon);	
		lblNewLabel_2.setBounds(102, 79, 170, 148);
		panel.add(lblNewLabel_2);
	}

	class LoginAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			String id = txtHansungnavercom.getText().trim();
	
			StartingScreen screen = new StartingScreen(id, "127.0.0.1", "30000");
			setVisible(false);
			
		}
	}
}