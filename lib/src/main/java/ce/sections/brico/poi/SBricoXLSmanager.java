package ce.sections.brico.poi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;


import ce.sections.brico.batch.infra.SBricoException;
import ce.sections.brico.tools.SBricoLogger;

public class SBricoXLSmanager 
{
	protected File 						_xlsFile = null;

	private HashMap<String, String> 	_indexColoneXLSCorrespondAuChampDB = new HashMap<String, String>();
	private int							_indiceDeLaColoneEntrepriseDansXLS = -1;
	private int							_indiceDeLaColoneDateDossierDansXLS = -1;
	private String 						_NomColoneDate = ""; 
	private String 						_NomColoneEntreprise = ""; 
	private HashMap<String, String> 	_MappingNomCE2NomDB = null;
	private HashMap<String, String> 	_MappingEntrepriceNomCE2NomDB = null;
	private String[] 					_MandArgs = null;
	private SBricoLogger 				_log			= SBricoLogger.getLogger();
	private SimpleDateFormat 			_sdfFromUIJava			= null;
	private SimpleDateFormat 			_sdf_From_XLS = null;
	

	


	public SBricoXLSmanager(File f, 
			String 					NomColoneDate, 
			String 					NomColoneEntreprise, 
			HashMap<String, String> MappingNomCE2NomDB, 
			HashMap<String, String> MappingEntreprise,
			String[] 				MandArgs,
			SimpleDateFormat 		sdf1, 
			SimpleDateFormat 		sdf2) 
	{
		_xlsFile = f;
		_NomColoneDate = NomColoneDate;
		_NomColoneEntreprise = NomColoneEntreprise;
		_MappingNomCE2NomDB = MappingNomCE2NomDB;
		_MappingEntrepriceNomCE2NomDB = MappingEntreprise;
		_MandArgs = MandArgs;
		_sdfFromUIJava = sdf1;
		_sdf_From_XLS = sdf2;
	}



	private void _OnFirstRawInitMappingTable (ArrayList<Cell> uneligne) throws Exception 
	{
		ArrayList<String> listeInfoPriseEnCompte = new ArrayList<String>();

		for (int i = 0; i < uneligne.size(); i++) {
			Cell cell  = uneligne.get(i);

			String nomChampCELu 	= SBricoXLSmanager.getCellValueAsString (cell, _sdf_From_XLS);
			String indiceCelluleLueAsText = Integer.toString(i);


			// --------------------------------------
			// Cette colone n'a pas besoin d'etre mappee, ou cas pas prevu 
			// Pour lemoment on laisse filer, on checkera les arg mandatory apres
			// --------------------------------------
			if (!_MappingNomCE2NomDB.containsKey(nomChampCELu))
				continue;

			// --------------------------------------
			// OK tout baigne je rajoute le lien Numero de colone <-> nom de colone
			// --------------------------------------
			String nomColoneEnDB = _MappingNomCE2NomDB.get(nomChampCELu);
			_indexColoneXLSCorrespondAuChampDB.put(indiceCelluleLueAsText, nomColoneEnDB);
			listeInfoPriseEnCompte.add (_MappingNomCE2NomDB.get(nomChampCELu));
			
			
			// -------------------------------------------
			// recherche des deux colone particuliere qui sont date et nom entreprise
			// -------------------------------------------
			if (nomColoneEnDB.equals(_NomColoneDate)) {
				_indiceDeLaColoneDateDossierDansXLS = i;
			}
			if (nomColoneEnDB.equals(_NomColoneEntreprise)) {
				_indiceDeLaColoneEntrepriseDansXLS = i;
			}
		}

		for (String mandArg : _MandArgs) {
			if (!listeInfoPriseEnCompte.contains(mandArg)) {
				throw new Exception ("Il manque des attr mandatory: " + mandArg);
			}
		}
	}


	public ArrayList<HashMap<String, String>> parseXLS (Date dateInscription) throws Exception
	{
		ArrayList<HashMap<String, String>> retour = new ArrayList<HashMap<String, String>>();
		if ((_xlsFile == null) || (!_xlsFile.exists()) || (!_xlsFile.isFile()))
			throw new SBricoException("XLS File not exist");

		InputStream inp = null;
		try
		{ 
			inp = new FileInputStream(_xlsFile);


			//------------------------------
			// Ouvrir le doc
			//------------------------------
			Workbook wb = WorkbookFactory.create (inp);

			//------------------------------
			// check nbsheet
			//------------------------------
			int nbSheet = wb.getNumberOfSheets();
			if (nbSheet > 2)
				throw new SBricoException("Fichier XLS verole - more than 1 sheet");

			//------------------------------
			// go
			//------------------------------
			Sheet sheet = wb.getSheetAt(0);
			boolean isFirstRow = true;
			for (Row row : sheet) 
			{
				if (row == null)
					continue;

				// ---------------------------------------
				// Lecture d'une ligne complete
				// ---------------------------------------
				ArrayList<Cell> uneLigneLueDansXLS = new ArrayList<Cell>();
				for(Cell cell : row) 
					uneLigneLueDansXLS.add(cell);


				// ----------------------------------------
				// check de la ligne
				// ----------------------------------------
				if (isFirstRow) {
					this._OnFirstRawInitMappingTable(uneLigneLueDansXLS);
					isFirstRow = false;
				}
				else {
					// --------------------------------------
					// est ce que cette ligne est valide
					// Pour cela je dois avoir des valeur pour chaque colone
					// --------------------------------------
					Iterator<Entry<String, String>> iteSurTousLesChampsDb = _indexColoneXLSCorrespondAuChampDB.entrySet().iterator();
					boolean ligneko=false;
					
					// Pour chaque colone
					while (iteSurTousLesChampsDb.hasNext()) {
						Entry<String, String> uneVal = iteSurTousLesChampsDb.next();
						
						// son indice dans le tableau issu de la lecture de l'XLS 
						int i = Integer.parseInt(uneVal.getKey());
						
						// s'il n'y a rien a cet endroit ... 
						if ((i >= uneLigneLueDansXLS.size()) || (uneLigneLueDansXLS.get(i) == null)) {
							_log.fatal("============================================================================");
							_log.fatal("Probleme a la lecture d'une ligne ");
							_log.fatal(SBricoXLSmanager.lineToString(uneLigneLueDansXLS, _sdf_From_XLS));
							_log.fatal("============================================================================");
							ligneko=true;
							break;
						}
					}
					if (ligneko)
						continue;
					
					
					// -------------------
					// suppression des user deja importer
					// -------------------
					if ((dateInscription != null) && (_indiceDeLaColoneDateDossierDansXLS > -1)) {
						try {
							Date d = _sdf_From_XLS.parse(SBricoXLSmanager.getCellValueAsString(uneLigneLueDansXLS.get(_indiceDeLaColoneDateDossierDansXLS), _sdf_From_XLS));
							if (_log.isDebug()) {
								_log.debug("---------------------------------------");
								_log.debug("- XLS date : " + _sdfFromUIJava.format(d));
								_log.debug("- Last Import date : " + _sdfFromUIJava.format(dateInscription));
								_log.debug("---------------------------------------");
							}
							if (d.before(dateInscription)){
								if (_log.isDebug()) {
									_log.debug("---------------------------------------");
									_log.debug("User anterieure a la date d'ajout - ne rien faire");
									_log.debug(SBricoXLSmanager.lineToString(uneLigneLueDansXLS, _sdf_From_XLS));
									_log.debug("---------------------------------------");
								}
								continue;
							}
						}
						catch (Exception e) {
							_log.fatal("Unable to parse date depuis le fichier XLS => " + uneLigneLueDansXLS.get(_indiceDeLaColoneDateDossierDansXLS));
							_log.fatal(e);
							_log.fatal("================================================================================");
							_log.fatal(SBricoXLSmanager.lineToString(uneLigneLueDansXLS, _sdf_From_XLS));
							_log.fatal("================================================================================");
							continue;
						}
					}

					
					
					// -------------------
					// suppression des user dont l'entreprise est inconnue
					// -------------------
					String entrepriseLuXLS = SBricoXLSmanager.getCellValueAsString(uneLigneLueDansXLS.get(_indiceDeLaColoneEntrepriseDansXLS), _sdf_From_XLS);
					if (!_MappingEntrepriceNomCE2NomDB.containsKey(entrepriseLuXLS)) {
						_log.fatal("=====================================================================");
						_log.fatal("Entreprise inconnue");
						_log.fatal(SBricoXLSmanager.lineToString(uneLigneLueDansXLS, _sdf_From_XLS));
						_log.fatal("=====================================================================");
						continue;
					}

					
					// ----------------------------------------------
					// OK la ligne a ete validee, on la dispatche dans les colones
					// ----------------------------------------------
					HashMap<String, String> uneLigneFormatee = this.pushXLSDataIntoHashTab(uneLigneLueDansXLS);

					// --------------------------------------------------------------
					// Est ce que tous les arg mand sont la
					// --------------------------------------------------------------
					Set<String> keys = uneLigneFormatee.keySet();
					for (String s : _MandArgs)
					{
						if (!keys.contains(s))
							throw new SBricoException("Il manque dans le xls l'arg mand: "+ s);
					}
					retour.add(uneLigneFormatee);
				}
			}


			//-------------------------------------------
			// Un petit dump ...
			//-------------------------------------------
			SBricoXLSmanager.dumpXLS(_log, retour);
		} 
		catch (Exception e) 
		{
			_log.fatal(e);
			throw e;
		} 
		finally 
		{
			try {
				inp.close();
			} catch (IOException e) {
				_log.fatal(e);
				throw e;
			}
		}

		
		return retour;
	}

	private static void dumpXLS(SBricoLogger _log, ArrayList<HashMap<String, String>> retour) {
		for (int i = 0; i < retour.size(); i++) {
			HashMap<String, String> hashMap = retour.get(i);
			_log.debug("retour["+i+"] = ");
			
			Iterator<Entry<String, String>> ite = hashMap.entrySet().iterator();
			StringBuffer sb = new StringBuffer("\t");
			while (ite.hasNext()) {
				Entry<String, String> x = ite.next();
				sb.append(x.getKey()+"="+x.getValue()+"; ");
			}
			_log.debug(sb.toString());
		}
	}



	private static String lineToString(ArrayList<Cell> uneLigneLueDansXLS, SimpleDateFormat _sdf_From_XLS) {
		StringBuffer sb = new StringBuffer("-->");
		for (int i = 0; i < uneLigneLueDansXLS.size(); i++) {
			Cell cell = uneLigneLueDansXLS.get(i);
			sb.append("[");
			sb.append(i);
			sb.append("]=");
			sb.append(SBricoXLSmanager.getCellValueAsString (cell, _sdf_From_XLS) + "; "); 
		}
		sb.append("<--");
		return sb.toString();
	}



	private HashMap<String, String> pushXLSDataIntoHashTab(ArrayList<Cell> uneLigneLueDansXLS) {
		HashMap<String, String> retour = new HashMap<String, String>();
		for (int i = 0; i < uneLigneLueDansXLS.size(); i++) {
			Cell cell = uneLigneLueDansXLS.get(i);

			// recherche du nom de la colone
			String indiceCelluleLueAsText = Integer.toString(i);
			String coloneName = _indexColoneXLSCorrespondAuChampDB.get(indiceCelluleLueAsText);

			// remplissage de la colone
			String valeurCelluleLueAsText = SBricoXLSmanager.getCellValueAsString (cell, _sdf_From_XLS);
			String s = StringEscapeUtils.escapeXml11(valeurCelluleLueAsText);
			if (i == _indiceDeLaColoneEntrepriseDansXLS)
				s = _MappingEntrepriceNomCE2NomDB.get(s);

			if (s.length() > 0)
				retour.put(coloneName, s);
		}
		return retour;
	}



	protected static String getCellValueAsString(Cell cell, SimpleDateFormat _sdf_From_XLS) 
	{
		String retour = "";
		if (cell == null)
			return retour;
		
		CellType cType 		= cell.getCellTypeEnum();
		switch (cType)
		{
		case BOOLEAN: 
			retour = Boolean.toString(cell.getBooleanCellValue()); 
			break;

		case FORMULA:
			CellType cFormulaType = cell.getCachedFormulaResultTypeEnum();
			switch (cFormulaType)
			{
			case NUMERIC: 
				if (DateUtil.isCellDateFormatted(cell)) {
					Date d = cell.getDateCellValue();
					retour = _sdf_From_XLS.format(d);
				} else {
					double dval=cell.getNumericCellValue();
					if(dval == (long) dval)
						retour = String.format("%d",(long)dval);
					else
						retour = String.format("%s",dval);				
				}
				break;
			case STRING:
				retour = cell.getStringCellValue();
				break;
			case BOOLEAN: 
				retour = Boolean.toString(cell.getBooleanCellValue()); 
				break;
			default: 
			}
			break;
		case NUMERIC: 
			if (DateUtil.isCellDateFormatted(cell)) {
				Date d = cell.getDateCellValue();
				retour = _sdf_From_XLS.format(d);
			} else {
				double dval=cell.getNumericCellValue();
				if(dval == (long) dval)
					retour = String.format("%d",(long)dval);
				else
					retour = String.format("%s",dval);	
			}
			break;
		case STRING: 
			retour = cell.getStringCellValue();
			break;
		default:
			retour = "";
		}
		return retour;
	}

	@Override
	public String toString() {
		return "SBricoXLSTools []";
	}

}
