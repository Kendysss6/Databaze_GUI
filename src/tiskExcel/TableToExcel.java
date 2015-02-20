package tiskExcel;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
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
	
	private JFrame hlavniOkno;
	
	/**
	 * Vytvo�� .xls soubor do p�edem dan� slo�ky s jedno��dkovou hlavi�kou.
	 * (Doporu�en�: �daje by se m�li vej�t na A4 na v��ku)
	 * @param hlavniOkno pouzit� na zobrazen� chyby (metoda export())
	 * @param model ze kter�ho �erp�me data. Mus� b�t <code>QueryTableModel</code>
	 * @param name jm�no souboru bez koncovky
	 * @param cisloExportu ��slo exportu (��slo �ablony, kterou pou�ijeme pro soubor xls)
	 * @throws Exception vyhod� chybu, pokud n�co nesouhlas�
	 */
	public static void exportToExcel(JFrame hlavniOkno, TableModel model, String name, int cisloExportu) throws Exception{
		new TableToExcel(hlavniOkno, model, name, cisloExportu);
	}
	
	/**
	 * Vytvo�� t��du, kter� vytvo�� .xls soubor do p�edem dan� slo�ky s jedno��dkovou hlavi�kou.
	 * (Doporu�en�: �daje by se m�li vej�t na A4 na v��ku)
	 * @param hlavniOkno pouzit� na zobrazen� chyby (metoda export())
	 * @param model ze kter�ho �erp�me data. Mus� b�t <code>QueryTableModel</code>
	 * @param name jm�no souboru bez koncovky
	 * @param cisloExportu ��slo exportu (��slo �ablony, kterou pou�ijeme pro soubor xls)
	 * @throws Exception vyhod� chybu, pokud n�co nesouhlas�
	 */
	public TableToExcel(JFrame hlavniOkno, TableModel model, String name, int cisloExportu) throws Exception{
		this.hlavniOkno = hlavniOkno;
		this.export((QueryTableModel) model, name, cisloExportu);
	}
	
	/**
	 * Metoda, kde d�je hlavn� algoritmus. Zde se v�e p�ev�d�
	 * @param model model, ze kret�ho �erp�me data
	 * @param name nazev souboru bez koncovky
	 * @param cisloExportu druh vypisu, podle kter�ho p�izp�sob�me .xls soubor
	 * @throws Exception 
	 */
	private void export(QueryTableModel model, String name, int cisloExportu) throws Exception{
		HSSFWorkbook wb = new HSSFWorkbook();
		String [] atr = this.getAtributes(cisloExportu);
		HSSFSheet sheet = wb.createSheet(atr[0]);
		
		//set header
		Header header = sheet.getHeader();
		header.setCenter(HSSFHeader.font("Stencil-Normal", "Italic")
				+ HSSFHeader.fontSize((short) 14)
				+ "Center Header");
		//header.setLeft("Left Header");
		/*header.setRight(HSSFHeader.font("Stencil-Normal", "Italic")
				+ HSSFHeader.fontSize((short) 16)
				+ "Right w/ Stencil-Normal Italic font and size 16");
		*/
		
		//number of pages
		Footer footer = sheet.getFooter();
		footer.setRight("Page " + HeaderFooter.page() + " of " + HeaderFooter.numPages());
		//set visibile grid lines on printed pages
		sheet.setPrintGridlines(true);
	    
		sheet.setMargin(Sheet.BottomMargin, 0.5);
		sheet.setMargin(Sheet.TopMargin, 0.6);
		sheet.setMargin(Sheet.LeftMargin, 0.5);
		sheet.setMargin(Sheet.RightMargin, 0.5);
		
		sheet.setMargin(Sheet.HeaderMargin, 0.1);
		sheet.setMargin(Sheet.FooterMargin, 0.3);
		//insert data
		this.insertData(atr[1], model, sheet, cisloExportu);
		//set First row as header at all printed pages
		sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));	
	

		
		// Write it into the output to a file
		
		File f = new File("./vypisy");
		try {
			if (f.mkdir()) {
				//System.out.println("Directory Created");
			} else {
				//System.out.println("Directory is not created");
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(hlavniOkno, "Jm�no z�kazn�ka je pr�zdn�");
		}

		FileOutputStream fileOut = new FileOutputStream("./vypisy/"+name+".xls");
		wb.write(fileOut);
		wb.close();
		fileOut.close();
	}
	
	/**
	 * Metoda pro vlo�en� dat do tabulky, je�t� bych m�l zv�it �e p�idam parametr pro mergovan� bun�k.
	 * Je�t� mus�m vymyslet jak. N�jaky jednoduhcy �e�en�. :)
	 * @param nadpis napis v tisku
	 * @param model ze kter�ho budeme ��st data
	 * @param sheet prazdny sheet do kter�ho budeme data vkl�dat
	 * @param cisloExportu ��slo exportu
	 * @throws Exception
	 */
	private void insertData(String nadpis, QueryTableModel model, HSSFSheet sheet, int cisloExportu) throws Exception{
		Row row = sheet.createRow(0);
		Cell cell = null;
		//insert Column name
		for(int i = 0; i < model.getColumnCount() -1 ; i++){//mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
			cell = row.createCell(i);
			cell.setCellValue(model.getColumnName(i));
		}
		//detect data format
		boolean [] isNumber = null;
		if(model.getRowCount() > 0){
			isNumber = new boolean [model.getColumnCount()-1];
			for(int j = 0; j < model.getColumnCount() -1 ; j++){ //mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
				try {
					Double.parseDouble((model.getValueAt(0, j)));
					isNumber[j] = true;
				} catch (NumberFormatException nfe) {
					isNumber[j] = false;
				}
			}
		}
		//insert data
		for(int i = 1; i < model.getRowCount() + 1; i++){
			row = sheet.createRow(i);
			for(int j = 0; j < model.getColumnCount() -1 ; j++){ //mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
				cell = row.createCell(j);
				if(isNumber[j]){
					cell.setCellValue(Double.parseDouble((model.getValueAt(i-1, j))));
				} else {
					cell.setCellValue(model.getValueAt(i-1, j));
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
	 * @return String [] o 3 prvc�ch nebo null pokud neexistuje
	 */
	private String [] getAtributes(int cisloExportu){
		String [] atr = {"prazdnyatr","prazdnyatr","prazdnyatr"};
		switch(cisloExportu){
		case 0:
			String [] atr1 = {"Stav neuzav�en�ch zak�zek", "Stav neuzav�en�ch zak�zek", ".//"};
			atr = atr1;
			break;
		case 2:
			break;
		}
		return atr;		
	}
}
