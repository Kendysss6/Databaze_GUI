package tiskExcel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

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
	public final static int liciPlanZakl = 21;
	public final static int liciPlanPlanovaci = 22;
	private JFrame hlavniOkno;
	private String [] columnNamesNotInt = {"��slo modelu", "Cislo_modelu","Jm�no z�kazn�ka","��slo objedn�vky",
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
		String [] atr = this.getAtributes(cisloExportu);
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
		//insert data
		this.insertData(model, sheet, cisloExportu);
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
		
		/*FileOutputStream fileOut = new FileOutputStream("./vypisy/"+name+".xls");
		wb.write(fileOut);
		wb.close();
		fileOut.close();*/
	}
	
	/**
	 * Metoda pro vlo�en� dat do tabulky, je�t� bych m�l zv�it �e p�idam parametr pro mergovan� bun�k.
	 * Je�t� mus�m vymyslet jak. N�jaky jednoduhcy �e�en�. :)
	 * @param model ze kter�ho budeme ��st data
	 * @param sheet prazdny sheet do kter�ho budeme data vkl�dat
	 * @param cisloExportu ��slo exportu
	 * @throws Exception
	 */
	private void insertData(QueryTableModel model, HSSFSheet sheet, int cisloExportu) throws Exception{
		Row row = sheet.createRow(0);
		Cell cell = null;
		//insert Column name
		for(int i = 0; i < model.getColumnCount() -1 ; i++){//mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
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
				//cell.getCellStyle().setWrapText(true);
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
	private String [] getAtributes(int cisloExportu){
		String [] atr = {"prazdnyatr1","prazdnyatr2","prazdnyatr3"};
		/**
		 * Kvuli tomu aby jmena souboru m�li �isla od 1 .. n a ne od 0 tak zv�t��m i o 1
		 * viz metoda vypisyAndTisk(.,.) v HledejListener.java
		 */
		switch(cisloExportu){
		case 0:
			atr[0] = "Stav neuzav�en�ch zak�zek";atr[1] = "Stav neuzav�en�ch zak�zek ke dni ";atr[2] = "./vypisy";
			break;
		case 1:
			atr[0] = "V�pis odlit�ch kus�";atr[1] = "V�pis odlit�ch kus� ke dni ";atr[2] = "./vypisy";
			break;
		case 2:
			atr[0] = "V�pis vy�i�t�n�ch kus� za obdob�";atr[1] = "Vy�i�t�n� kusy od ";atr[2] = "./vypisy";
			break;
		case 3:
			atr[0] = "Mzdy sl�va��";atr[1] = "Mzdy sl�va�� ke dni ";atr[2] = "./vypisy";
			break;
		case 4:
			atr[0] = "V�pis odlitk� v kg-k� za obdob�";atr[1] = "V�pis odlitk� v kg-k� od " ;atr[2] = "./vypisy";
			break;
		case 5:
			atr[0] = "V�pis vyroben�ch kus� za obdob�";atr[1] = atr[0];atr[2] = "./vypisy";
			break;
		case 6:
			atr[0] = "V�pis polo�ek s odhadovanou hmotnost�";atr[1] = "Polo�ky s odhadovou hmotnost� ke dni ";atr[2] = "./vypisy";
			break;
		case 7:
			atr[0] = "V�pis zak�zek s term�nem expedice v dan�m t�dnu";atr[1] = atr[0];atr[2] = "./vypisy";
			break;
		case 8:
			atr[0] = "V�pis expedice zbo�� za obdob�";atr[1] = "Expedice zbo�� od ";atr[2] = "./vypisy";
			break;
		case 9:
			atr[0] = "V�pis zpo�d�n� v�roby ke dni";atr[1] = atr[0];atr[2] = "./vypisy";
			break;
		case 10:
			atr[0] = "Inventura rozpracovan� v�roby";atr[1] = "Rozpracovan� v�roba ke dni ";atr[2] = "./vypisy";
			break;
		case 11:
			atr[0] = "V�pis skladu ke dne�n�mu dni";atr[1] = "Seznam kus� na sklad� ke dni ";atr[2] = "./vypisy";
			break;
		case 12:
			atr[0] = "V�pis zmetk� za obdob�";atr[1] = "V�pis zmetk� od ";atr[2] = "./vypisy";
			break;
		case 13:
			atr[0] = "V�pis vin�k� za obdob�";atr[1] = "V�pis vin�k� od ";atr[2] = "./vypisy";
			break;
		case TableToExcel.liciPlanZakl:
			atr[0] = "Z�kladni lic� pl�n";atr[1] = "Z�kladni lic� pl�n pro t�den: ";atr[2] = "./lici_plany";
			break;
		case TableToExcel.liciPlanPlanovaci:
			atr[0] = "V�pis skladu ke dne�n�mu dni";atr[1] = "Lic� pl�n pro t�den: ";atr[2] = "./lici_plany";
			break;
		}
		return atr;		
	}
	
	/**
	 * Detects whether column is a number or not based on column name or column value.
	 * @param model Table with values a column names
	 * @return boolean field with n-1 columns of the table with true or false values
	 */
	private boolean [] detectDataFormat(QueryTableModel model){
		boolean [] isNumber = null;
		if(model.getRowCount() > 0){
			isNumber = new boolean [model.getColumnCount()-1];
			/**
			 * Moc se tady nechapu ale doposud to funguje tak to nebudu menit
			 */
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
		}
		return isNumber;
	}
}
