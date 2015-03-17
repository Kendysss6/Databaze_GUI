package app;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import sConect.CreateAppAndConnection;
import sablony.errorwin.ExceptionWin;

import javax.swing.JPasswordField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.Toolkit;

import javax.swing.JTextField;

/**
 * Licence:
 * Super csv Apache License Version 2.0,
 * Jcalendar GNU Lesser General Public License http://www.gnu.org/licenses/lgpl.html
 * mysql GNU GPL v2
 * Opencsv Apache License V2.0
 * POI Apache License, Version 2.0 (current)
 * java n�jaka license? :D
 * @author Ond�ej Havl��ek
 *
 */
public class LoginWindow extends JFrame implements KeyListener {
	
	/**
	 * Verze
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Ip adresa po��ta�e, na kter�m se vyskytuje server, na kter� se budeme p�ipojovat.
	 */
	//private static final String ipServeru = "10.190.33.1";
	private static final String ipServeru = "localhost";
	private static final String url = "jdbc:mysql://"+ipServeru+":3306/";
	/**
	 * JButton prihla�en�
	 */
	private JButton prihlasButton;
	
	private JPanel contentPane;
	private JPasswordField passwordField;
	/**
	 * JTextField, do kter�ho se zapisuje u�ivatelsk� jm�no
	 */
	private JTextField account;
	/**
	 * JFrame, kter� kter� ukazuje na tuto t��du. Pou�ijeme jej v anonymn� t��de (add action listener)
	 */
	private JFrame okno = this;
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try { 
					UIManager.setLookAndFeel("com.jgoodies.looks.windows.WindowsLookAndFeel");
			    } 
			    catch(Exception e){
			    	ExceptionWin.showExceptionMessage(e);
			    }
				try {
					LoginWindow frame = new LoginWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					ExceptionWin.showExceptionMessage(e);
				}
			}
		});
	}
	
	/**
	 * Vytvo�� p�ihla�ovac� okno
	 */
	public LoginWindow() {		
		setIconImage(Toolkit.getDefaultToolkit().getImage(LoginWindow.class.getResource("/app/dbSlevarnaIco.png")));
		setMinimumSize(new Dimension(350, 310));
		setTitle("P\u0159ihl\u00E1\u0161en\u00ED do datab\u00E1ze");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 352, 352);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel lblNewLabel = new JLabel("Sl\u00E9v\u00E1rna Stra\u0161ice");
		lblNewLabel.setForeground(new Color(37,37,37));
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		
		JLabel lblPihlenDoAdministrace = new JLabel("p\u0159ihl\u00E1\u0161en\u00ED do administrace");
		lblPihlenDoAdministrace.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblPihlenDoAdministrace.setForeground(new Color(88, 88, 87));
		
		JLabel lblNewLabel_1 = new JLabel("Jm\u00E9no:");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		JLabel lblHeslo = new JLabel("Heslo:");
		lblHeslo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		
		prihlasButton = new JButton("P\u0159ihl\u00E1sit se");
		prihlasButton.setActionCommand("Prihlas se");
		prihlasButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {						          
				if(arg0.getActionCommand().equalsIgnoreCase("Prihlas se")){
					char [] heslo =	passwordField.getPassword();
					passwordField.setText("");
					String jmeno = account.getText();
					if(jmeno.equalsIgnoreCase("")){
						JOptionPane.showMessageDialog(okno, "U�ivatelsk� jm�no je pr�zdn�");
						return;
					}
					if(heslo.length < 1){
						JOptionPane.showMessageDialog(okno, "Heslo je pr�zdn�");
						return;
					}
				
					ProgresBarFrame barFrame = new ProgresBarFrame();
					barFrame.setVisible(true);
					//
					CreateAppAndConnection over = new CreateAppAndConnection(okno, barFrame, url);
					over.executeCreateApp(jmeno, heslo);
				}
			}
		});
		
		
		
		account = new JTextField();
		account.addKeyListener(this);
		
		passwordField = new JPasswordField();
		passwordField.addKeyListener(this);
		
		JLabel lblIpAdresaServeru = new JLabel("IP adresa serveru: "+ipServeru);
		
		JLabel lblOndejHavlek = new JLabel("Vytvo\u0159il: Ond\u0159ej Havl\u00ED\u010Dek");
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addGap(42)
							.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING, false)
								.addComponent(lblNewLabel)
								.addComponent(lblPihlenDoAdministrace)
								.addComponent(prihlasButton)
								.addGroup(gl_contentPane.createSequentialGroup()
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(lblHeslo)
										.addComponent(lblNewLabel_1))
									.addGap(35)
									.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
										.addComponent(account, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE)
										.addComponent(passwordField, GroupLayout.DEFAULT_SIZE, 86, Short.MAX_VALUE)))))
						.addComponent(lblIpAdresaServeru))
					.addContainerGap(70, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
					.addContainerGap(234, Short.MAX_VALUE)
					.addComponent(lblOndejHavlek)
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addComponent(lblIpAdresaServeru)
					.addGap(25)
					.addComponent(lblNewLabel)
					.addGap(5)
					.addComponent(lblPihlenDoAdministrace)
					.addGap(48)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel_1)
						.addComponent(account, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(12)
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblHeslo)
						.addComponent(passwordField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(28)
					.addComponent(prihlasButton)
					.addGap(43)
					.addComponent(lblOndejHavlek)
					.addContainerGap())
		);
		contentPane.setLayout(gl_contentPane);
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 10) {
			for(ActionListener a: this.prihlasButton.getActionListeners()) {
			    a.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Prihlas se"));
			}
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
}
