package tiskExcel;

import javax.swing.table.TableModel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class TableToExcel {
	
	/**
	 * Vytvo�� t��du, kter� vytvo�� .xls soubor do dan� slo�ky. Table model a ��slo v�pisu spolu souvis�, jeloko�
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
	 */
	private void export(TableModel model, String path, int cisloVypis){
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = this.createSheet(wb, cisloVypis);
		this.insertData(model, sheet, cisloVypis);
		
	}
	
	/**
	 * Metoda pro vlo�en� dat do tabulky, je�t� bych m�l zv�it �e p�idam parametr pro mergovan� bun�k.
	 * Je�t� mus�m vymyslet jak. N�jaky jednoduhcy �e�en�. :)
	 * @param model ze kter�ho budeme ��st data
	 * @param sheet do kter�ho budeme data vkl�dat
	 * @param cisloVypis ��slo vypisu
	 */
	private void insertData(TableModel model, Sheet sheet, int cisloVypis){
		
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
