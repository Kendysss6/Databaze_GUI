package tiskExcel;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.usermodel.HeaderFooter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

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
	
	/**
	 * Vytvo�� t��du, kter� vytvo�� .xls soubor do dan� slo�ky. Table model a ��slo v�pisu spolu souvis�, jeliko�
	 * podle ��sla v�pisu se vybere dan� �ablona, do kter� se budou data vkl�dat.
	 * @param model ze kter�ho �erp�me data
	 * @param name jm�no souboru bez koncovky
	 * @param cisloVypisu ��slo v�pisu (��slo �ablony, kterou pou�ijeme pro soubor xls)
	 * @throws Exception vyhod� chybu, pokud n�co nesouhlas�
	 */
	public TableToExcel(TableModel model, String name, int cisloVypisu) throws Exception{
		this.export(model, name, cisloVypisu);
	}
	
	/**
	 * Metoda, kde d�je hlavn� algoritmus. Zde se v�e p�ev�d�
	 * @param model model, ze kret�ho �erp�me data
	 * @param name nazev souboru bez koncovky
	 * @param cisloVypis druh vypisu, podle kter�ho p�izp�sob�me .xls soubor
	 * @throws Exception 
	 */
	private void export(TableModel model, String name, int cisloVypis) throws Exception{
		HSSFWorkbook wb = new HSSFWorkbook();
		String [] atr = this.getAtributes(cisloVypis);
		HSSFSheet sheet = wb.createSheet(atr[0]);
		
		
		Footer footer = sheet.getFooter();
		footer.setRight("Page " + HeaderFooter.page() + " of " + HeaderFooter.numPages());
		
		sheet.setPrintGridlines(true);
	    
		sheet.setMargin(Sheet.BottomMargin, 0.5);
		sheet.setMargin(Sheet.TopMargin, 0.5);
		sheet.setMargin(Sheet.LeftMargin, 0.5);
		sheet.setMargin(Sheet.RightMargin, 0.5);
		
		sheet.setMargin(Sheet.HeaderMargin, 0.5);
		sheet.setMargin(Sheet.FooterMargin, 0.5);
		
		sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));
		
		
			Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);
		cell.setCellValue("Nadpis1");
		cell = row.createCell(1);
		cell.setCellValue("Nadpis2");
		for(int i = 1; i < 100; i++){
			row = sheet.createRow(i);
			cell = row.createCell(0);
			cell.setCellValue("Skoda smrd� "+i);
		}
		
		
		this.insertData(atr[1], model, sheet, cisloVypis);
		
	

		
		// Write it into the output to a file
		
		File f = new File("./vypisy");
		try {
			if (f.mkdir()) {
				//System.out.println("Directory Created");
			} else {
				//System.out.println("Directory is not created");
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	 * @param sheet do kter�ho budeme data vkl�dat
	 * @param cisloVypis ��slo vypisu
	 * @throws Exception
	 */
	private void insertData(String nadpis, TableModel model, HSSFSheet sheet, int cisloVypis) throws Exception{
		HSSFRow fRow = (HSSFRow) sheet.getRow(sheet.getFirstRowNum());
		int colCount = 0;
		for(Cell cell : fRow){
			colCount++;
		}
		System.out.println("celkem bunek: "+colCount);
		colCount = sheet.getRow(0).getPhysicalNumberOfCells();
		System.out.println("celkem bunek: "+colCount);
		colCount = sheet.getRow(0).getLastCellNum();
		System.out.println("celkem bunek: "+colCount);
		if(model.getColumnCount() != colCount){
			throw new Exception("Spatny pocet sloupcu v modelu nebo �ablon�");
		}
	}
	
	/**
	 * Metoda pro zji�t�n� atribut� v�pisu. Navr�t� String pole o 3 prvc�ch v po�ad�:
	 * <ol>
	 *  <li>Jm�no sheetu</li>
	 *  <li>Nadpis v tisku pap�ru</li>
	 *  <li>Cestu kam ulo�it (relativn�)</li>
	 * </ol>
	 * @param cisloVypisu ��slo v�pisu, o kter�m chceme zn�t atributy
	 * @return String [] o 3 prvc�ch nebo null pokud neexistuje
	 */
	private String [] getAtributes(int cisloVypisu){
		String [] atr = null;
		switch(cisloVypisu){
		case 1:
			String [] atr1 = {"Stav neuzav�en�ch zak�zek", "Stav neuzav�en�ch zak�zek", ".//"};
			atr = atr1;
			break;
		case 2:
			break;
		}
		return atr;		
	}
}
