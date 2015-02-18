package tiskExcel;

import java.io.FileOutputStream;
import java.io.IOException;

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

public class TableToExcel {
	
	/**
	 * Vytvo�� t��du, kter� vytvo�� .xls soubor do dan� slo�ky. Table model a ��slo v�pisu spolu souvis�, jeliko�
	 * podle ��sla v�pisu se vybere dan� �ablona, do kter� se budou data vkl�dat.
	 * @param model ze kter�ho �erp�me data
	 * @param path kam chceme ulo�it soubor
	 * @param cisloVypisu ��slo v�pisu (��slo �ablony, kterou pou�ijeme pro soubor xls)
	 * @throws Exception vyhod� chybu, pokud n�co nesouhlas�
	 */
	public TableToExcel(TableModel model, String path, int cisloVypisu) throws Exception{
		this.export(model, path, cisloVypisu);
	}
	
	/**
	 * Metoda, kde d�je hlavn� algorytmus. Zde se v�e p�ev�d�
	 * @param model model, ze kret�ho �erp�me data
	 * @param path kam chceme ulo�it .xls soubor (pokud mo�no relativn�)
	 * @param cisloVypis druh vypisu, podle kter�ho p�izp�sob�me .xls soubor
	 * @throws Exception 
	 */
	private void export(TableModel model, String path, int cisloVypis) throws Exception{
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = this.createSheet(wb, cisloVypis);
		
		Footer footer = sheet.getFooter();
		footer.setRight("Page " + HeaderFooter.page() + " of "
				+ HeaderFooter.numPages());
		
		sheet.setPrintGridlines(true);
	    
		sheet.setMargin(Sheet.BottomMargin, 0.5);
		sheet.setMargin(Sheet.TopMargin, 0.5);
		sheet.setMargin(Sheet.LeftMargin, 0.5);
		sheet.setMargin(Sheet.RightMargin, 0.5);
		
		sheet.setMargin(Sheet.HeaderMargin, 0.5);
		sheet.setMargin(Sheet.FooterMargin, 0.5);
		
		sheet.setRepeatingRows(CellRangeAddress.valueOf("1:1"));
		
		
		this.insertData(model, sheet, cisloVypis);
		
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
		

		
		// Write the output to a file
		FileOutputStream fileOut = new FileOutputStream(path);
		wb.write(fileOut);
		fileOut.close();
	}
	
	/**
	 * Metoda pro vlo�en� dat do tabulky, je�t� bych m�l zv�it �e p�idam parametr pro mergovan� bun�k.
	 * Je�t� mus�m vymyslet jak. N�jaky jednoduhcy �e�en�. :)
	 * @param model ze kter�ho budeme ��st data
	 * @param sheet do kter�ho budeme data vkl�dat
	 * @param cisloVypis ��slo vypisu
	 * @throws Exception 
	 */
	private void insertData(TableModel model, HSSFSheet sheet, int cisloVypis) throws Exception{
		HSSFRow fRow = (HSSFRow) sheet.getRow(sheet.getFirstRowNum());
		int colCount = 0;
		for(Cell cell : fRow){
			colCount++;
		}
		System.out.println("celkem bunek: "+colCount);
		if(model.getColumnCount() != colCount){
			throw new Exception("Spatny pocet sloupcu v modelu nebo �ablon�");
		}
	}
	
	/**
	 * Metoda pro vytvo�en� t��dy Sheet. Ud�l� mu jm�no podle ��sla v�pisu.
	 * @param wb do kter�ho vytvo�� Sheet
	 * @param cisloVypisu ��slo v�pisu
	 * @return Sheet s p�id�len�m jm�nem
	 */
	private HSSFSheet createSheet(HSSFWorkbook wb, int cisloVypisu){
		HSSFSheet sheet = wb.createSheet("new sheet");
		return sheet;
	}

}
