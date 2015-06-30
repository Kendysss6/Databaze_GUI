package tiskExcel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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

import sablony.tabulka.QueryTableModel;
import app.MainFrame;

public class PlanovaniLitiToExcelTisk {
	
	private MainFrame hlavniOkno;
	private SimpleDateFormat sdf;
	private static String [] columnNamesNotInt = {"��slo modelu", "Cislo_modelu","Jm�no z�kazn�ka","��slo objedn�vky",
		"Jm�no modelu", "Materi�l", "Vlastn� materi�l"};
	private final String [][] columnNamesPlanLiti = {
			{"Z�kazn�k", "Jm�no modelu", "��slo modelu", "Po", "�t", "St", "�t", "P�", "Celk."},
			{"Materi�l", "Materi�l vl.", "Hmotnost", "Term�n", "Objed.", "Odl � zm.", "Norma", "Norma celk."}
			};
	private final int [][] sirkyBunek = {
			{2,2,2,1,1,  1,1,1,1},
			{1,1,1,1,1,  1,3,3}
	};
	
	private final static int TEXT = 1;
	private final static int CISLO = 2;
	private final int [][] dataFormat = {
			{TEXT,TEXT,TEXT,CISLO,CISLO,   CISLO,CISLO,CISLO,CISLO},
			{TEXT,TEXT,CISLO,TEXT,CISLO,   CISLO, CISLO, CISLO}
	};
	
	public static void exportPlanovaniLitiToExcel(MainFrame hlavniOkno, TableModel model,
			String nadpisExt, String name, SimpleDateFormat sdf) throws Exception{
		PlanovaniLitiToExcelTisk proces = new PlanovaniLitiToExcelTisk(hlavniOkno, (QueryTableModel) model, nadpisExt, name, sdf);
	}
	
	private PlanovaniLitiToExcelTisk(MainFrame hlavniOkno, QueryTableModel model,
			String nadpisExt, String name, SimpleDateFormat sdf) throws Exception{
		this.hlavniOkno = hlavniOkno;
		this.sdf = sdf;
		this.export(model, nadpisExt, name);;
		
	}
	
	private void export(QueryTableModel model, String nadpisExt, String name) throws Exception{
		HSSFWorkbook wb = new HSSFWorkbook();
		String [] atr = getAtributes();
		HSSFSheet sheet = wb.createSheet(atr[0]);
		
		// check konstanty
		//checkKonstants(sirkyBunek, columnNamesPlanLiti);	
		
		//set header (nadpis v tisku)
		Header header = sheet.getHeader();
		header.setCenter(HSSFHeader.font("Stencil-Normal", "bold")+ HSSFHeader.fontSize((short) 14)+ atr[1] + nadpisExt);// + nadpisExt);
		
		sheet.getPrintSetup().setPaperSize(PrintSetup.A4_PAPERSIZE);

		sheet.getPrintSetup().setLandscape(false);
		
		
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
		
		HSSFCellStyle obycBorder = wb.createCellStyle();
		obycBorder.setBorderBottom(CellStyle.BORDER_THIN);
		obycBorder.setBorderLeft(CellStyle.BORDER_THIN);
		obycBorder.setBorderRight(CellStyle.BORDER_THIN);
		obycBorder.setBorderTop(CellStyle.BORDER_THIN);
		obycBorder.setFont(font);
		
		// styl bunky s dvojitou hranici dole a 12 pismem
		HSSFCellStyle style = wb.createCellStyle();
		style.setBorderBottom(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderTop(CellStyle.BORDER_DOUBLE);
		style.setFont(font);
		//insert data
		this.insertDataPlanovani(model, sheet, obycBorder, style);
		//set First row as header at all printed pages
		sheet.setRepeatingRows(CellRangeAddress.valueOf("1:2"));	
		
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
			JOptionPane.showMessageDialog(hlavniOkno, "Slo�ka se nepoda�ilo vytvo�it");
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
	 * Metoda pro zji�t�n� atribut� v�pisu. Navr�t� String pole o 3 prvc�ch v po�ad�:
	 * <ol>
	 *  <li>Jm�no sheetu</li>
	 *  <li>Nadpis v tisku pap�ru</li>
	 *  <li>Cestu kam ulo�it (relativn�)</li>
	 * </ol>
	 * @param cisloExportu ��slo exportu, o kter�m chceme zn�t atributy
	 * @return String [] o 3 prvc�ch nikdy <code>null</code>
	 */
	private String [] getAtributes(){
		String[] atr = { "prazdnyatr1", "prazdnyatr2", "prazdnyatr3" };
		atr[0] = "Lic� pl�n";
		atr[1] = "Lic� pl�n pro t�den: ";
		atr[2] = "./lici_plany";
		return atr;
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
	private void insertDataPlanovani(QueryTableModel model, HSSFSheet sheet, HSSFCellStyle obycBorder, HSSFCellStyle stylBunkySDvojitouHranici) throws Exception{
		Row row = null;
		Cell cell = null;
		
		int colCount = 0;
		for(int i = 0; i < sirkyBunek[0].length; i++){
			colCount += sirkyBunek[0][i];
		}
		int cisBunky = 0;
		
		// jmena sloupcu u� je jinak ne� normal
		
		for(int j = 0; j < sirkyBunek.length; j++){
			row = sheet.createRow(j);
			int sloupec = 0;
			for(int i = 0; i < sirkyBunek[j].length; i++){
				for (int m = 0; m < sirkyBunek[j][i]; m++){
					cell = row.createCell(sloupec);
					if(m == 0)cell.setCellValue(columnNamesPlanLiti[j][i]);
					sloupec++;
					cell.setCellStyle(obycBorder);
					if( j % 2 == 0){
						cell.setCellStyle(stylBunkySDvojitouHranici);
					}
					cell.getCellStyle().setAlignment(CellStyle.ALIGN_CENTER);
				}
				sheet.addMergedRegion(new CellRangeAddress(j,j,cisBunky,cisBunky + sirkyBunek[j][i] - 1));
				cisBunky += sirkyBunek[j][i];
			}
			cisBunky = 0;
		}
		
		
		//detect data format
		 // je dan predem
		
		//insert data jinak ne� normal
		int rowIndex = 0;
		for(int r = 0; r < model.getRowCount(); r++){
			int sloupecModeluData = 0;
			for(int j = 0; j < sirkyBunek.length; j++){
				row = sheet.createRow(rowIndex + 2);
				int sloupec = 0;
				cisBunky = 0;
				for(int i = 0; i < sirkyBunek[j].length; i++){
					for(int m = 0; m < sirkyBunek[j][i]; m++){
						cell = row.createCell(sloupec);
						if(m == 0){
							if(dataFormat[j][i] == CISLO){
								if(!model.getValueAt(r, sloupecModeluData).isEmpty()){
									cell.setCellValue(Double.parseDouble(model.getValueAt(r, sloupecModeluData)));
								}
							} else {
								cell.setCellValue(model.getValueAt(r, sloupecModeluData));
							}
							sloupecModeluData++;
						}
						sloupec++;
						cell.setCellStyle(obycBorder);
						if( j % 2 == 0){
							cell.setCellStyle(stylBunkySDvojitouHranici);
						}
					}
					sheet.addMergedRegion(new CellRangeAddress(rowIndex +2,rowIndex +2,cisBunky,cisBunky + sirkyBunek[j][i] - 1));
					cisBunky += sirkyBunek[j][i];					
				}
				rowIndex++;
			}
		}
		
		//Format data
		
		for(int i = 0; i < colCount ; i++){//mam toti� jeden sloupec navic aby se mi srovnali tabulky viz QuerytableModel
			sheet.autoSizeColumn(i,true);
		}
	}
}
