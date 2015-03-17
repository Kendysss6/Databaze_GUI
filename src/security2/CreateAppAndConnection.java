package security2;
import javax.swing.JFrame;

import app.MainFrame;
import app.ProgresBarFrame;
import security.conect.CreateConectionToMySQL;

public class CreateAppAndConnection {
	private CreateConectionToMySQL connToMysql;
	private MainFrame hlavniOkno = null;
	
	public CreateAppAndConnection(JFrame okno, ProgresBarFrame mujBar, String url){
		connToMysql = new CreateConectionToMySQL(okno, mujBar, url);
	}
	
	public void executeCreateApp(String jmeno, char [] heslo){
		connToMysql.executeVytvor(jmeno, heslo);
	}
}
