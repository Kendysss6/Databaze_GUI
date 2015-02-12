package tiskExcel;

import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class TableToExcel {
	
	public TableToExcel(TableModel model, String path, int cisloVypisu){
		this.export(model, path, cisloVypisu);
	}
	
	/**
	 * Metoda, kde d�je hlavn� algorytmus. Zde se v�e p�ev�d�
	 * @param model model, ze kret�ho �erp�me data
	 * @param path kam chceme ulo�it .xls soubor (pokud mo�no relativn�)
	 * @param cisloVypis druh vypisu, podle kter�ho p�izp�sob�me .xls soubor
	 */
	private void export(TableModel model, String path, int cisloVypis){
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = this.createSheet(wb, cisloVypis);
		this.insertData(model, sheet);

	}
	
	/**
	 * Metoda pro vlo�en� dat do tabulky, je�t� bych m�l zv�it �e p�idam parametr pro mergovan� bun�k.
	 * Je�t� mus�m vymyslet jak. N�jaky jednoduhcy �e�en�. :)
	 * @param model ze kter�ho budeme ��st data
	 * @param sheet do kter�ho budeme data vkl�dat
	 */
	private void insertData(TableModel model, Sheet sheet){
		
	}
	
	/**
	 * Metoda pro vytvo�en� t��dy Sheet. Ud�l� mu jm�no podle ��sla v�pisu.
	 * @param wb do kter�ho vytvo�� Sheet
	 * @param cisloVypisu ��slo v�pisu
	 * @return Sheet s p�id�len�m jm�nem
	 */
	private Sheet createSheet(Workbook wb, int cisloVypisu){
		Sheet sheet = wb.createSheet("new sheet");
		return sheet;
	}

}
