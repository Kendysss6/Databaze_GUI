package sConect;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import app.LoginWindow;
import app.MainFrame;
import app.ProgresBarFrame;
import sablony.errorwin.ExceptionWin;
import thread.ObnovStrukturuDB;
import thread.ScriptRunner;

/**
 * T��da pro vytv��en� spojen� mezi datab�z�. Pomoc� t�to t��dy z�sk�me t��du java.sql.Connection pro komnikaci s datab�z�
 * @author Ond�ej Havl��ek
 *
 */
public class CreateConectionToMySQL implements ActionListener {
	private final String url;
	private final String driverMysql = "com.mysql.jdbc.Driver";
	private final String driverMariaDB = "org.mariadb.jdbc.Driver";
	private final String spatneHeslo= "Access denied for user";
	private final String neniPripojeni= "Communications link failure";
	private static final String acesDenied = "execute command denied to user";
	private JFrame loginWindow;
	private ProgresBarFrame prgbarFrame;
	private Task t;
	private static final String cancelZprava = "java.util.concurrent.CancellationException";
	
	private int returnVal = -100;
	
	/**
	 * Konstruktor, vypln� se pouze �daje pot�ebn� k p�ipojen�.
	 * @param okno JFrame, na kter� by se p��padn� zobrazil dialog s vyvolanou vyjimkou.
	 * @param progresBar Progressbar na kter� budeme zobrazovat v�voj p�ipojov�n�
	 * @param url kompletn� IP adresa serveru
	 */
	public CreateConectionToMySQL(JFrame okno, ProgresBarFrame progresBar, String url){
		this.url = url;
		this.loginWindow = okno;
		prgbarFrame = progresBar;
		prgbarFrame.addListener(this);
	}
	
	/**
	 * Metoda vracej�c� odkaz na vytvo�enou aplikaci. Spu�t� se zde thread {@link sConect.CreateConectionToMySQL.Task Task}
	 * @param userName u�ivatelsk� jm�no pro p�ipojen� do datab�ze
	 * @param pass heslo v char [] field.
	 * @return
	 */
	public void executeVytvor(String userName, char [] pass){
		t = new Task(userName, pass);
		t.execute();
	}
	
	
	/**
	 * <p>T��da roz�i�uj�c� <a href="https://docs.oracle.com/javase/8/docs/api/javax/swing/SwingWorker.html">SwingWorker</a>,
	 *  kter� vytv��� celou aplikaci v pozad�, zat�mco zobrazuje stav vytv��en� pomoc� {@link app.ProgresBarFrame}</p>
	 * <p>Trochu zbyte�n� t��da, proto�e u� tuhle t��du vytv��im ve <a href="https://docs.oracle.com/javase/8/docs/api/javax/swing/SwingWorker.html">SwingWorker</a>,
	 * ale tak nic se nestane. Bylo by moc pr�ce to zase p�episovat a d�vat do jednoho.</p>
	 * @author Ond�ej Havl��ek
	 */
	private class Task extends SwingWorker<MainFrame, Void> {
        /*
         * Main task. Executed in background thread.
         */
		private String userName;
		private char [] pass;
		private Task(String jmeno, char [] pass){
			prgbarFrame.setVisible(true);
			this.pass = pass;
			this.userName = jmeno;
		}
		
        @Override
        public MainFrame doInBackground() {  
            prgbarFrame.setPripojuji();
        	// pripojuju
        	String password;
        	Connection conn = null;
        	try{
        		password = new String(pass);
        		Arrays.fill(pass,'0');
        		Class.forName(driverMysql).newInstance();
        		conn = DriverManager.getConnection(url,userName,password);
        		password = " ";
        		password = null;
        	} catch(Exception e){
        		if(e.getMessage().contains(spatneHeslo)){
        			prgbarFrame.setVisible(false);
        			if(!isCancelled()){
        				JOptionPane.showMessageDialog(loginWindow, "\u0160patn\u00E9 heslo nebo u�ivatelsk� jm�no");
        			}
				}
				else if(e.getMessage().startsWith(neniPripojeni)) {
					prgbarFrame.setVisible(false);
					if(!isCancelled()){
						JOptionPane.showMessageDialog(loginWindow, "Nen� zapnut� server (nebo �patn� IP adresa serveru)");
					}
				}
				else if(e.getClass().getCanonicalName().equals("java.sql.SQLNonTransientConnectionException")) {
					prgbarFrame.setVisible(false);
					if(!isCancelled()){
						JOptionPane.showMessageDialog(loginWindow, "Nen� zapnut� server (nebo �patn� IP adresa serveru), nep�ipojeno");
					}
				}
				else {
					prgbarFrame.setVisible(false);
					ExceptionWin.showExceptionMessage(e);
				}
        		if(conn != null){
        			try {
						conn.close();
					} catch (SQLException e1) {
						ExceptionWin.showExceptionMessage(e1);
					}
        			conn = null;
        		}
        		return null;
        	} finally{
        		password = " ";
        		password = null;
        		System.gc();
        	}
        	//vytvarim aplikaci
        	prgbarFrame.setVytvarimAplikaci();
        	
        	MainFrame hlavniOkno = null;
        	try{
        		if(isNewestVersionGUI(conn.prepareCall("{CALL pomdb.verze()}").executeQuery())){
        			hlavniOkno = new MainFrame(conn, userName, prgbarFrame.getApProgresBar());
        		} else {
        			prgbarFrame.setVisible(false);
        			JOptionPane.showMessageDialog(loginWindow, "Nem�te nejnov�j�� verzi aplikace Datab�ze Stra�ice, nelze spustit.");
        		}
        	} catch (SQLException e){
        		prgbarFrame.setVisible(false);
        		prgbarFrame.setZalohaDB();
        		if(e.getMessage().startsWith(acesDenied)){        			
        			JOptionPane.showMessageDialog(loginWindow, "Chyb� v�m pravomoce. Kontaktuje pros�m administr�tora.");
				} else {
					//ExceptionWin.showExceptionMessage(e);
					e.printStackTrace();
					JOptionPane.showMessageDialog(loginWindow, "Objevila se chyba p�i vytv��en� GUI, CreateConection -doInBackground(), pokud chcete obnovit datab�zi vyberte soubor strukturaDB.sql");
					File curDirectory = new File("./");
					File obnovDBSqlFile = null;
					JFileChooser chooser = new JFileChooser(curDirectory);
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Soubor SQL", "sql");
					chooser.setFileFilter(filter);
					chooser.setDialogTitle("Vyberte soubor strukturaDB.sql");
					returnVal = chooser.showOpenDialog(loginWindow);
					if(returnVal == JFileChooser.APPROVE_OPTION) {
						prgbarFrame.setVisible(true);
						obnovaStrukturyDB(conn, chooser.getSelectedFile());
					}
				}
				
        	}
        	
        	if(returnVal != 0){
        		prgbarFrame.setHotovo();
        	}
        	
        	return hlavniOkno;
        }
        private void obnovaStrukturyDB(Connection conn, File obnovDBSqlFile){
        	prgbarFrame.setVisible(true);
        	ObnovStrukturuDB obnov = new ObnovStrukturuDB(loginWindow, prgbarFrame, conn, obnovDBSqlFile);
        	obnov.execute();
    	}
       

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
        	if(returnVal != 0 ){
        		prgbarFrame.setVisible(false);
        		prgbarFrame.dispose();
        	}
        	
        	
        	MainFrame okno = null;
        	try {
    			okno = t.get();
    		} catch (Exception e) {			
    			if (cancelZprava.startsWith(e.toString())) {
    				JOptionPane.showMessageDialog(loginWindow, "P�ipojov�n� p�eru�eno");
    			} else {
    				ExceptionWin.showExceptionMessage(e);
    			}
    			
    		}
        	if(okno != null){
        		okno.setVisible(true);
        		loginWindow.setVisible(false);
        		loginWindow.dispose();
        	}
        }
    }
	/**
	 * Obsluha pro {@code CancelButton} v {@link app.ProgresBarFrame}
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equalsIgnoreCase("Cancel")){
			boolean pom = t.cancel(true);
			System.out.println("preruseno "+pom );
			if(!pom){
				JOptionPane.showMessageDialog(loginWindow, "Aplikace je ji� vyto�ena");
			}
		}
		
	}
	
	private boolean isNewestVersionGUI(ResultSet rs) throws SQLException{
		if(rs.first()){
			String verze = rs.getString(1);
			String appVer = LoginWindow.verzeGUI;
			// kontrolujeme prvni t�i pismena
			for(int i = 0; i < 3; i++){
				if (verze.charAt(i) != appVer.charAt(i)){
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
