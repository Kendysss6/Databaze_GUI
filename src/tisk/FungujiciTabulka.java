/*
 * This class is part of the book "iText in Action - 2nd Edition"
 * written by Bruno Lowagie (ISBN: 9781935182610)
 * For more info, go to: http://itextpdf.com/examples/
 * This example only works with the AGPL version of iText.
 */

package tisk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.table.TableModel;

import sablony.errorwin.ExceptionWin;
import app.LoginWindow;
import app.MainFrame;

import com.itextpdf.text.Chapter;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class FungujiciTabulka {

    /** The resulting PDF file. */
    public static final String RESULT = "E:\\databaze-vyborny material\\Pdfzkousky\\MyFirstTable.pdf";
    //public static final String RESULT = "C:\\1Ondrej_Havlicek\\Skola\\PdfZkousky\\MyFirstTable.pdf";
    /** nazvy sloupcu tabulky     */
    private static final String [] hlavicka = {"Z\u00E1kazn\u00EDk", "N\u00E1zev", "\u010C\u00EDslo modelu", "Po","�t", "St", "�t", "P�", "Celkem"};
    private static final String [] hlavicka2 = {"Materi�l", "Materi�l 2", "Hmotnost", "Term�n","Objed.", "Odl. � zmet.", "Norma", "Norma celkem"};
    private static final int [] colSpan =  {2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,3,3};
    private MainFrame hlavniOkno;
    private Document document;
    
    /**
     * hlavicky [0] = Vypis odlitku v kg, k�
     * hlavicky [1] = 
     * hlavicky [2] = 
     * hlavicky [3] = zakladni liti
     * hlavicky [4] = zakladni liti
     * hlavicky [5] = zakladni liti
     * hlavicky [6] = zakladni liti
     * hlavicky [0] = zakladni liti
     */
    private final String [][][] hlavicky= {
    		{ // vpiy odlitku v kg kc
    			{"Formovna", "Cena v K�", "Hmotnost"}
    		},
    		{ //vypis zpozdeni vyroby
    			{"Term�n expedice", "ID zak�zky", "Jm�no z�kazn�ka", "��slo modelu", ""},
    			{" ","Objedn�no", "Odlito", "Vy�i�t�no", "Expedov�no"}
    		},
    		{//Vypis zakazek s terminem exp. v tydnu
    			{"Term�n expedice", "ID zak�zky", "Z�kazn�k", "N�zev modelu", "��slo modelu", "��slo objedn�vky"},
    			{"D�l�� term�n", "Hmotnost","Objedn�no kus�", "Odlito", "Vy�i�t�no", "Expedov�no"}
    		}, 
    		{ // vypis polozek s odhad hmot
    			{"ID zak�zky", "ID modelu", "Jm�no z�kazn�ka", "��slo modelu", "Jm�no modelu", "Hmotnost"}
    		},
    		{//mzdy slevacu
    			{"ID zak�zky", "Jm�no z�kazn�ka", "��slo modelu", "Norma", "Formovna", "Odlito", "Vyrobeno"}
    		},
    		{// (denni) vypis odlitych kusu
    			{"Datum lit�", "ID zak�zky", "Jm�no z�kazn�ka", "��slo modelu", "Norma", "Odlito", "Materi�l", "Vlastn� materi�l"}
    		}, 
    		{ // Vypis vy�i�t�n�ch kusu za obdob�
    			{"Datum �i�t�n�", "ID zak�zky", "Jm�no z�kazn�ka", "��slo modelu", "Norma", "Odlito", "Cena celkem", "Kg celkem"} 
    		},
    		{// inventura rozpracovane vyroby
    			{"ID zak�zky", "Jm�no z�kazn�ka", "��slo modelu", "Po�et kus�", "K� celkem", "Hmotnost"}
    		},
    		{// vypis Expedovanych kusu od do
    			{""}
    		},    		
    		{// vypis skladu
    			{"ID zak�zky", "Jm�no z�kazn�ka", "��slo modelu", "Cena za sklad.", "Term�n expedice", "Na sklad�", "Expedov�no"}
    		},
    		{ // zakla lici plan
    			{"Z\u00E1kazn\u00EDk", "N\u00E1zev", "\u010C\u00EDslo modelu", "Po","�t", "St", "�t", "P�", "Celkem"}
    		},
    		{ // pokrocili lici plan
    			{"Z\u00E1kazn\u00EDk", "N\u00E1zev", "\u010C\u00EDslo modelu", "Po","�t", "St", "�t", "P�", "Celkem"},
    			{"Materi�l", "Materi�l 2", "Hmotnost", "Term�n","Objed.", "Odl. � zmet.", "Norma", "Norma celkem"}
    		}
    		
    };
    
    private final int [][][] colspans= {
    		{
    			{1,1,1}
    		},
    		{
    			{1,1,2,2,1},
    			{3,1,1,1,1}
    		},
    		{
    			{1,1,1,1,1,1},
    			{1,1,1,1,1,1}
    		},
    		{
    			{1,1,1,1,1,1}
    		}, 
    		{
    			{1,1,1,1,1,1,1}
    		}, 
    		{
    			{1,1,1,1,1,1,1,1}
    		},
    		{
    			{1,1,1,1,1,1,1,1}
    		},
    		{
    			{1,1,1,1,1,1}
    		},
    		{//vypis Expedice
    			
    		},
    		{
    			{1,1,1,1,1,1,1}
    		}
    };
    /**
     *  int [][] pole. ���ky tabulek, kter� se tisknou do PDF souboru, d�lky jednotliv�ch pol� jsou stejn�
     *  jako sou�et prvk� v poli colSpan na stejn�m ��slu ��dku 
     *  (colspan je 3D, toto plat� pro kter� koliv ��dek z vybran�ho 2D pole).
     */
    private final int [][] widthsAll = {
    		{1,1,1},
    		{7,7,5,5,5,5,5},
    		{4,3,4,4,4,4},
    		{1,1,2,2,2,1},
    		{2,4,4,2,2,2,2},
    		{3,3,4,4,3,2,3,3},
    		{6,5,7,7,5,3,6,6},
    		{1,1,1,1,1,1},
    		{},
    		{7,12,12,9,10,6,8}
    };
    
    /**
     * Jm�na sloupc�, pod kter�mi se zobrazuj� ��sla. A ja mam nastaveno, �e ��sla se zarovn�vaj� do prava. 
     * Proto se i tyto n�zvy budou v PDF zarovnavat do prava.
     */
    private final String [] numberColumsNames = {"Objedn�no", "Odlito", "Vy�i�t�no", "Expedov�no", "Hmotnost", "Objedn�no kus�", "Norma"};
    
    public static final int vypisOdlitkuVKgKc = 0;
    public static final int zpozdeniVyroby = 1;
    public static final int vypisSTerminExp = 2;
    public static final int vypisOdhadHmot = 3;
    public static final int mezdySlevacu = 4;
    public static final int vypisOdlitkuOdDo = 5;
    public static final int vypisVycistenychOdDo = 6;
    public static final int inventuraVyroby = 7;
    public static final int vypisExpediceOdDo = 8;
    public static final int vypisSkladu = 9;
    
    public static final int zaklLitiPlan = 10;
    public static final int liciPlan = 11;
    
    
    public FungujiciTabulka(MainFrame hlavniOkno){
    	this.hlavniOkno = hlavniOkno;
    	/*
		 *Vytvo�im dokument 
		 */
    	this.document = new Document(PageSize.A4, 20,20,30,20);
    }
   
    /**
     * Metoda, kter� vytvo�� PDF z dan�ho table modelu, do ur�it� slo�ky podle typ PDF.
     * @param jmenoSouboru jak se dan� pdf bude jmenovat
     * @param model model, ze kter�ho budeme br�t data
     * @param typ typ PDF do kter�ho exportujeme tabulku
     * @throws IOException 
     * @throws DocumentException
     */
    public void createPdfTable(String jmenoSouboru, javax.swing.table.TableModel model, int typ) throws IOException, DocumentException {
    	/*
    	 * vytvo�ime slo�ku s tabulkama
    	 */
    	String adresa = adresaPDF(typ);
    	if(adresa == null){
    		throw new DocumentException("Spatny typ dokumentu");
    	}
    	String[] slozky = adresa.split("/");
    	File file;
    	adresa = "";
    	for(int i = 0; i < slozky.length; i++){
    		file = new File(adresa+slozky[i]);
    		adresa += slozky[i]+"/";
    		file.mkdir();
    	}
		// adresa souboru
		String adresar = adresa+jmenoSouboru+".pdf";
		/*
		 * Vytvo�im font
		 */
		BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1250, BaseFont.EMBEDDED);
    	Font f = new com.itextpdf.text.Font(bf,12);
    	/*
    	 * Vytvo�im zapisovac (PdfWriter)
    	 */
    	PdfWriter writer = null;
    	try{
    		writer = PdfWriter.getInstance(document, new FileOutputStream(adresar));
    	} catch(FileNotFoundException e){
    		System.out.println(e.getLocalizedMessage()+" LooooooLL");
    		JOptionPane.showMessageDialog(this.hlavniOkno, "Zav�ene v�echna PDF, jinak je chyba v "+this.getClass().getName());
    		return;
    	}
    	
    	/*
    	 * Pridam listener, ktery bude pridava cisla stranek
    	 */
    	EventPageListener pom = new EventPageListener();
        writer.setPageEvent(pom);
        /*
         * Zjistim si parametry tabulky dle jej�ho typu
         */
        String [][] jmenaSloupcu = getJmenaSloupcu(typ);
        int [] widths = getWidths(typ);
        int [][] colspan = getColspan(typ);
        
        /*
         * Otevru a pracuju s dokumentem, tzn p�id�m tabulku
         */
        document.open();
        /**
         * Je�t� p�idat n�jaky nadpis nesmim zapomenout
         */
        
        
        /*
         * Pridam tabulku
         */
        boolean pridano = 
        		this.addTable(jmenaSloupcu, widths, colspan, model, f);
    	if(!pridano){
    		JOptionPane.showMessageDialog(this.hlavniOkno, "PDF vytvo�eno, ale s chybami. Chyba v "+this.getClass().getName());
    	}
    	document.close();
    }
    
   /**
    * 
    * @param model
    * @param typ 
    * @throws IOException
    * @throws DocumentException
    */
    public boolean createPdf(String jmenoSouboru, javax.swing.table.TableModel model, int typ) throws IOException, DocumentException {
    	String adresa = "./tabulky";
    	File file = new File(adresa);
		file.mkdir();
		
    	
    	String adresar = adresa+"/"+jmenoSouboru+".pdf";
		
    	BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1250, BaseFont.EMBEDDED);
    	Font f = new com.itextpdf.text.Font(bf);
    	// step 1
    	Document document = new Document(PageSize.A4, 20,20,30,20);
        // step 2
    	PdfWriter writer = null;
    	try{
    		writer = PdfWriter.getInstance(document, new FileOutputStream(adresar));
    	} catch(FileNotFoundException e){
    		System.out.println(e.getLocalizedMessage()+" LooooooLL");
    		JOptionPane.showMessageDialog(this.hlavniOkno, "Zav�ene v�echna PDF, jinak je chyba v "+this.getClass().getName());
    		return false;
    	}
    	
        // pridat listener
    	EventPageListener pom = new EventPageListener();
        writer.setPageEvent(pom);
        // step 3
        
        
        document.open();
        
        // step 4
       
        Paragraph p = new Paragraph("P��li� �lu�ou�k� k�� �p�l ��belsk� �dy", f);
        p.setSpacingAfter(5);
        document.add(p);
        if(typ == zaklLitiPlan){ // zakladni lici plan
        	document.add(createTableZaklPlan(document, model));
        } else if(typ == liciPlan){ //Normal lici plan
        	document.add(createTablePlanovani(document, model));
        }
       // document.add(createTableZaklPlan(document, null)); // createTableZaklPlan  createTablePlanovani
        // step 5
        document.close();
        return true;
    }
    
    /**
     * Creates our first table
     * @return our first table
     * @throws DocumentException 
     */
    public PdfPTable createTablePlanovani(Document document, javax.swing.table.TableModel model) throws DocumentException {
    	// a table with three columns
    	int colCount = 12;
        PdfPTable table = new PdfPTable(colCount);
        table.setWidthPercentage(100);
        table.setHeaderRows(2);
        int [] widths = {18,18,24,24,23,23,7,7,7,7,7,14};
        if(widths.length != colCount)System.out.println("Je vice sloupcu nez je zadano ���ek");
        else table.setWidths(widths);
        // the cell object
        PdfPCell cell;
        // we add a cell with colspan 3
        
        
        com.itextpdf.text.Font f = null;
        
        try {
        	BaseFont bf;
			bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1250, BaseFont.EMBEDDED);
			f = new com.itextpdf.text.Font(bf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        for(int i = 0; i < hlavicka.length; i++){
        	cell = new PdfPCell(new Phrase(hlavicka[i], f));
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(colSpan[i]);
            table.addCell(cell);
        }
          
        
        for(int i = 0; i < 6; i++){
        	cell = new PdfPCell(new Phrase(hlavicka2[i], f));
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            table.addCell(cell);
        }
        for(int i = 6; i < hlavicka2.length; i++){
        	cell = new PdfPCell(new Phrase(hlavicka2[i], f));
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(3);
            table.addCell(cell);
        }
        
        
        // we add the four remaining cells with addCell()
        int r = 100; //pocet Radku
        String [] prom = new String [17];
        for(int i = 0; i < r; i++){
        	for(int j = 0; j < prom.length; j++){
        		prom[j] = "cell "+i;
        	}
        	addRowLiciPlanP(document, table, f, prom);
        	//table.addCell(""+i);
        }
        return table;
    }
    
    public PdfPTable createTableZaklPlan(Document document, javax.swing.table.TableModel model) throws DocumentException {
    	// a table with three columns
    	int colCount = 12;
    	
        PdfPTable table = new PdfPTable(colCount);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        int [] widths = {18,18,24,24,23,23,7,7,7,7,7,14};
        if(widths.length != colCount)System.out.println("Je vice sloupcu nez je zadano ���ek");
        else table.setWidths(widths);
        // the cell object
        PdfPCell cell;
        // we add a cell with colspan 3
        
        
        com.itextpdf.text.Font f = null;
        
        try {
        	BaseFont bf;
			bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.CP1250, BaseFont.EMBEDDED);
			f = new com.itextpdf.text.Font(bf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        for(int i = 0; i < hlavicka.length; i++){
        	cell = new PdfPCell(new Phrase(hlavicka[i], f));
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(colSpan[i]);
            table.addCell(cell);
        }        
        
        // we add the four remaining cells with addCell()
        int r = model.getRowCount(); //pocet Radku
        String [] prom = new String [9];
        for(int i = 0; i < r; i++){
        	for(int j = 0; j < prom.length; j++){
        		prom[j] = (String) model.getValueAt(i, j);
        	}
        	addRowLiciPlanP(document, table, f, prom);
        	//table.addCell(""+i);
        }
        return table;
    }
    
    /**
     * Metoda pro p�id�n� tabulky do PDF dokumentu (knihovna itext)
     * @param jmenaSloupcu String [][] pole, stejne rozm�ry jako [][]colspan jinak DocumentException
     * @param widths int [] pole stejn� d�lka jako nejdel�� pole z jmenaSloupcu
     * @param colspan int [][] pole stejn� rozm�ry jako String [][] jmenaSloupcu
     * @param model Table model z kter�ho budeme tahat data
     * @param document Dokument do kter�ho budeme p�id�vat tabulku
     * @return true pokud se poda�ilo tabulu p�idat, jinak false
     * @throws DocumentException
     */
    private boolean addTable(String [][] jmenaSloupcu, int [] widths, int [][]colspan, javax.swing.table.TableModel model, com.itextpdf.text.Font f) throws DocumentException{
    	int colCount = widths.length;
    	boolean vseOk = true;
    	if(colspan.length != jmenaSloupcu.length){
    		throw new DocumentException("Colspan ruzna delka nez jmena sloupcu");
    	}
    	for(int i = 0; i < jmenaSloupcu.length && vseOk; i++){
    		/*if(colCount < jmenaSloupcu[i].length){
    			colCount = jmenaSloupcu[i].length;
    		}*/
    		if(jmenaSloupcu[i].length != colspan[i].length){
    			vseOk = false;
    		}
    	}
    	if(!vseOk){
    		throw new DocumentException("colspan a jmena sloupc� nejsou stejn� velk�");
    	}
    	if(colCount <= 0){
    		throw new DocumentException("Po�et sloupc� je men�� ne� nula");
    	}
    	 if(widths.length != colCount){
         	throw new DocumentException("Je v�ce sloupc� nez je z�dano ���ek");
         }
    	
    	
    	int countHeaderRows = jmenaSloupcu.length;
        PdfPTable table = new PdfPTable(colCount);
        table.setWidthPercentage(100);
        table.setHeaderRows(countHeaderRows);
        table.setWidths(widths);
        
        /*
         * Pridam hlavicku do tabulky
         */
        PdfPCell cell;
		for (int i = 0; i < jmenaSloupcu.length; i++) {
			for (int j = 0; j < jmenaSloupcu[i].length; j++) {
				cell = new PdfPCell(new Phrase(jmenaSloupcu[i][j], f));
				// cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
				// cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
				if (isNumberColumn(jmenaSloupcu[i][j])) {
					cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
				}
				cell.setColspan(colspan[i][j]);
				table.addCell(cell);
			}
		}
        
        /*
         * Nactu data z table model
         */
        int countRows = model.getRowCount();
        String [][] totalRow = nactiDataPolozky(model, colspan, 0);
        for(int i = 0; i < countRows; i++){
        	totalRow = nactiDataPolozky(model, colspan, i);
        	addTotalRow(table, f, totalRow, colspan);
        }
        
        this.document.add(table);
        
        return true;
    }
    
    private void addRowLiciPlanP(Document document, PdfPTable table, com.itextpdf.text.Font f, String [] parametres){
    	PdfPCell cell;
    	for(int i = 0; i < parametres.length; i++){
    		cell = new PdfPCell(new Phrase(parametres[i], f));
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); 
            cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE);
            cell.setColspan(colSpan[i]);
            cell.setBorderWidth(0.2f);
            if(i < 9){
            	cell.setBorderWidthTop(1.5f);
            }            
            table.addCell(cell);
   
    	}
    }
    
   /**
    * Metoda slou��c� pro na�ten� jedn� polo�ky z table modelu (jedna polo�ka mu�e m�t vic �adku)
    * @param tab TableModel ze ktereho tahame data
    * @param colspan slou�� pouze k tomu abych v�d� jak� jsou rozm�ry jednoho aboslutn�ho ��dku
    * @param radek z kter�ho pr�v� vyb�r�me data
    * @return
    */
    private String [][] nactiDataPolozky(TableModel tab, int [][] colspan, int radek){
    	int sloupecTabulky = 0;
    	String pom = null;
    	String [][] data = new String [colspan.length][];
		for (int i = 0; i < colspan.length; i++) {
			data[i] = new String[colspan[i].length];
			for (int j = 0; j < colspan[i].length; j++) {
				pom = (String) tab.getValueAt(radek, sloupecTabulky);
				sloupecTabulky++;
				data[i][j] = pom;
			}
		}
    	return data;
    }
    
    /**
     * P�ida absolutn� �adu do tabulky. Absolutn� je my�leno,
     *  �e pokud se data rozlo�� na v�ce ��dek, tak p�id� tolik ��dek kolik je jedna polo�ka.
     * @param table tabulka, dokter� p�id�v�me absolutn� �adu
     * @param f font p�sma (v�etn� kodov�n�)
     * @param data data ve String poli
     * @param colspan ���ka jednotliv�ch bun�k
     */
    private void addTotalRow(PdfPTable table, com.itextpdf.text.Font f, String [][] data, int [][] colspan){
    	PdfPCell cell;
    	for(int i = 0; i < data.length; i++){
			for (int j = 0; j < data[i].length; j++) {
				cell = new PdfPCell(new Phrase(data[i][j], f));
				if( i == 0){
					cell.setBorderWidthTop(1.5f);
				} 
				if(j == 0){
					cell.setBorderWidthLeft(1.3f);
				} else if(j == data[i].length - 1){
					cell.setBorderWidthRight(1.3f);
				}
				try{
					if(data[i][j] != null){
						Double.parseDouble(data[i][j]);
					}
					cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
				} catch(NumberFormatException e1){
					cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
				}
				
				cell.setColspan(colspan[i][j]);
				//cell.setBorderWidth(0.2f);
				table.addCell(cell);
			}
    	}
    }
    
    private boolean isNumberColumn(String jmeno){
    	for(int i = 0; i < numberColumsNames.length; i++){
    		if(jmeno.equalsIgnoreCase(numberColumsNames[i])){
    			return true;
    		}
    	}
    	return false;
    }
    
    private String adresaPDF(int typPdf){
		switch (typPdf) {
		case 0:
			return "Vypisy/Odlitky_kg_kc";
		case 1:
			return "Vypisy/Zpozdena_vyroba";
		case 2:
			return "Vypisy/Dle_terminu_expedice";
		case 3:
			return "Vypisy/Polozky_s_odhad_hmot";
		case 4:
			return "Vypisy/Mzdy_slevacu";
		case 5:
			return "Vypisy/Odlite_kusy_od_do";
		case 6:
			return "Vypisy/Vycistene_kusy_od_do";
		case 7:
			return "Vypisy/Inventura_rozprac_vyroby";
		case 8:
			return "Vypisy/Expedovane_kusy_od_do";
		case 9:
			return "Vypisy/Vypis_skladu";
		default:
			JOptionPane.showMessageDialog(hlavniOkno, "�patn� typ pdf Fungujjici tabulka adresaPDF()");
			break;
		}
		return null;
    }
    
    private  String [][] getJmenaSloupcu(int typPdf){
    	return this.hlavicky[typPdf];
    }
    
    private int [] getWidths(int typPdf){
    	return this.widthsAll[typPdf];
    }
    
    private int [][] getColspan(int typPdf){
    	return this.colspans[typPdf];
    }
}
