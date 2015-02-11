package sablony.tabulka;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Locale;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;
//s
//QueryTableModel.java
//A basic implementation of the TableModel interface that fills out a Vector of
//String[] structures from a query's result set.
//
public class CopyOfQueryTableModel extends AbstractTableModel {

	/**
	 * Verze
	 */
	private static final long serialVersionUID = 1L;
	private Vector<Object> cache; // will hold String[] objects . . .
	
	private ResultSet [] rsf;
	private int colCount;
	private String[] headers;
	
	private SimpleDateFormat sdf;

	public CopyOfQueryTableModel(ResultSet [] rsf) {
		this.rsf = rsf;
		//setMyModel();
	}
	public CopyOfQueryTableModel(ResultSet rs) {
		setMyModel(rs);
	}

	/**
	 * 
	 * @param rs
	 * @param jmenaSloupcu
	 * @throws SQLException
	 * @throws ParseException
	 */
	public CopyOfQueryTableModel(ResultSet rs, String[] jmenaSloupcu, boolean prom)	throws SQLException, ParseException {
		cache = new Vector<Object>();

		// ResultSetMetaData meta = rs.getMetaData();
		colCount = jmenaSloupcu.length;

		// Now we must rebuild the headers array with the new column names
		headers = new String[colCount];
		for (int h = 0; h < colCount; h++) {
			headers[h] = jmenaSloupcu[h];
		}
		
		int cisloPrvnihoTydne = -1, cisloPoslednihoTydne = -1;
		LocalDateTime posledniDatum = null, prvniDatum = null;
		
		cisloPoslednihoTydne = -1;
		Calendar firstDate = Calendar.getInstance();
		Calendar lastDate = Calendar.getInstance();

		// je�t� musim upravit podle toho jakym sloupci bude Datum liti
		java.sql.Date sqlDate;
		java.util.Date utilDate;
		int rozdilTydnu = 0;
		if (rs.first()) {
			sqlDate = rs.getDate(5);
			firstDate.setTime(sqlDate);
			utilDate = sdf.parse(sqlDate.toString());
			cisloPrvnihoTydne = rs.getInt(2);
			prvniDatum = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

			if (rs.last()) {
				sqlDate = rs.getDate(5);
				lastDate.setTime(sqlDate);
				utilDate = sdf.parse(sqlDate.toString());
				cisloPoslednihoTydne = rs.getInt(2);
				posledniDatum = utilDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
			}

			Duration delka = Duration.between(posledniDatum, prvniDatum);
			if (Math.abs(delka.toDays()) > 365) {
				throw new SQLException(
						"Pri pl�nov�ni se vyskytka neo�et�en� chyba. Zak�zka trv� d�le ne� jeden rok");
			}
			int prvCislDne = firstDate.get(Calendar.DAY_OF_WEEK), poslCislDne = lastDate
					.get(Calendar.DAY_OF_WEEK);
			
			firstDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
			lastDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);

			rozdilTydnu = cisloPoslednihoTydne
					+ ((lastDate.get(Calendar.YEAR) - firstDate.get(Calendar.YEAR))	* firstDate.getActualMaximum(Calendar.WEEK_OF_YEAR) + 1) // obvykle bude nula, kdyz nebudeme planovat pres rok
					- cisloPrvnihoTydne;
			
			firstDate.set(Calendar.DAY_OF_WEEK, prvCislDne);
			lastDate.set(Calendar.DAY_OF_WEEK, poslCislDne);

			if (rozdilTydnu > 52) {
				throw new SQLException(
						"Pri pl�nov�ni se vyskytka neo�et�en� chyba. Zak�zka trv� d�le ne� jeden rok");
			}
		}
		// rozdilTydnu++; // po��t�me oba t�dny v�etn�

		Calendar cal = Calendar.getInstance(), pomocKalendar = Calendar.getInstance();
		cal = firstDate;
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		pomocKalendar.setTime(cal.getTime());
		
		if (rs.first()) {
			int rsTyden = rs.getInt(2), rsDen = rs.getInt(3), aktualniTyden;
			String[] record;
			java.sql.Date pomDatum;

			for (int i = 0; i < rozdilTydnu; i++) {
				record = new String[colCount];
				record[0] = nazevMesice(cal.get(Calendar.MONTH));
				record[1] = Integer.toString(cal.get(Calendar.WEEK_OF_YEAR));
				cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
				record[12] = Integer.toString(cal.get(Calendar.YEAR));
				cal.set(Calendar.DAY_OF_WEEK, 2);
				/*
				 * if (rs.next()) { rs.previous(); rsTyden = rs.getInt(2); rsDen = rs.getInt(3); }
				 */
				aktualniTyden = cal.get(Calendar.WEEK_OF_YEAR);
				for (int j = 2; j < 7; j++) { // tyden planujeme na 5 dnu ned�le == 1, planujeme od pond�l� do patku
					if (rsDen == j && rsTyden == aktualniTyden) {
						record[2 * (j - 1)] = Integer.toString(cal.get(Calendar.DAY_OF_MONTH));
						record[1 + 2 * (j - 1)] = rs.getString(6);
						System.out.println("Zapisuji " + rs.getString(5));
						cal.add(Calendar.DAY_OF_WEEK, 1);
						// System.out.println(xy++);
						if (rs.next()) {
							rsDen = rs.getInt(3);
							if (rsDen == 1)
								rs.next(); // pro p�ipad �e by byla v databazi chyba a byla naplanovana ned�le
							if (rsDen == 7)
								rs.next(); // pro p�ipad �e by byla v databazi chyba a byla naplanovana sobota

							rsDen = rs.getInt(3);
							rsTyden = rs.getInt(2);
							pomDatum = rs.getDate(5);
							pomocKalendar.setTime(pomDatum);
						}
					} else {
						record[2 * (j - 1)] = Integer.toString(cal
								.get(Calendar.DAY_OF_MONTH));
						cal.add(Calendar.DAY_OF_WEEK, 1);
					}
				}
				//record[12] = Integer.toString(cal.get(Calendar.YEAR));
				cal.add(Calendar.WEEK_OF_YEAR, 1);
				cal.set(Calendar.DAY_OF_WEEK, 2);
				cache.addElement(record);
			}

		}

		if (rs.next()) {
			throw new SQLException("Nevypr�zdnily jsme cely ResultSet,"
					+ " kdy� prvn� lit� za��n� v prvn�m tydnu v roce");
		}
/*
		if (cisloPoslednihoTydne - cisloPrvnihoTydne >= 0) {
			System.out.println("Pl�nuji normaln�");
		} else {
			System.out.println("Pl�nuji p�es rok");
		}
*/
		fireTableChanged(null); // notify everyone that we have a new table.
	}

	@Override
	public boolean isCellEditable(int i, int j){
		return true;
	}
	
	/**
	 * 
	 * @param predRadek
	 * @param data
	 */
	public void addRow(int predRadek, Object [] data){
		cache.add(predRadek, data);
	}
	
	public String getColumnName(int i) {
		return headers[i];
	}

	public int getColumnCount() {
		return colCount;
	}

	public int getRowCount() {
		return cache.size();
	}
	
	public Object getValueAt(int row, int col) {
		return ((String[]) cache.elementAt(row))[col];
	}

	public void setValueAt(Object aValue, int row, int col) {
		((String[]) cache.elementAt(row))[col] = (String) aValue;
		fireTableCellUpdated(row, col);
	}

	// All the real work happens here; in a real application,
	// we'd probably perform the query in a separate thread.
	public void setMyModel(ResultSet rs) {
		sdf = new SimpleDateFormat("dd.MM.yyy");
		cache = new Vector();
		try {
			ResultSetMetaData meta = rs.getMetaData();
			colCount = meta.getColumnCount() + 1;

			// Now we must rebuild the headers array with the new column names
			headers = new String[colCount];
			for (int h = 1; h <= colCount; h++) {
				if (colCount - 1 >= h) {
					//headers[h - 1] = meta.getColumnName(h);
					headers[h - 1] = meta.getColumnLabel(h);
				} else {
					headers[h - 1] = " ";
				}
			}

			// and file the cache with the records from our query. This would
			// not be
			// practical if we were expecting a few million records in response
			// to our
			// query, but we aren't, so we can do this.
			DecimalFormatSymbols  sym = new DecimalFormatSymbols(Locale.ENGLISH);
			DecimalFormat formatNumber = new DecimalFormat("###.###",sym);
			;
			String pom;
			java.sql.Date datum;
			while (rs.next()) {
				String[] record = new String[colCount];
				for (int i = 0; i < colCount; i++) {
					if (colCount - 2 >= i) {
						// System.out.println(i+" "+meta.getColumnTypeName(i+1)+" "+meta.getColumnType(i+1)+
						// " "+java.sql.Types.TINYINT);
						if (meta.getColumnTypeName(i + 1).equalsIgnoreCase("TINYINT")) { // tinyInt reprezentuje boolean
							pom = rs.getString(i + 1);
							if (pom.equalsIgnoreCase("0"))
								record[i] = "Ne";
							else if (pom.equalsIgnoreCase("1"))
								record[i] = "Ano";
							else
								record[i] = "neni boolean " + pom;
						} else if (meta.getColumnTypeName(i + 1).equalsIgnoreCase("DATE")) { // tinyInt reprezentuje boolean
							datum = rs.getDate(i+1);
							pom = null;
							if(datum != null){
								pom = sdf.format(datum);
							}
							record[i] = pom;
						} else if (meta.getColumnTypeName(i + 1).startsWith("DECIMAL")) {
							double x = rs.getDouble(i + 1);
							record[i] = formatNumber.format(x);
						} 
						else {
							// pro zbytek
							pom = rs.getString(i + 1);

							record[i] = pom;
						}
					} else
						record[i] = " ";
				}
				cache.addElement(record);
			}

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fireTableChanged(null); // notify everyone that we have a new table.
	}
	
	public String nazevMesice(int i) {
		String prom;
		switch (i) {
		case Calendar.JANUARY:
			prom = "leden";
			break;
		case Calendar.FEBRUARY:
			prom = "�nor";
			break;
		case Calendar.MARCH:
			prom = "b�ezen";
			break;
		case Calendar.APRIL:
			prom = "duben";
			break;
		case Calendar.MAY:
			prom = "kv�ten";
			break;
		case Calendar.JUNE:
			prom = "�erven";
			break;
		case Calendar.JULY:
			prom = "�ervenec";
			break;
		case Calendar.AUGUST:
			prom = "srpen";
			break;
		case Calendar.SEPTEMBER:
			prom = "z���";
			break;
		case Calendar.OCTOBER:
			prom = "��jen";
			break;
		case Calendar.NOVEMBER:
			prom = "listopad";
			break;
		case Calendar.DECEMBER:
			prom = "prosinec";
			break;
		case Calendar.UNDECIMBER:
			prom = "chyba, Calendar vratil UNDECIMBER";
			break;
		default:
			prom = "Chyba planovani "+i;
			break;
		}
		return prom;
	}

	
	public CopyOfQueryTableModel(ResultSet rs, String[] jmenaSloupcu)	throws SQLException, ParseException {
		cache = new Vector<Object>();

		// ResultSetMetaData meta = rs.getMetaData();
		colCount = jmenaSloupcu.length;

		// Now we must rebuild the headers array with the new column names
		headers = new String[colCount];
		for (int h = 0; h < colCount; h++) {
			headers[h] = jmenaSloupcu[h];
		}
		
		/**
		 * V p��pad�, �e rs je pr�zdn� navr�t�me pouze model s nadpisy
		 */
		if(rs == null)return;
		if(!rs.first())return;
		
		Calendar firstDate = Calendar.getInstance();
		Calendar lastDate = Calendar.getInstance();
		java.sql.Date sqlDate;
		rs.first();
		sqlDate = rs.getDate(5);
		firstDate.setTime(sqlDate);
		rs.last();
		sqlDate = rs.getDate(5);
		lastDate.setTime(sqlDate);
		
		int rozdilTydnu = rozdilTydnu(firstDate, lastDate, rs);

		cache = generujRozvrh(colCount, firstDate, rozdilTydnu, rs, cache);	
		
		if (rs.next()) {
			rs.close();
			throw new SQLException("Nevypr�zdnili jsme cel� ResultSet");
		}
		rs.close();
		fireTableChanged(null); // notify everyone that we have a new table.
	}
	
	/**
	 * Doba mezi prvn�m a posledn�m datumem nesm� b�t v�t�� ne� 365 dn� nebo 52 t�dnu, jinak vyhodi SQLException. ResultSet slou�� pouze pro kontrolu.
	 * Vrac� po�et t�dn� mezi datumy. �isloTydne prvn�ho datumu m�nus ��slo t�dne druh�ho datumu s ohledem na p�es rok.<br>
	 * Tato metoda spol�h� na p�edem dan� form�t ResultSetu a to, �e ve 2 sloupci je ��slo t�dne datumu, kter� se nach�z� ve sloupci �. 5. 
	 * @param firstDate
	 * @param lastDate
	 * @param rs slou�� pouze pro kontrolu
	 * @return po�et t�dn� mezi Datumy
	 * @throws SQLException 
	 */
	private int rozdilTydnu(Calendar firstDate, Calendar lastDate, ResultSet rs) throws SQLException{
		int rozdilTydnu = 0;
		
		int rsCisloPrvnihoTydne = -1;
		int rsCisloPoslednihoTydne = -1;
		/**
		 * Slou�� pouze pro kotrolu
		 */
		if(rs.first())rsCisloPrvnihoTydne = rs.getInt(2);
		if(rs.last())rsCisloPoslednihoTydne = rs.getInt(2); 
		
		int cisloPrvnihoTydne = -1;
		int cisloPoslednihoTydne = -1;
		
		LocalDateTime prvniDatum = LocalDateTime.of(firstDate.get(Calendar.YEAR), firstDate.get(Calendar.MONTH) + 1, firstDate.get(Calendar.DAY_OF_MONTH), 0, 0);
		LocalDateTime posledniDatum = LocalDateTime.of(lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH) + 1, lastDate.get(Calendar.DAY_OF_MONTH), 0, 0);

		Duration delka = Duration.between(posledniDatum, prvniDatum);
		if (Math.abs(delka.toDays()) > 365) {
			throw new SQLException("P�i pl�nov�ni se vyskytka neo�et�en� chyba. Zak�zka trv� d�le ne� jeden rok");
		}
		int prvCislDne = firstDate.get(Calendar.DAY_OF_WEEK), poslCislDne = lastDate.get(Calendar.DAY_OF_WEEK);

		firstDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		lastDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
		
		cisloPrvnihoTydne = firstDate.get(Calendar.WEEK_OF_YEAR);
		cisloPoslednihoTydne = lastDate.get(Calendar.WEEK_OF_YEAR);
		
		//kontrola
		if(cisloPrvnihoTydne != rsCisloPrvnihoTydne || cisloPoslednihoTydne != rsCisloPoslednihoTydne){
			throw new SQLException("Vyskytla se chyba, neouhlas� ��sla t�dn� z datab�ze a knihovny Calendar");
		}

		rozdilTydnu = cisloPoslednihoTydne
				+ (lastDate.get(Calendar.YEAR) - firstDate.get(Calendar.YEAR))	* firstDate.getActualMaximum(Calendar.WEEK_OF_YEAR) // obvykle bude nula, kdyz nebudeme planovat pres rok
				- cisloPrvnihoTydne;
		
		firstDate.set(Calendar.DAY_OF_WEEK, prvCislDne);
		lastDate.set(Calendar.DAY_OF_WEEK, poslCislDne);
		
		if (rozdilTydnu > 52) {
			throw new SQLException("P�i pl�nov�ni se vyskytka neo�et�en� chyba. Zak�zka trv� d�le ne� jeden rok");
		}

		return rozdilTydnu;
	}
	
	private Vector generujRozvrh(int colCount, Calendar firstDate, int rozdilTydnu, ResultSet rs, Vector cache) throws SQLException{
		firstDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		if (rs.first()) {
			int rsTyden = rs.getInt(2), rsDen = rs.getInt(3), aktualniTyden;
			String[] record;

			for (int i = 0; i <= rozdilTydnu; i++) {
				record = new String[colCount];
				record[0] = nazevMesice(firstDate.get(Calendar.MONTH));
				record[1] = Integer.toString(firstDate.get(Calendar.WEEK_OF_YEAR));
				firstDate.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
				record[12] = Integer.toString(firstDate.get(Calendar.YEAR));
				firstDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				
				aktualniTyden = firstDate.get(Calendar.WEEK_OF_YEAR);
				for (int j = 2; j < 7; j++) { // tyden planujeme na 5 dnu ned�le == 1, planujeme od pond�l� do patku
					if (rsDen == j && rsTyden == aktualniTyden) {
						record[2 * (j - 1)] = Integer.toString(firstDate.get(Calendar.DAY_OF_MONTH));
						record[1 + 2 * (j - 1)] = rs.getString(6);
						firstDate.add(Calendar.DAY_OF_WEEK, 1);
						if (rs.next()) {
							rsDen = rs.getInt(3);
							if (rsDen == 1) rs.next(); // pro p�ipad �e by byla v databazi chyba a byla naplanovana ned�le
							if (rsDen == 7) rs.next(); // pro p�ipad �e by byla v databazi chyba a byla naplanovana sobota

							rsDen = rs.getInt(3);
							rsTyden = rs.getInt(2);
						}
					} else {
						record[2 * (j - 1)] = Integer.toString(firstDate.get(Calendar.DAY_OF_MONTH));
						firstDate.add(Calendar.DAY_OF_WEEK, 1);
					}
				}
				firstDate.add(Calendar.WEEK_OF_YEAR, 1);
				firstDate.set(Calendar.DAY_OF_WEEK, 2);
				cache.addElement(record);
			}

		}
		return cache;
	}
}


