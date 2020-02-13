
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class PriceBot {
	private static String apiKey = "408d0154-eed6-4ae9-94b7-5d769607e5a2";

	public PriceBot() {

	}
	
	public JSONArray getLatest() {
	 String uri = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
	 List<NameValuePair> paratmers = new ArrayList<NameValuePair>();
	 paratmers.add(new BasicNameValuePair("start","1"));
	 paratmers.add(new BasicNameValuePair("limit","300"));
	 paratmers.add(new BasicNameValuePair("convert","USD"));


 	 JSONArray result = null;
	 try {
 		result = makeAPICall(uri, paratmers);
 		return result;
	 } catch (URISyntaxException e) {
		e.printStackTrace();
	 } catch (IOException e) {
		e.printStackTrace();
	 }
	 return result;
	}
	
	private JSONArray makeAPICall(String uri, List<NameValuePair> parameters) 
		      throws URISyntaxException, IOException {
		    URIBuilder query = new URIBuilder(uri);
		    query.addParameters(parameters);

		    CloseableHttpClient client = HttpClients.createDefault();
		    HttpGet request = new HttpGet(query.build());

		    request.setHeader(HttpHeaders.ACCEPT, "application/json");
		    request.addHeader("X-CMC_PRO_API_KEY", apiKey);

		    CloseableHttpResponse response = client.execute(request);

		    try {
		      HttpEntity entity = response.getEntity();
		      JSONObject content = new JSONObject(EntityUtils.toString(entity));
		      EntityUtils.consume(entity);
		      response.close();
			  return content.getJSONArray("data");
			  
		    } finally {
		      response.close();
		    }
	}
}
