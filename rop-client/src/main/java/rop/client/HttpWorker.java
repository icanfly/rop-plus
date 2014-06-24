package rop.client;

import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * HTTP请求工具类
 *
 * @author luopeng
 *         Created on 2014/6/20.
 */
public class HttpWorker {

	private RequestConfig requestConfig;

	private final static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	private CloseableHttpClient client;

	private HttpWorker(){
		client = HttpClientBuilder.create().build();
	}

	public static HttpWorker create(RequestConfig requestConfig){
		HttpWorker worker = new HttpWorker();
		worker.requestConfig = requestConfig;
		return worker;
	}

	private CloseableHttpClient getHttpClient() {
		return client;
	}

	public void stop(){
		if(client != null){
			IOUtils.closeQuietly(client);
		}
	}

	public String get(String url, Map<String, String> headers) throws IOException {
		HttpGet get = new HttpGet(url);
		get.setConfig(requestConfig);
		setHeaders(get, headers);
		CloseableHttpResponse response = null;
		String responseStr = null;
		try{
			response = getHttpClient().execute(get);
			checkOK(response);
			responseStr  =  EntityUtils.toString(response.getEntity());
		}finally {
			if(response != null){
				IOUtils.closeQuietly(response);
			}
		}

		return responseStr;
	}

	public String post(String url, Map<String, String> headers, Map<String, String> body, boolean multipart) throws IOException {
		HttpPost post = new HttpPost(url);
		post.setConfig(requestConfig);
		setHeaders(post, headers);
		if (body != null) {
			if (multipart) {
				MultipartEntityBuilder builder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE).setCharset(DEFAULT_CHARSET);
				for (Map.Entry<String, String> entry : body.entrySet()) {
					StringBody entity = new StringBody(entry.getValue(),ContentType.create(ContentType.MULTIPART_FORM_DATA.getMimeType(),	DEFAULT_CHARSET));
					builder.addPart(entry.getKey(), entity);
				}
				post.setEntity(builder.build());
			} else {
				UrlEncodedFormEntity encodedFormEntity = new UrlEncodedFormEntity(toNameValuePairList(body), DEFAULT_CHARSET);
				post.setEntity(encodedFormEntity);
			}
		}
		CloseableHttpResponse response = null;
		String responseStr = null;
		try{
			response = getHttpClient().execute(post);
			checkOK(response);
			responseStr = EntityUtils.toString(response.getEntity());
		}finally {
			if(response != null){
				IOUtils.closeQuietly(response);
			}
		}
		return responseStr;
	}

	private static List<? extends NameValuePair> toNameValuePairList(Map<String, String> body) {
        List<NameValuePair> retList = Lists.newLinkedList();
		for (Map.Entry<String, String> entry : body.entrySet()) {
			retList.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
		}
		return retList;
	}

	private void setHeaders(HttpRequestBase request, Map<String, String> headers) {
		if (headers != null) {
			for (Map.Entry<String, String> header : headers.entrySet()) {
				request.addHeader(header.getKey(), header.getValue());
			}
		}
	}

	private void checkOK(CloseableHttpResponse response) {
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
			throw new RuntimeException("error status code:" + statusLine.getStatusCode());
		}
	}

}
