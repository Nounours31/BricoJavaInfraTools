package ce.sections.brico.tools;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class SBricoSaxParser {
	public __MyData root = null;
	public __MyData current = null;

	class __MyData {
		public __MyData _prev = null;
		public ArrayList<__MyData> _suiv = null;
		String          _feuille = null;
		public HashMap<String, String> _val = null;
		
		__MyData (__MyData prev, String feuille) {
			this._prev = prev;
			this._feuille = feuille;
			this._suiv = new ArrayList<__MyData>();
			this._val = new HashMap<String, String>();
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer (_prev != null ? "<-" : "0");
			for (__MyData a : _suiv) {
				sb.append(a != null ? "[" + a.toString() + "]" : "0");
			}
			sb.append(_feuille);
			if (_val == null) sb.append("()");
			else {
				sb.append("(");
				Iterator<Entry<String, String>> it = _val.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> i = it.next();
					sb.append(i.getKey() + "=" + i.getValue() + "; ");
				}
				sb.append(")");
			}
			return sb.toString();
		}
	}
	
	class __MyHandler extends DefaultHandler {
		private String tagCourant = "";
		
		/**
		 * Actions à réaliser lors de la détection d'un nouvel élément.
		 */
		public void startElement(String nameSpace, String localName,
				String qName, Attributes attr) throws SAXException  {
			tagCourant = localName;
			//System.out.println("debut tag : " + localName);
			__MyData x = new __MyData(current, tagCourant);
			current._suiv.add (x);
			current = x;
		}

		/**
		 * Actions à réaliser lors de la détection de la fin d'un élément.
		 */
		public void endElement(String nameSpace, String localName,
				String qName) throws SAXException {
			tagCourant = "";
			//System.out.println("Fin tag " + localName);
			current = current._prev;
		}

		/**
		 * Actions à réaliser au début du document.
		 */
		public void startDocument() {
			current = new __MyData(null, "root");
			root = current;
			//System.out.println("Debut du document");
		}

		/**
		 * Actions à réaliser lors de la fin du document XML.
		 */
		public void endDocument() {
			//System.out.println("Fin du document");
		}

		/**
		 * Actions à réaliser sur les données
		 */
		public void characters(char[] caracteres, int debut, int longueur) throws SAXException {
			String donnees = new String(caracteres, debut, longueur);

			if (!tagCourant.equals("")) {  
				if(!Character.isISOControl(caracteres[debut])) {
					// System.out.println("   Element " + tagCourant +"  valeur = *" + donnees + "*");
					current._val.put(tagCourant, donnees);
				}
			}
		}
	}

		public SBricoSaxParser(String xMLToParse)
		{
			try
			{
				Class<?> c = Class.forName("org.apache.xerces.parsers.SAXParser");
				XMLReader reader = (XMLReader)c.newInstance();
				__MyHandler handler = new __MyHandler();
				reader.setContentHandler(handler);
				InputSource is = new InputSource(new ByteArrayInputStream(xMLToParse.getBytes()));
				reader.parse(is);
				//System.out.println(root.toString());
			}
			catch(Exception e){
				System.out.println(e);
				e.printStackTrace();
			}
		}
		
		public ArrayList<String> getAllTagByValue (String tag) {
			return this._getAllTagByValue (tag, root);
		}
		
		private ArrayList<String> _getAllTagByValue (String tag, __MyData noeud) {
			ArrayList<String> retour = new ArrayList<String>();

			for (__MyData a : noeud._suiv) {
				ArrayList<String> x = this._getAllTagByValue (tag, a);
				if ((x != null) && (x.size()>0))
					retour.addAll(x);
			}
			
			if (noeud._val != null) {
				Iterator<Entry<String, String>> it = noeud._val.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, String> i = it.next();
					if (i.getKey().equals(tag))
						retour.add(i.getValue());
				}
			}
			return retour;
		}
	}
