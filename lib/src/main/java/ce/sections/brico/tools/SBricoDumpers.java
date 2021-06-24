package ce.sections.brico.tools;

import java.io.StringWriter;
import java.io.Writer;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

public class SBricoDumpers 
{
	SBricoLogger _log = null;
	
	public SBricoDumpers(SBricoLogger log) { _log = log;}
	
	
	public void dumpTxt(String msg, String s) 
	{
		_log.debug(msg + "["+s+"]");
	}
	
	public void dumpXML( String msg, String s) 
	{
		dumpTxt (msg, s);
	}

	
	public void dumpXML(String msg, Document d) 
	{
		DOMImplementationRegistry registry;
		try {
			registry = DOMImplementationRegistry.newInstance();
			DOMImplementationLS DOMimplLS = (DOMImplementationLS)registry.getDOMImplementation("LS");

			
			Writer stringWriter = new StringWriter();

			LSOutput lsOutput =  DOMimplLS.createLSOutput();
			lsOutput.setEncoding("UTF-8");
			lsOutput.setCharacterStream(stringWriter);

			LSSerializer lsSerializer = DOMimplLS.createLSSerializer();
			DOMConfiguration domConf = lsSerializer.getDomConfig();
			domConf.setParameter("format-pretty-print",true);
			
			lsSerializer.write(d, lsOutput);     
			String str = stringWriter.toString();
			
			
			SBricoLogger log = SBricoLogger.getLogger();
			log.debug( msg + "[\n"+str+"]");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
	}

}
