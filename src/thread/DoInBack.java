package thread;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import app.MainFrame;
import app.ProgresBarFrame;
import sablony.errorwin.ExceptionWin;
import sqlstorage.SQLStor;
import sqlstorage.TransClass;
import sqlstorage.SQLStor;
import storage.SkladOdkazu;
/**
 * Objekt pro ukl�d�n� Connection a CallableStatements pro komunikaci s datab�z�.
 * @author Havlicek
 *
 */
public class DoInBack {
	private SQLStor sql = null;
	private Connection conn = null;
	private CallableStatement [][] cst = null;
	private Date [][] naposledyPouzito = null;
	private CallableStatement c;
	private ResultSet rs = null;
	private SkladOdkazu sklad;
	private MainFrame hlavniOkno;
	private static final int dobaNaZavrenyPripojeni = 2*60000;
	private static final int maxDelkaRetezce = 30;
	private static final int maxDelkaPaganyrky = 5;
	private static final int maxDelkaPoznamkyUZakazky = 45, maxDelkaVady = 45;
	private static final int maxDelkamaterialu = 25, maxDelkaVinika = 25;
	private static final int maxDelkaVlastnihoMaterialu = 10, maxDelkaCislaTavby = 10;
	private static final int maxPocetKusuNovaZakazka = 2000;
	private ProgresBarFrame prgbar = new ProgresBarFrame();
	
	/**
	 * Sql prikazy
	 */
	private final String [][] sqlPrikazy = {
			{"{CALL pomdb.novyZakaznik(?)}", "{CALL pomdb.novyModel(?,?,?,?,?,?,?,?)}", "{CALL pomdb.novaZakazka(?,?,?,?,?,?,?,?,?,?,?)}"},   // insert
			{"{CALL pomdb.vyberZakazniky(?)}", "{CALL pomdb.vyberModely(?,?,?,?,?,?,?)}", "{CALL pomdb.vyberZakazky2(?,?,?,?,?,?,?)}", "{CALL pomdb.vyberFyzKusy(?,?)}"},	//select
			{"{CALL pomdb.upravZakaznika(?,?)}", "{CALL pomdb.upravModel(?,?,?,?,?,?,?,?,?)}", "{CALL pomdb.upravZakazku(?,?,?,?,?,?,?,?,?,?,?,?)}"},  //update
			{"{CALL pomdb.zadejCisloTavby(?,?)}", "{CALL pomdb.zadejDilciTerminDatumLiti(?,?,?)}", "{CALL pomdb.zadejOdlitek(?,?,?,?,?,?)}", "{CALL pomdb.zadejUdajeOZmetku(?,?,?,?)}"},
			{"{CALL pomdb.pridejVinika(?,?)}", "{CALL pomdb.pridejVinika(?,?)}", "{CALL pomdb.planovaniRozvrh(?,?,?,?)}"}
	};
	/**
	 * Prikazy pro vybrani viniku a vad 
	 */
	private static final String [] preparedStatements = {
		"SELECT vinici.Id_vinika,vinici.Jmeno_vinika FROM pomdb.vinici;",
		"SELECT vady.idvady, vady.vada FROM pomdb.vady;"
		};
	/**
	 * �lo�i�t� pro objekt Connection a pro objekty CallableStatement. Tato t��da je implementov�na p�esn� na m�ru moj� datab�ze.
	 * @param sklad Sklad, kde jsou ulo�eny v�t�ina prom�nn�ch, respektive jejich odkazy
	 */
	public DoInBack(SkladOdkazu sklad){
		this.sklad = sklad;
		this.conn = sklad.getConn();
		this.hlavniOkno = sklad.getHlavniOkno();
	}
	
	/**
	 * @param jmeno nov� jmeno zakaznika
	 * @throws SQLException
	 */
	
	public void novyZakaznik(String jmeno) throws SQLException{
		Task t = new Task() {
			@Override
			protected ResultSet udelej() throws Exception{
				try{
					//sql.novyZakaznik(jmeno);
					throw new Exception();
				} catch(Exception e){
					
				}
				return null;
			}
		};
		t.execute();		
	}
	
	/**
	 * 
	 * @param jmeno
	 * @param cisloModelu
	 * @param material
	 * @param materialVlastni
	 * @param hmotnost
	 * @param isOdhadHmot
	 * @param formovna
	 * @param norma
	 * @throws SQLException
	 */
	public void novyModel(String jmeno, String cisloModelu, String material, String materialVlastni, double hmotnost,
			boolean isOdhadHmot,  String formovna, double norma) throws SQLException{
		int i = 0, j = 1;
		if(jmeno == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no modelu je pr�zdn�");
			return;
		}
		else if(jmeno.equalsIgnoreCase("") || jmeno.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no modelu je pr�zdn� nebo moc velk�");
			return;
		}
		if(cisloModelu == null){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo modelu je pr�zdn�");
			return;
		}
		else if(cisloModelu.equalsIgnoreCase("") || cisloModelu.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo modelu je pr�zdn� nebo moc velk�");
			return;
		}
		if(material == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no materi�lu je pr�zdn�");
			return;
		}
		else if(material.equalsIgnoreCase("") || material.length() > maxDelkamaterialu){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no materi�lu je pr�zdn� nebo moc velk�");
			return;
		}
		if(materialVlastni == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no vlastn�ho materi�lu je pr�zdn�");
			return;
		}
		else if(materialVlastni.equalsIgnoreCase("") || materialVlastni.length() > maxDelkaVlastnihoMaterialu){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no vlastn�ho mareri�lu je pr�zdn� nebo moc velk�");
			return;
		}
		if(hmotnost <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Hmotnost je men�� nebo rovno nule");
			return;
		}
		if(formovna == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Formovna je �patn� zapsan�");
			return;
		}
		else if(formovna.equalsIgnoreCase("") || formovna.length() > 1){
			JOptionPane.showMessageDialog(hlavniOkno, "Formovna je �patn� zapsan�");
			return;
		}
		if(norma <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Norma je men�� nebo rovno nule");
			return;
		}
		
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setString(1, jmeno); c.setString(2, cisloModelu); c.setString(3, material); c.setString(4, materialVlastni);
		c.setDouble(5, hmotnost); c.setBoolean(6, isOdhadHmot); c.setString(7, formovna); c.setDouble(8, norma);
		c.execute();
	}
	
	/**
	 * 
	 * @param idZakaznika
	 * @param idModelu
	 * @param cisloObjednavky
	 * @param datumPrijetiZakazky
	 * @param pocetKusu
	 * @param paganyrka
	 * @param cena
	 * @param isCZK
	 * @param isZakus
	 * @param KurzEuNaCZK
	 * @param poznamka
	 * @throws SQLException
	 */
	public void novaZakazka(int idZakaznika, int idModelu, String cisloObjednavky, Date datumPrijetiZakazky, int pocetKusu, String paganyrka,
			double cena, boolean isCZK, boolean isZakus, double KurzEuNaCZK, String poznamka) throws SQLException{
		if(idZakaznika < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id z�kazn�ka je men�� ne� nula");
			return;
		}
		if(idModelu < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id modelu je men�� ne� nula");
			return;
		}
		if(cisloObjednavky == null){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo objedn�vky je pr�zdn�");
			return;
		}else if(cisloObjednavky.equalsIgnoreCase("") || maxDelkaRetezce < cisloObjednavky.length()){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo objedn�vky je moc velk� nebo je pr�zdn�, m��e obsahovat pouze "+maxDelkaRetezce+" znak�");
			return;
		}
		if(datumPrijetiZakazky == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Datum p�ijet� objedn�vky je �patn�");
			return;
		}
		if(pocetKusu <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Po�et kus� je men�� nebo rovno nule");
			return;
		}else if(pocetKusu > maxPocetKusuNovaZakazka){
			JOptionPane.showMessageDialog(hlavniOkno, "Datab�ze je nastavena na maxim�ln� po�et kus� v objedn�vce na 2000 kus�, aby omylem nedo�lo k vygenerov�n� p�il� nov�ch kus� a zahlcen� datab�ze. \n V p��pad�, �e jedn�te to �mysln� rozd�lt� zak�zku na v�ce ��st�.");
		}
		if(paganyrka != null){
			if(paganyrka.length() > maxDelkaPaganyrky){
				JOptionPane.showMessageDialog(hlavniOkno, "Pagan�rka je moc m��e obsahovat max "+maxDelkaPaganyrky+" znak�");
				return;
			}
		}
		if(cena <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Cena je men�� nebo rovno nule");
			return;
		}
		if(!isCZK){
			if(KurzEuNaCZK <= 0){
				JOptionPane.showMessageDialog(hlavniOkno, "Kurz je men�� nebo rovno nule");
				return;
			}
		}
		if(poznamka != null){
			if(poznamka.length() > maxDelkaPoznamkyUZakazky){
				JOptionPane.showMessageDialog(hlavniOkno, "Pozn�mka je moc m��e obsahovat max "+maxDelkaPoznamkyUZakazky+" znak�");
				return;
			}
		}
		int i = 0, j = 2;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		java.sql.Date  pomDate = new java.sql.Date (datumPrijetiZakazky.getTime());
		c.setInt(1, idZakaznika); c.setInt(2, idModelu); c.setString(3, cisloObjednavky); c.setInt(4, pocetKusu); c.setDate(5, pomDate);
		if(paganyrka == null || paganyrka.equalsIgnoreCase("")){
			if(isCZK)c.setNull(6, java.sql.Types.VARCHAR);
		}
		else {
			c.setString(6, paganyrka);
		}
		c.setDouble(7, cena); c.setBoolean(8, isCZK); c.setBoolean(9, isZakus);
		
		if(isCZK){
			c.setNull(10, java.sql.Types.DOUBLE);
		}
		else{
			c.setDouble(10, KurzEuNaCZK);
		}
		if(poznamka == null || poznamka.equalsIgnoreCase("")){
			c.setNull(11, java.sql.Types.VARCHAR);
		}else {
			c.setString(11, poznamka);
		}
		
		rs = c.executeQuery();
		if(!rs.getBoolean(1)){
			JOptionPane.showMessageDialog(hlavniOkno, "Duplicita, stejn� zak�zka u� existuje (nebere se v podtaz pozn�mka, ta m��e b�t ruzn�) \n nebo"
					+ "jste zadali vice jak 2000 kusu v nove zakazce");
		}
	}
	
	/**
	 * 
	 * @param jmeno
	 * @return
	 * @throws SQLException
	 */
	public ResultSet vyberZakazniky(String jmeno) throws SQLException{
		if(jmeno == null)jmeno = "";
		if(jmeno.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no z�kazn�ka je pr�zdn� nebo moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return null;
		}
		int i = 1,j = 0;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setString(1, jmeno);
		return c.executeQuery();
	}

	/**
	 * 
	 * @param jmenoZakaznika
	 * @param cisloModelu
	 * @param nazevModelu
	 * @param idModelu
	 * @param datumZakazky
	 * @param formovna
	 * @param idZakazky
	 * @return
	 * @throws SQLException
	 */
	public ResultSet vyberModely(String jmenoZakaznika, String cisloModelu, String nazevModelu, int idModelu, java.util.Date datumZakazky, String formovna, int idZakazky) throws SQLException{
		if(jmenoZakaznika == null){
			jmenoZakaznika = "";
		}else if(jmenoZakaznika.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no z�kazn�ka je pr�zdn� nebo moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return null;
		}
		if(cisloModelu == null) {
			cisloModelu = "";
		}else if(cisloModelu.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo modelu je moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return null;
		}
		if(nazevModelu == null){
			nazevModelu = "";
		}else if(nazevModelu.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no z�kazn�ka je moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return null;
		}
		if(idModelu < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id modelu je men�� ne� nula");
			return null;
		}		
		if(formovna == null){
			formovna = "";
		} else if (formovna.length() != 1){
			formovna = "";
		}
		if(idZakazky < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id modelu je men�� ne� nula");
			return null;
		}
		
		int i = 1,j = 1;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setString(1, jmenoZakaznika); c.setString(2, cisloModelu); c.setString(3, nazevModelu); c.setInt(4, idModelu);
		
		if(datumZakazky == null) {
			c.setNull(5, java.sql.Types.DATE);		
		}else {
			java.sql.Date sqlDate = new java.sql.Date(datumZakazky.getTime());
			c.setDate(5, sqlDate);  // datumZakazky 
		}		
		c.setString(6, formovna); c.setInt(7, idZakazky);
		return c.executeQuery();
	}
	
	/**
	 * 
	 * @param idZakazky
	 * @param jmenoZakaznika
	 * @param cisloModelu
	 * @param nazevModelu
	 * @param idModelu
	 * @param datumZakazky
	 * @param cisloObjednavky
	 * @return
	 * @throws SQLException
	 */
	public ResultSet vyberZakazky(int idZakazky, String jmenoZakaznika, String cisloModelu, String nazevModelu, int idModelu, java.util.Date datumZakazky, String cisloObjednavky) throws SQLException{
		if(idZakazky < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id zak�zky je men�� ne� nula");
			return null;
		}
		if(jmenoZakaznika == null){
			jmenoZakaznika = "";
		}else if(jmenoZakaznika.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no z�kazn�ka je moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return null;
		}
		if(cisloModelu == null) {
			cisloModelu = "";
		}else if(cisloModelu.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo modelu je moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return null;
		}
		if(nazevModelu == null){
			nazevModelu = "";
		}else if(nazevModelu.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no z�kazn�ka je moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return null;
		}
		if(cisloObjednavky == null){
			cisloObjednavky = "";
		}else if(cisloObjednavky.length() > maxDelkaRetezce){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo objedn�vky je moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return null;
		}
		if(idModelu < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id modelu je men�� ne� nula");
			return null;
		}		
		int i = 1,j = 2;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setInt(1, idZakazky); c.setString(2, jmenoZakaznika); c.setString(3, cisloModelu); c.setString(4, nazevModelu); c.setInt(5, idModelu);
		
		if(datumZakazky == null) {
			c.setNull(6, java.sql.Types.DATE);		
		}else {
			java.sql.Date sqlDate = new java.sql.Date(datumZakazky.getTime());
			c.setDate(6, sqlDate);  // datumZakazky 
		}
		c.setString(7, cisloObjednavky);
		return c.executeQuery();
	}
	
	/**
	 * 
	 * @param idZakazky
	 * @return
	 * @throws SQLException
	 */
	public TransClass vyberFyzKusy(int idZakazky) throws SQLException{
		if(idZakazky < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id zak�zky je v programu �patn� zaps�no");
			return null;
		}
		
		Task t = new Task();
		t.execute();
		int i = 1,j = 3;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setInt(1, idZakazky);
		int pom;
		c.registerOutParameter(2, java.sql.Types.SMALLINT);
		ResultSet rs = c.executeQuery();
		TransClass s= new TransClass(c.getInt(2), rs);
		return s;
	}
	
	/**
	 * 
	 * @param idZakazky
	 * @param jmenoZakaznika
	 * @param cisloModelu
	 * @param nazevModelu
	 * @param idModelu
	 * @param datumZakazky
	 * @param cisloObjednavky
	 * @return
	 * @throws SQLException
	 */
	
	public ResultSet vyberZmetky(int idZakazky, String jmenoZakaznika, String cisloModelu, String nazevModelu, int idModelu, java.util.Date datumZakazky, String cisloObjednavky) throws SQLException{
		
		return null;
	}
	
	/**
	 * 
	 * @return ResultSet s Viniky, kter� ulo��m do t��dy skladodkazu
	 * @throws SQLException
	 */
	public ResultSet vyberViniky() throws SQLException{
		PreparedStatement s = conn.prepareStatement(preparedStatements[0]);
		rs = s.executeQuery();
		return rs;
	}
	
	/**
	 * 
	 * @return ResultSet s Vady, kter� ulo��m do t��dy skladodkazu
	 * @throws SQLException
	 */
	public ResultSet vyberVady() throws SQLException{
		PreparedStatement s = conn.prepareStatement(preparedStatements[1]);
		rs = s.executeQuery();
		return rs;
	}
	
	/**
	 * Upravi zakazn�ka
	 * @param idZakaznika Id z�kazn�ka v databazi
	 * @param noveJmeno Nov� jmeno
	 * @throws SQLException 
	 */
	
	public void updateZakaznika(int idZakaznika, String noveJmeno) throws SQLException{
		if(idZakaznika < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id z�kazn�ka je �patn� v pragramu zapsan�");
			return;
		}
		int i = 2, j = 0;
		if(noveJmeno == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Nov� jm�no je pr�zdn�");
			return;
		}
		if(noveJmeno.equalsIgnoreCase("")){
			JOptionPane.showMessageDialog(hlavniOkno, "Nov� jm�no je pr�zdn�");
			return;
		}
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setInt(1, idZakaznika);
		c.setString(2, noveJmeno);
		c.execute();
	}
	
	/**
	 * 
	 * @param idModelu
	 * @param jmenoModelu
	 * @param cisloModelu
	 * @param material
	 * @param materialVlastni
	 * @param hmotnost
	 * @param isOdhadHmot
	 * @param formovna
	 * @param norma
	 * @throws SQLException
	 */
	public void updateModel(int idModelu, String jmenoModelu, String cisloModelu, String material, String materialVlastni, double hmotnost, boolean isOdhadHmot, String formovna, double norma) throws SQLException{
		if(idModelu < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id modelu je �patn� zapsan� v programu");
			return;
		}
		if(jmenoModelu == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no modelu je pr�zdn�");
			return;
		}else if(jmenoModelu.length() > maxDelkaRetezce || jmenoModelu.equalsIgnoreCase("")){
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no modelu je pr�zdn� nebo moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return;
		}
		if(cisloModelu == null) {
			JOptionPane.showMessageDialog(hlavniOkno, "��slo modelu je pr�zdn�");
			return;
		}else if(cisloModelu.length() > maxDelkaRetezce || cisloModelu.equalsIgnoreCase("")){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo modelu je pr�zdn� nebo moc dlouh�, m��e obsahovat max "+maxDelkaRetezce+" znak�");
			return;
		}
		if(material == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Material je pr�zdn�");
			return;
		}else if(material.length() > maxDelkamaterialu || material.equalsIgnoreCase("")){
			JOptionPane.showMessageDialog(hlavniOkno, "Material je pr�zdn� nebo moc dlouh�, m��e obsahovat max "+maxDelkamaterialu+" znak�");
			return;
		}
		if(materialVlastni == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Material je pr�zdn�");
			return;
		}else if(materialVlastni.length() > maxDelkamaterialu || materialVlastni.equalsIgnoreCase("")){
			JOptionPane.showMessageDialog(hlavniOkno, "Vlastn� material je pr�zdn� nebo moc dlouh�, m��e obsahovat max "+maxDelkamaterialu+" znak�");
			return;
		}
		if(hmotnost <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Hmotnost je men�� nebo rovno nule");
			return;
		}
		
		if(formovna == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Formovna je �patn� zaps�na");
		}
		else if( formovna.length() != 1){
			JOptionPane.showMessageDialog(hlavniOkno, "Formovna je �patn� zaps�na");
			return;
		}
			
			
		
		
		
		
		int i = 2, j = 1;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setInt(1, idModelu); c.setString(2, jmenoModelu); c.setString(3, cisloModelu); c.setString(4, material); c.setString(5, materialVlastni);
		c.setDouble(6, hmotnost); c.setBoolean(7, isOdhadHmot); c.setString(8, formovna); c.setDouble(9, norma);
		c.execute();
	}
	
	/**
	 * 
	 * @param idZakazky
	 * @param idZakaznika
	 * @param idModelu
	 * @param cisloObjednavky
	 * @param datumPrijetiZakazky
	 * @param pocetKusu
	 * @param paganyrka
	 * @param cena
	 * @param isCZK
	 * @param isZakus
	 * @param KurzEuNaCZK
	 * @param poznamka
	 * @throws SQLException
	 */
	public void updateZakazku(int idZakazky, int idZakaznika, int idModelu, String cisloObjednavky, Date datumPrijetiZakazky, int pocetKusu, String paganyrka,
			double cena, boolean isCZK, boolean isZakus, double KurzEuNaCZK, String poznamka) throws SQLException{
		if(idZakazky < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id z�kazn�ka je men�� ne� nula");
			return;
		}
		if(idZakaznika < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id z�kazn�ka je men�� ne� nula");
			return;
		}
		if(idModelu < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id modelu je men�� ne� nula");
			return;
		}
		if(cisloObjednavky == null){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo objedn�vky je pr�zdn�");
			return;
		}else if(cisloObjednavky.equalsIgnoreCase("") || maxDelkaRetezce < cisloObjednavky.length()){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo objedn�vky je moc velk� nebo je pr�zdn�, m��e obsahovat pouze "+maxDelkaPaganyrky+" znak�");
			return;
		}
		if(datumPrijetiZakazky == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Datum p�ijet� objedn�vky je �patn�");
			return;
		}
		if(pocetKusu <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Po�et kus� je men�� nebo rovno nule");
			return;
		}
		if(paganyrka != null){
			if(paganyrka.length() > maxDelkaPaganyrky){
				JOptionPane.showMessageDialog(hlavniOkno, "Pagan�rka je moc m��e obsahovat max "+maxDelkaPaganyrky+" znak�");
				return;
			}
		}
		if(cena <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Cena je men�� nebo rovno nule");
			return;
		}
		if(KurzEuNaCZK <=0){
			JOptionPane.showMessageDialog(hlavniOkno, "Kurz je men�� nebo rovno nule");
			return;
		}
		if(poznamka != null){
			if(poznamka.length() > maxDelkaPoznamkyUZakazky){
				JOptionPane.showMessageDialog(hlavniOkno, "Pozn�mka je moc m��e obsahovat max"+maxDelkaPoznamkyUZakazky+" znak�");
				return;
			}
		}
		int i = 2, j = 2;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		java.sql.Date pomDate = new java.sql.Date (datumPrijetiZakazky.getTime());
		
		c.setInt(1, idZakazky);
		c.setInt(2, idZakaznika); c.setInt(3, idModelu); c.setString(4, cisloObjednavky); c.setInt(5, pocetKusu); c.setDate(6, pomDate);
		if(paganyrka == null || paganyrka.equalsIgnoreCase("")){
			if(isCZK)c.setNull(7, java.sql.Types.VARCHAR);
		}
		else {
			c.setString(7, paganyrka);
		}
		c.setDouble(8, cena); c.setBoolean(9, isCZK); c.setBoolean(10, isZakus);
		
		if(isCZK){
			c.setNull(11, java.sql.Types.DOUBLE);
		}
		else{
			c.setDouble(11, KurzEuNaCZK);
		}
		if(poznamka == null || poznamka.equalsIgnoreCase("")){
			c.setNull(12, java.sql.Types.VARCHAR);
		}else {
			c.setString(12, poznamka);
		}
		c.execute();
	}
	/**
	 * 
	 * @param fyzKusu
	 * @param cisloTavby
	 * @throws SQLException
	 */
	public void zadejCisloTavby(int fyzKusu, String cisloTavby) throws SQLException{
		if(fyzKusu < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id fyz. kusu je �patn� zapsan�");
			return;			
		}
		if(cisloTavby == null){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo tavby je pr�zdn�");
			return;
		} else if (cisloTavby.equalsIgnoreCase("")|| cisloTavby.length() > maxDelkaCislaTavby){
			JOptionPane.showMessageDialog(hlavniOkno, "��slo tavby je moc dlouh� nebo je pr�zdn�, ��slo tavby mu�e m�t "+maxDelkaCislaTavby+" znak�");
			return;		
		}
		
		int i = 3, j = 0;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setInt(1, fyzKusu);
		c.setString(2, cisloTavby);
		c.execute();
	}
	
	/**
	 * 
	 * @param idKusu
	 * @param datumLiti
	 * @param dilciTermin
	 * @throws SQLException 
	 */
	public void zadejDatumLitiDilciTermin(int idKusu, Date datumLiti, Date dilciTermin) throws SQLException{
		if(idKusu < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id fyz. kusu je �patn� zapsan�");
			return;
		}
		int i = 3, j = 1;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setInt(1, idKusu);
		if(datumLiti == null) {
			c.setNull(2, java.sql.Types.DATE);		
		}else {
			java.sql.Date sqlDate = new java.sql.Date(datumLiti.getTime());
			c.setDate(2, sqlDate);  // datumZakazky 
		}
		if(dilciTermin == null) {
			c.setNull(3, java.sql.Types.DATE);		
		}else {
			java.sql.Date sqlDate = new java.sql.Date(dilciTermin.getTime());
			c.setDate(3, sqlDate);  // datumZakazky 
		}
		c.execute();
	}
	
	/**
	 * 
	 * @param idFyzKusu
	 * @param isOdlito
	 * @param isVycisteno
	 * @throws SQLException 
	 */
	public void zadejOdlitek(int idFyzKusu, boolean isOdlito, boolean isVycisteno,boolean isZmetek, Date datumExpedice, boolean isExpedovano) throws SQLException{
		if(idFyzKusu < 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Id fyz. kusu je �patn� zapsan�");
			return;
		}
		int i = 3, j = 2;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		
		c.setInt(1, idFyzKusu);
		c.setBoolean(2, isOdlito);
		c.setBoolean(3, isVycisteno);
		c.setBoolean(4, isZmetek);
		if(datumExpedice == null){
			c.setNull(5, java.sql.Types.DATE);
		} else {
			c.setDate(5, new java.sql.Date(datumExpedice.getTime()));
		}
		c.setBoolean(6, isExpedovano);
		c.execute();
	}
	
	/**
	 * Je�t� neni implementovan�
	 * @throws SQLException 
	 */
	public boolean zadejVadyZmetku(int idFyzKusu, int idVinika, int idVady) throws SQLException{
		int i = 3, j = 3;
		if(idFyzKusu <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "�patn� Id fyzick�ho kusu");
			return false;
		}
		if(idVinika <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "�patn� Id vin�ka");
			return false;
		}
		if(idVady <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "�patn� Id vady");
			return false;
		}
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setInt(1, idFyzKusu);
		c.setInt(2, idVinika);
		c.setInt(3, idVady);
		c.registerOutParameter(4, java.sql.Types.TINYINT);
		c.executeQuery();
		boolean pom = c.getBoolean(4);
		return pom;
	}
	
	/**
	 * 
	 * @param vinik
	 * @throws SQLException
	 */
	public void pridejVinika(String vinik) throws SQLException{
		if(vinik == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Vinik je prazdny");
			return;
		}else if(vinik.length() > maxDelkaVinika || vinik.equalsIgnoreCase("")){
			JOptionPane.showMessageDialog(hlavniOkno, "Vinik je moc dlouh� nebo je pr�zdn�");
			return;
		}
		int i = 4, j = 0;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		c.setString(1, vinik);
		c.execute();
	}
	
	/**
	 * 
	 * @param vada
	 * @throws SQLException
	 */
	public void pridejVadu(String vada) throws SQLException{
		if(vada == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Vada je pr�zdn�");
			return;
		}else if(vada.length() > maxDelkaVady|| vada.equalsIgnoreCase("")){
			JOptionPane.showMessageDialog(hlavniOkno, "Vada je moc dlouh� nebo je pr�zdn�");
			return;
		}
		int i = 4, j = 1;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];	
		c.setString(1, vada);
		c.execute();
	}
	
	/**
	 * 
	 * @param zacatekRozvrhu
	 * @param pocetTydnu
	 * @param idZakazky
	 * @return vraci TransClass, obsahuje ResultSet s v�sledky a Integer, kter� reprezentuje po�et nenapl�novan�ch kus�
	 * @throws SQLException
	 */
	public TransClass planovaniRozvrh(Date zacatekRozvrhu, int pocetTydnu, int idZakazky) throws SQLException{
		if(zacatekRozvrhu == null){
			JOptionPane.showMessageDialog(hlavniOkno, "Datum je �patn� (sqlstor)");
			throw new SQLException("Datum je spatne");
		}
		if(pocetTydnu <= 0){
			JOptionPane.showMessageDialog(hlavniOkno, "Datum je �patn� (sqlstor)");
			throw new SQLException("Pocet tydnu je spatne, je mensi nebo rovno 0");
		}
		int i = 4, j = 2;
		if(cst[i][j] == null){
			cst[i][j] = conn.prepareCall(sqlPrikazy[i][j]);
			naposledyPouzito[i][j] = new Date();
		}
		c = cst[i][j];
		
		java.sql.Date sqlDate = new java.sql.Date(zacatekRozvrhu.getTime());
		c.setDate(1, sqlDate); // datumZakazky
		c.setInt(2, pocetTydnu);
		c.registerOutParameter(3, java.sql.Types.INTEGER);
		c.setInt(4, idZakazky);
		rs = c.executeQuery();
		int pocet = c.getInt(3);
		TransClass ts = new TransClass(pocet,rs);
		return ts;
	}
	
	/**
	 * Zav�e ve�ker� spojen� s datab�z�
	 * @throws SQLException
	 */
	public void closeConnections() throws SQLException {
		if(conn != null){
			conn.close();
			conn = null;
		}else{
			JOptionPane.showMessageDialog(hlavniOkno, "Spojeni s databaz� u� bylo d��ve p�eru�eno");
		}
		for (int i = 0; i < cst.length; i++) {
			for (int j = 0; j < cst[i].length; j++) {
				if (cst[i][j] != null){
					cst[i][j].close();
					cst[i][j] = null;
				}
			}
		}
	}
	
	private class Task extends SwingWorker<ResultSet, Void> {
        /*
         * Main task. Executed in background thread.
         */
		private Task(){
			prgbar.setVisible(true);
		}
        @Override
        public ResultSet doInBackground() {
        	try{
        		return udelej();
        	} catch(Exception e){
        		ExceptionWin win = new ExceptionWin(e);
        		win.nic();
        		return null;
        	}
        }
        protected ResultSet udelej() throws Exception{
        	return null;
        }

        /*
         * Executed in event dispatching thread
         */
        @Override
        public void done() {
        	prgbar.setVisible(false);
        }
    }
	
	
	private class PosluchacCasovace implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        	Date tedDate = new Date();
        	System.out.println(tedDate.getTime());
        	System.out.println("ma�u Callable statement");
        	for(int i = 0; i < naposledyPouzito.length; i++){
    			for(int j = 0; j < naposledyPouzito[i].length; j++){
    				if(naposledyPouzito[i][j] == null)continue;
    				System.out.println("zkousim");
    				if(tedDate.getTime() - naposledyPouzito[i][j].getTime() > dobaNaZavrenyPripojeni){
    					try {
							cst[i][j].close();
							cst[i][j] = null;
							System.out.println("Callable statement "+i+" " +j+" zru�eno");
							naposledyPouzito[i][j] = null;
						} catch (SQLException e1) {
							ExceptionWin win = new ExceptionWin(e1);
						}
    				}
    			}
    		}
        }
    }
}

