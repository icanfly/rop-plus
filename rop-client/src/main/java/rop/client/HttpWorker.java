package rop.client;

import rop.http.HttpRequest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;

/**
 * HTTP请求工具类
 *
 * @author luopeng
 *         Created on 2014/6/20.
 */
public class HttpWorker {

	private static volatile HttpWorker instance;

	private HttpWorker() {

	}

	public static HttpWorker getInstance() {
		if (instance == null) {
			synchronized (HttpWorker.class) {
				if (instance == null) {
					instance = new HttpWorker();
				}
			}
		}
		return instance;


	}


	public String get(String url, Map<String, String> headers,int connTimeout,int readTimeout) throws IOException {
		return _innerGet(url,headers,new TimedConnectionFactory(connTimeout,readTimeout));

	}

	public String get(String url, Map<String, String> headers) throws IOException {
		return _innerGet(url,headers,new TimedConnectionFactory());
	}

	private String _innerGet(String url, Map<String, String> headers,HttpRequest.ConnectionFactory connectionFactory){
		HttpRequest request = new HttpRequest(url, HttpRequest.METHOD_GET);
		request.headers(headers);
		request.setInstanceConnectionFactory(connectionFactory);

		checkOK(request);
		return request.body(HttpRequest.CHARSET_UTF8);
	}

	public String post(String url, Map<String, String> headers, Map<String, String> body, boolean multipart) throws IOException {
		return _innerPost(url,headers,body,multipart,new TimedConnectionFactory());
	}

	public String post(String url, Map<String, String> headers, Map<String, String> body, boolean multipart,
					   int connTimeout,int readTimeout) throws IOException {
		return _innerPost(url,headers,body,multipart,new TimedConnectionFactory(connTimeout,readTimeout));
	}

	private String _innerPost(String url, Map<String, String> headers, Map<String, String> body, boolean multipart,
							  HttpRequest.ConnectionFactory connectionFactory){
		HttpRequest request = new HttpRequest(url, HttpRequest.METHOD_POST);
		request.setInstanceConnectionFactory(connectionFactory);
		request.headers(headers);
		if (body != null) {
			if (multipart) {
				for (Map.Entry<String, String> entry : body.entrySet()) {
					request.part(entry.getKey(), entry.getValue());
				}
			} else {
				request.form(body);
			}
		}

		checkOK(request);
		return request.body(HttpRequest.CHARSET_UTF8);
	}

	private void checkOK(HttpRequest request) {
		if (!request.ok()) {
			throw new RuntimeException("error status code:" + request.code() + ",detail message:" + request.message());
		}
	}

	private static class TimedConnectionFactory implements HttpRequest.ConnectionFactory{
		private int connTimeout = 30000;
		private int readTimeout = 60000;

		public TimedConnectionFactory(){

		}

		public TimedConnectionFactory(int connTimeout,int readTimeout){
			this.connTimeout = connTimeout;
			this.readTimeout = readTimeout;
		}

		@Override
		public HttpURLConnection create(URL url) throws IOException {
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setConnectTimeout(connTimeout);
			connection.setReadTimeout(readTimeout);
			return connection;
		}

		@Override
		public HttpURLConnection create(URL url, Proxy proxy) throws IOException {
			throw new RuntimeException("not supported.");
		}
	}

}
