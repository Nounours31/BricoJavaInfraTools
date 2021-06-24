package ce.sections.brico.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import ce.sections.brico.batch.infra.SBricoConstantes;

public class SBricoHTTPMngr 
{
	private boolean _fiddlerHook = false;
	private SBricoLogger _log = SBricoLogger.getLogger();
	private String _response = "";
	
	public SBricoHTTPMngr() {}
	
	public String sendXMLPost(String url, String XMLmsg) throws Exception 
	{
		String retour = null;
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try 
		{
			
			HttpPost request = new HttpPost (url);
			request.setHeader("User-Agent", SBricoConstantes.USER_AGENT);
			request.setHeader("Content-type", "text/xml");
			if (_fiddlerHook) 
			{
				HttpHost proxy = new HttpHost("127.0.0.1", 8888, "http");
				Builder builder = RequestConfig.custom();
				builder.setProxy(proxy);
				RequestConfig config = builder.build();
	            request.setConfig(config);
			}

			HttpEntity entityIn = new ByteArrayEntity(XMLmsg.getBytes(StandardCharsets.UTF_8));
			request.setEntity(entityIn);

			_log.debug ("Try to send: POST URL :" + url);
			_log.debug ("                  DATA:" + XMLmsg);
			CloseableHttpResponse response = httpclient.execute(request);
			try {
				_log.debug("HTTP: response.getStatusLine() = "+response.getStatusLine());
				_log.debug("HTTP: response.getStatusLine().getStatusCode() = "+response.getStatusLine().getStatusCode());

				HttpEntity entityResponse = response.getEntity();
				this._response = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
				_log.debug ("HTTP: response = "+this._response);

				retour = this._response;
			} 
			finally 
			{
				response.close();
			}
		} 
		finally {
			httpclient.close();
		}
		return retour;
	}

	public void sendFormPost(String url, HashMap<String, String> form) throws Exception 
	{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try 
		{
			HttpPost request = new HttpPost (url);
			request.setHeader("User-Agent", SBricoConstantes.USER_AGENT);
			request.setHeader("Content-type", "application/x-www-form-urlencoded");

			List <NameValuePair> postParams = new ArrayList <NameValuePair>();
			postParams.add(new BasicNameValuePair("username", "vip"));
			postParams.add(new BasicNameValuePair("password", "secret"));
			request.setEntity(new UrlEncodedFormEntity(postParams));
			 			
			CloseableHttpResponse response2 = httpclient.execute(request);
			try {
				System.out.println(response2.getStatusLine());
				System.out.println(response2.getStatusLine().getStatusCode());
				HttpEntity entity2 = response2.getEntity();

				BufferedReader rd = new BufferedReader(new InputStreamReader(entity2.getContent()));
				StringBuffer result = new StringBuffer();
				String line = "";
				while ((line = rd.readLine()) != null) {
					result.append(line);
				}
				System.out.println(result.toString());
				EntityUtils.consume(entity2);
			} 
			finally 
			{
				response2.close();
			}
		} 
		finally {
			httpclient.close();
		}
		System.out.println("Done");
	}

	public String getResponse() {
		return this._response;
	}
}
