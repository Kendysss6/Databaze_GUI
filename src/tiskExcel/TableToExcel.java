package tiskExcel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import app.MainFrame;
import sablony.ParametryFiltr;
import sablony.tabulka.QueryTableModel;

/**
 * T��da pro export Table model do .xls souboru (Excel soubor). N�vrh �e�en� probl�mu:
 * 1. vytvo�it metodu se switchem, kter� podle ��sla vypisu navrat� n�zev sheetu, nadpis, cestu kde ulo�it v�etn� jm�na
 * 2. vytvo�it metodu kter� n�m vr�t� kolik kter� bunka m� ���ku a kolik ��dek jsou nadpisy. 
 * 	 mo�n� jen boolean jestli je to nutn�, proto�e jediny kdo pot�ebuje v�c ��dk�
 *   je Lic� pl�n - planovac� - nezapomen doplnit poznamku do sloupce
 * 3. vytvo�it metodu kter� vlo�� data do xls souboru  (insertData(.))
 * Rozlo�en� str�nek (okraje stranky) budou pro v�echny v�pisy stejn�
 * @author Havlicek
 *
 */
public class TableToExcel {
	public static final int liciPlanZakl = 21;
	public static final int liciPlanPlanovaci = 22;
	public static final int planExpedice = 23;
	public static final int VYPIS_ZMETKU_MZDY = 30;
	
	private JFrame hlavniOkno;
	private static String [] columnNamesNotInt = {"��slo modelu", "Cislo_modelu","Jm�no z�kazn�ka","��slo objedn�vky",
			"Jm�no modelu", "Materi�l", "Vlastn� materi�l"};
	
	/**
	 * Vytvo�� .xls soubor do p�edem dan� slo�ky s jedno��dkovou hlavi�kou.
	 * (Doporu�en�: �daje by se m�li vej�t na A4 na v��ku)
	 * @param hlavniOkno pouzit� na zobrazen� chyby (metoda export())
	 * @param model ze kter�ho �erp�me data. Mus� b�t <code>QueryTableModel</code>
	 * @param nadpisExt roz���en� nadpisu v tisk (obvykle datum)
	 * @param name jm�no souboru bez koncovky
	 * @param cisloExportu ��slo exportu (��slo �ablony, kterou pou�ijeme pro soubor xls)
	 * @throws Exception vyhod� chybu, pokud n�co nesouhlas�
	 */
	public static void exportToExcelNaVysku(JFrame hlavniOkno, TableModel model, String nadpisExt, String name, int cisloExportu) throws Exception{
		new TableToExcel(hlavniOkno, model, nadpisExt, name, cisloExportu, true);
	}
	
	/**
	 * Vytvo�� .xls soubor do p�edem dan� slo�ky specialn� pro lici plan- Bude vice radku
	 * (Doporu�en�: �daje by se m�li vej�t na A4 na ���ku)
	 * @param hlavniOkno pouzit� na zobrazen� chyby (metoda export())
	 * @param model ze kter�ho �erp�me data. Mus� b�t <code>QueryTableModel</code>
	 * @param nadpisExt roz���en� nadpisu v tisk (obvykle datum)
	 * @param name jm�no souboru bez koncovky
	 * @param cisloExportu ��slo exportu (��slo �ablony, kterou pou�ijeme pro soubor xls)
	 * @throws Exception vyhod� chybu, pokud n�co nesouhlas�
	 */
	public static void exportToExcelNaSirku(JFrame hlavniOkno, TableModel model, String nadpisExt, String name, int cisloExportu) throws Exception{
		new TableToExcel(hlavniOkno, model, nadpisExt, name, cisloExportu, false);
	}
	
	/**
	 * Vytvo�� t��du, kter� vytvo�� .xls soubor do p�edem dan� slo�ky s jedno��dkovou hlavi�kou.
	 * (Doporu�en�: �daje by se m�li vej�t na A4 na v��ku)
	 * @param hlavniOkno pouzit� na zobrazen� chyby (metoda export())
	 * @param model ze kter�ho �erp�me data. Mus� b�t <code>QueryTableModel</code>
	 * @param nadpisExt roz���en� nadpisu v tisk (obvykle datum)
	 * @param name jm�no souboru bez koncovky
	 * @param cisloExportu ��slo exportu (��slo �ablony, kterou pou�ijeme pro soubor xls)
	 * @param isNaVysku zda je tisk na v��ku nebo ���ku. V��ku = <code>true</code>
	 * @throws Exception vyhod� chybu, pokud n�co nesouhlas�
	 */
	public TableToExcel(JFrame hlavniOkno, TableModel model,String nadpisExt, String name, int cisloExportu, boolean isNaVysku) throws Exception{
		this.hlavniOkno = hlavniOkno;
		this.export((QueryTableModel) model, nadpisExt, name, cisloExportu, isNaVysku);
	}
	
	/**
	 * Metoda, kde d�je hlavn� algoritmus. Zde se v�e p�ev�d�
	 * @param model model, ze kret�ho �erp�me data
	 * @param name nazev souboru bez koncovky
	 * @param nadpisExt roz���en� nadpisu v tisk (obvykle datum)
	 * @param cisloExportu druh vypisu, podle kter�ho p�izp�sob�me .xls soubor
	 * @throws Exception 
	 */
	private void export(QueryTableModel model, String nadpisExt, String name, int cisloExportu, boolean isNaVysku) throws Exception{
		HSSFWorkbook wb = new HSSFWorkbook();
		String [] atr = getAtributes(cisloExportu);
		HSSFSheet sheet = wb.createSheet(atr[0]);
		
		//set header (nadpis v tisku)
		Header header = sheet.getHeader();
		header.setCenter(HSSFHeader.font("Stencil-Normal", "bold")+ HSSFHeader.fontSize((short) 14)+ atr[1] + nadpisExt);// + nadpisExt);
		
		sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

		sheet.getPrintSetup().setLandscape(!isNaVysku);
		
		
		//number of pages
		Footer footer = sheet.getFooter();
		footer.setRight("Strana " + HeaderFooter.page() + " z " + HeaderFooter.numPages());
		//set visibile grid lines on printed pages
		sheet.setPrintGridlines(true);
	    
		sheet.setMargin(Sheet.BottomMargin, 0.5);
		sheet.setMargin(Sheet.TopMargin, 0.6);
		sheet.setMargin(Sheet.LeftMargin, 0.3);
		sheet.setMargin(Sheet.RightMargin, 0.3);
		
		sheet.setMargin(Sheet.HeaderMargin, 0.1);
		sheet.setMargin(Sheet.FooterMargin, 0.3);
		
		// font pro bunky		
		HSSFFont font = wb.createFont();
		font.setFontHeightInPoints((short) 12);
		//HSSFCellStyle style = wb.createCellStyle();
		//style.setBorderTop(CellStyle.BORDER_DOUBLE);
		//style.setFont(font);
		//insert data
		this.insertData(model, sheet, cisloExportu, font);
		//set First row as header at all printed pages
		sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));	
		
		// Write it into the output to a file
		
		//create folder
		File f = new File(atr[2]);
		try {
			if (f.mkdir()) {
				//System.out.println("Directory Created");
			} else {
				//System.out.println("Directory is not created");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(hlavniOkno, "Slo�ka nebyla vytvo�ena");
		}
		
		try {
			FileOutputStream fileOut = new FileOutputStream(atr[2]+"/"+name+".xls");
			wb.write(fileOut);
			wb.close();
			fileOut.close();
			JOptionPane.showMessageDialog(hlavniOkno, "Excel soubor "+atr[2].substring(2, atr[2].length())+"/"+name+".xls vytvo�en.");
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(hlavniOkno, "M�te otev�en� soubor, do kter�ho se zapisuje. Zav�ete jej pros�m");
			wb.close();
		}
	}
	
	/**
	 * Metoda pro vlo�en� dat do tabulky, je�t� bych m�l zv�it �e p�idam parametr pro mergovan� bun�k.
	 * Je�t� mus�m vymyslet jak. N�jaky jednoduhcy �e�en�. :)
	 * @param model ze kter�ho budeme ��st data
	 * @param sheet prazdny sheet do kter�ho budeme data vkl�dat
	 * @param cisloExportu ��slo exportu
	 * @param font font ktery pou�ijeme v bunkach
	 * @throws Exception
	 */
	private void insertData(QueryTableModel model, HSSFSheet sheet, int cisloExportu, HSSFFont font) throws Exception{
		Row row = sheet.createRow(0);
		Cell cell = null;
		
		//insert Column name
			
		cell = row.createCell(0);
		cell.setCellValue(model.getColumnName(0));
			// naveni fontu
		if (cisloExportu != liciPlanPlanovaci){ // Jedina vyjimka je liciPlanPlanovaci tomu zustane pismo velkosti 10, jem moc velky
			cell.getCellStyle().setFont(font);
		}
		for(int i = 1; i < model.getColumnCount() -1 ; i++){ //mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
			cell = row.createCell(i);
			cell.setCellValue(model.getColumnName(i));
		}
		//detect data format
		boolean [] isNumber = detectDataFormat(model); 
		//insert data 
		for(int i = 1; i <= model.getRowCount(); i++){
			row = sheet.createRow(i);
			for(int j = 0; j < model.getColumnCount() -1 ; j++){ //mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
				cell = row.createCell(j);
				
				try {
					if (isNumber[j]) {
						if (model.getValueAt(i - 1, j).length() > 0) {
							cell.setCellValue(Double.parseDouble((model.getValueAt(i - 1, j))));
						}
					} else {
						cell.setCellValue(model.getValueAt(i - 1, j));
					}
				} catch (NumberFormatException | NullPointerException ex ) {
					isNumber[j] = false;
					cell.setCellValue(model.getValueAt(i - 1, j));
				}
			}
		}
		//Format data
		for(int i = 0; i < model.getColumnCount() -1 ; i++){//mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
			sheet.autoSizeColumn(i);
		}
	}
	
	/**
	 * Metoda pro zji�t�n� atribut� v�pisu. Navr�t� String pole o 3 prvc�ch v po�ad�:
	 * <ol>
	 *  <li>Jm�no sheetu</li>
	 *  <li>Nadpis v tisku pap�ru</li>
	 *  <li>Cestu kam ulo�it (relativn�)</li>
	 * </ol>
	 * @param cisloExportu ��slo exportu, o kter�m chceme zn�t atributy
	 * @return String [] o 3 prvc�ch nikdy <code>null</code>
	 */
	private static String [] getAtributes(int cisloExportu){
		String [] atr = {"prazdnyatr1","prazdnyatr2","prazdnyatr3"};
		/**
		 * Kvuli tomu aby jmena souboru m�li �isla od 1 .. n a ne od 0 tak zv�t��m i o 1
		 * viz metoda vypisyAndTisk(.,.) v HledejListener.java
		 */
		switch(cisloExportu){
		case ParametryFiltr.VypisStavNeuzavrenychZakazek:
			atr[0] = "Stav neuzav�en�ch zak�zek";atr[1] = "Stav neuzav�en�ch zak�zek ke dni ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.DenniVypisOdlitychKusu:
			atr[0] = "V�pis odlit�ch kus�";atr[1] = "V�pis odlit�ch kus� ke dni ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisVycistenychKusuZaObdobi:
			atr[0] = "V�pis vy�i�t�n�ch kus� za obdob�";atr[1] = "Vy�i�t�n� kusy od ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.MzdySlevacu:
			atr[0] = "Mzdy sl�va��";atr[1] = "Mzdy sl�va�� ke dni ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisOdlitkuVKgKc:
			atr[0] = "V�pis odlitk� v kg-k� za obdob�";atr[1] = "V�pis odlitk� v kg-k� od " ;atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisOdlitychKusuOdDo:
			atr[0] = "V�pis vyroben�ch kus� za obdob�";atr[1] = atr[0];atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisPolozekSOdhadHmot:
			atr[0] = "V�pis polo�ek s odhadovanou hmotnost�";atr[1] = "Polo�ky s odhadovou hmotnost� ke dni ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisDleTerminuExpedice:
			atr[0] = "V�pis zak�zek s term�nem expedice v dan�m t�dnu";atr[1] = atr[0];atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisExpedice_od_do:
			atr[0] = "V�pis expedice zbo�� za obdob�";atr[1] = "Expedice zbo�� od ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisZpozdeneVyroby:
			atr[0] = "V�pis zpo�d�n� v�roby ke dni";atr[1] = atr[0];atr[2] = "./vypisy";
			break;
		case ParametryFiltr.InventuraRozpracVyroby:
			atr[0] = "Inventura rozpracovan� v�roby";atr[1] = "Rozpracovan� v�roba ke dni ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisSkladuKeDnesnimuDni:
			atr[0] = "V�pis skladu ke dne�n�mu dni";atr[1] = "Seznam kus� na sklad� ke dni ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisZmetkuZaObdobi:
			atr[0] = "V�pis zmetk� za obdob�";atr[1] = "V�pis zmetk� od ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.VypisVinikuVKgKcMzdy:
			atr[0] = "V�pis vin�k� v kg-k� obdob�";atr[1] = "V�pis vin�k� v kg/k� od ";atr[2] = "./vypisy";
			break;
		case ParametryFiltr.ZaklPlanLiti:
			atr[0] = "Z�kladni lic� pl�n";atr[1] = "Z�kladni lic� pl�n pro t�den: ";atr[2] = "./lici_plany";
			break;
		case ParametryFiltr.PlanovaniLiti:
			atr[0] = "Lic� pl�n";atr[1] = "Lic� pl�n pro t�den: ";atr[2] = "./lici_plany";
			break;
		case ParametryFiltr.PlanExpedice:
			atr[0] = "Pl�n expedice";atr[1] = "Pl�n expedice ke dni: ";atr[2] = "./lici_plany";
			break;
			}
		return atr;		
	}
	
	/**
	 * Detects whether column is a number or not based on column name or column value.
	 * @param model Table with values a column names
	 * @return boolean field with n-1 columns of the table with true or false values
	 */
	private static boolean [] detectDataFormat(QueryTableModel model){
		boolean [] isNumber = null;
		if(model.getRowCount() >= 2){
			isNumber = new boolean [model.getColumnCount()-1];
			/**
			 * Moc se tady nechapu ale doposud to funguje tak to nebudu menit
			 */
			// pokud ma model vice nez dve radky
			boolean exit = false;
			for (int m = 0; m < 2; m++) { // kontroluju prvni dve radky
				for (int j = 0; j < model.getColumnCount() - 1; j++) { // mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
					String tmp = model.getValueAt(m, j);
					try {
						if (tmp != null) {
							exit = false;
							for(int i = 0; i < columnNamesNotInt.length;i++){ // zda sloupec neni nahodou povinny String a ne cislo
								if(columnNamesNotInt[i].equalsIgnoreCase(model.getColumnName(j))){
									isNumber[j] = false;
									exit = true;
									break;
								}
							}
							if(exit){continue;}
							
							Double.parseDouble((tmp));
							if(m > 0){
								isNumber[j] = isNumber[j] && true; // ten and je jen pro nazornost // druhy radek tabulky
							}else {
								isNumber[j] = true; // prvni radek
							}
						} else {
							isNumber[j] = false;
						}
					} catch (NumberFormatException nfe) {
						isNumber[j] = false;
					}
				}
			}
		} else {
			isNumber = new boolean [model.getColumnCount()-1];
			for(int i = 0; i < model.getColumnCount() - 1; i++){
				isNumber[i] = false;
			}
		}
		return isNumber;
	}
	
	/**
	 * Metoda pro vytvoreni specialniho vypisu zmetku pro sefa. Vymyslet sablonu. ;)
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void vypisZmetkuZmdyToExcel(QueryTableModel tm, MainFrame hlavniOkno, String nadpisExt, String name) throws IOException, ParseException{
		HSSFWorkbook wb = new HSSFWorkbook();
		String [] atr = getAtributes(ParametryFiltr.VypisZmetkuZaObdobi);
		HSSFSheet sheet = wb.createSheet(atr[0]);
		
		//set header (nadpis v tisku)
		Header header = sheet.getHeader();
		header.setCenter(HSSFHeader.font("Stencil-Normal", "bold")+ HSSFHeader.fontSize((short) 16)+ atr[1] + nadpisExt);// + nadpisExt);
		
		sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);
		// nasvateni na vysku
		sheet.getPrintSetup().setLandscape(false);
		
		
		//number of pages
		Footer footer = sheet.getFooter();
		footer.setRight("Strana " + HeaderFooter.page() + " z " + HeaderFooter.numPages());
		//set visibile grid lines on printed pages
		//sheet.setPrintGridlines(true);
	    
		sheet.setMargin(Sheet.BottomMargin, 0.5);
		sheet.setMargin(Sheet.TopMargin, 0.6);
		sheet.setMargin(Sheet.LeftMargin, 0.3);
		sheet.setMargin(Sheet.RightMargin, 0.3);
		
		sheet.setMargin(Sheet.HeaderMargin, 0.1);
		sheet.setMargin(Sheet.FooterMargin, 0.3);
		
		/**
		 * Pokud zmen�m velikost pisma (14) tak mus�m
		 * zm�nit i ��slo ZMETKUNASTRANKU a POCETRADEKNASTRANKU podle toho.
		 */
		HSSFFont font = wb.createFont();
		font.setFontHeightInPoints((short) 13);
		HSSFCellStyle style = wb.createCellStyle();
		style.setBorderTop(CellStyle.BORDER_DOUBLE);
		style.setFont(font);
		
		//insert data
		insertDataToVypisZmetkyMzdy(tm, sheet, font, style);
		
		// Write it into the output to a file
		
		//create folder
		File f = new File(atr[2]);
		try {
			if (f.mkdir()) {
				//System.out.println("Directory Created");
			} else {
				//System.out.println("Directory is not created");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(hlavniOkno, "Slo�ka nebyla vytvo�ena");
		}
		
		try {
			FileOutputStream fileOut = new FileOutputStream(atr[2]+"/"+name+".xls");
			wb.write(fileOut);
			wb.close();
			fileOut.close();
			JOptionPane.showMessageDialog(hlavniOkno, "Excel soubor "+atr[2].substring(2, atr[2].length())+"/"+name+".xls vytvo�en.");
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(hlavniOkno, "M�te otev�en� soubor, do kter�ho se zapisuje. Zav�ete jej pros�m");
			wb.close();
		}
		
	}
	
	private static void insertDataToVypisZmetkyMzdy(QueryTableModel model, HSSFSheet sheet, HSSFFont font, HSSFCellStyle style) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
		int indexOfBeginRow = 0;
		for(int i = 0; i < model.getRowCount(); i++){
			if(i % ZMETKUNASTRANKU == 0 && i != 0){
				int s = indexOfBeginRow % POCETRADEKNASTRANKU;
				indexOfBeginRow += POCETRADEKNASTRANKU - s;
			}
			indexOfBeginRow = insertZmetekDoExcelu(model.getRow(i), sheet, indexOfBeginRow, sdf, font, style);
		}
	}
	
	/**
	 * Indexy sloupcu v tabulce
	 */
	public static final int JMENOZAKAZNIKA = 0;
	public static final int JMENOMODELU = 1;
	public static final int CISLOMODELU = 2;
	public static final int DATUMZMETKU = 3;
	public static final int JMENOVINIKA = 4;
	public static final int VADA = 5;
	public static final int KS = 6;
	public static final int NORMA = 7;
	public static final int NORMACELKEM = 8;
	public static final int HMOTNOSTNAKUS = 9;
	public static final int HMOTNOSTCELKEM = 10;
	public static final int VLASTNIMATERIAL = 11;
	public static final int CENAZAKUS = 12;
	public static final int CENACELKEM = 13;
	
	public static final int STRINGDATA = 0;
	public static final int NUMERICDATA = 1;
	public static final int DATEDATA = 2;
	
	public static final int ZMETKUNASTRANKU = 4;
	public static final int POCETRADEKNASTRANKU = 47;
	
	/**
	 * Vlo�� do Excelu podle p�edem dan� �ablony jednu "bunku" ktera, reprezentuje Pro danou zakazku a dan� datum po�et zmetku, Jmeno zakaznika ...
	 * @throws ParseException 
	 */
	public static int insertZmetekDoExcelu(String [] data, HSSFSheet sheet, int indexOfBeginRow, SimpleDateFormat sdf,
			HSSFFont f, HSSFCellStyle style) throws ParseException{
		// nastaveni tloustky bunky
		//cell.getCellStyle().setBorderBottom(border);
		
		Cell cell = null;
		Row row = null;
		row = sheet.createRow(indexOfBeginRow);		
		addRowValuesVypisZmetekMzdy(row, "Z�kazn�k", data[JMENOZAKAZNIKA], STRINGDATA, null, null, STRINGDATA, sdf, f);
		indexOfBeginRow++;
		row = sheet.createRow(indexOfBeginRow);
		addRowValuesVypisZmetekMzdy(row, "N�zev", data[JMENOMODELU], STRINGDATA, "�. modelu", data[CISLOMODELU], STRINGDATA, sdf, f);
		indexOfBeginRow++;
		row = sheet.createRow(indexOfBeginRow);
		addRowValuesVypisZmetekMzdy(row, "Vin�k", data[JMENOVINIKA], STRINGDATA, "Druh vady", data[VADA], STRINGDATA, sdf, f);
		indexOfBeginRow++;
		row = sheet.createRow(indexOfBeginRow);
		addRowValuesVypisZmetekMzdy(row, "Datum", data[DATUMZMETKU], DATEDATA, "Kus�", data[KS], NUMERICDATA, sdf, f);
		indexOfBeginRow++;
		row = sheet.createRow(indexOfBeginRow);
		addRowValuesVypisZmetekMzdy(row, "Mzda za kus", data[NORMA], NUMERICDATA, "Mzda celkem", data[NORMACELKEM], NUMERICDATA, sdf, f);
		indexOfBeginRow++;
		row = sheet.createRow(indexOfBeginRow);
		addRowValuesVypisZmetekMzdy(row, "Hmotnost ks", data[HMOTNOSTNAKUS], NUMERICDATA, "Hmotnost celkem", data[HMOTNOSTCELKEM], NUMERICDATA, sdf, f);
		indexOfBeginRow++;
		row = sheet.createRow(indexOfBeginRow);
		addRowValuesVypisZmetekMzdy(row, "P. cena", data[CENAZAKUS], NUMERICDATA, "Cena celkem", data[CENACELKEM], NUMERICDATA, sdf, f);
		indexOfBeginRow++;
		row = sheet.createRow(indexOfBeginRow);
		addRowValuesVypisZmetekMzdy(row, "Materi�l", data[VLASTNIMATERIAL], STRINGDATA, null, null, STRINGDATA, sdf, f);
		indexOfBeginRow++;
		row = sheet.createRow(indexOfBeginRow);
		
		cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		cell.setCellValue("Platit mzdy");
		cell.setCellStyle(style);
		cell = row.createCell(1, Cell.CELL_TYPE_STRING);
		cell.setCellValue("ANO - NE");
		cell.setCellStyle(style);
		cell = row.createCell(2, Cell.CELL_TYPE_BLANK);
		cell.setCellStyle(style);
		cell = row.createCell(3, Cell.CELL_TYPE_BLANK);
		cell.setCellStyle(style);
		cell = row.createCell(4, Cell.CELL_TYPE_BLANK);
		cell.setCellStyle(style);
		indexOfBeginRow++;
		
		//volna radka
		indexOfBeginRow++;
		
		//Format data
		for(int i = 0; i < 5 ; i++){ // mam 4 sloupce
			sheet.autoSizeColumn(i);
		}
		// vytvo�it mezeru
		sheet.setColumnWidth(2, 2000);
		
		return indexOfBeginRow;
	}
	
	private static void addRowValuesVypisZmetekMzdy(Row row,
			String nazevHodnoty1, String hodnota1, int isNumber1,
			String nazevHodnoty2, String hodnota2, int isNumber2,
			SimpleDateFormat sdf, HSSFFont f) throws ParseException {
		
		Cell cell;
		cell = row.createCell(0, Cell.CELL_TYPE_STRING);
		cell.setCellValue(nazevHodnoty1);
		cell.getCellStyle().setFont(f);
		if (isNumber1 == NUMERICDATA){
			cell = row.createCell(1, Cell.CELL_TYPE_NUMERIC);
			cell.setCellValue(Double.parseDouble(hodnota1));
		} else  if (isNumber1 == DATEDATA){ // datum
			cell = row.createCell(1);
			cell.setCellValue(sdf.parse(hodnota1));
		} else {
			cell = row.createCell(1, Cell.CELL_TYPE_STRING);
			cell.setCellValue(hodnota1);
		}
		cell.getCellStyle().setFont(f);
		// mezera
		cell = row.createCell(2, Cell.CELL_TYPE_BLANK);
		cell.getCellStyle().setFont(f);
		
		cell = row.createCell(3, Cell.CELL_TYPE_STRING);
		cell.setCellValue(nazevHodnoty2);
		cell.getCellStyle().setFont(f);
		if (isNumber2  == NUMERICDATA){
			cell = row.createCell(4, Cell.CELL_TYPE_NUMERIC);
			cell.setCellValue(Double.parseDouble(hodnota2));
		} else  if (isNumber2 == DATEDATA){ // datum
			cell = row.createCell(4);
			cell.setCellValue(sdf.parse(hodnota2));
		} else {
			cell = row.createCell(4, Cell.CELL_TYPE_STRING);
			cell.setCellValue(hodnota2);
		}
		cell.getCellStyle().setFont(f);
	}
}
