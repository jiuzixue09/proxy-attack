package com.bigbigwork.util;

import org.apache.http.*;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class HttpClientTools {

	static class MyConnectionSocketFactory extends SSLConnectionSocketFactory {

		public MyConnectionSocketFactory(final SSLContext sslContext) {
			super(sslContext);
		}

		@Override
		public Socket createSocket(final HttpContext context) throws IOException {
			InetSocketAddress socksaddr = new InetSocketAddress("127.0.0.1", 21771);
			Proxy proxy = new Proxy(Proxy.Type.SOCKS, socksaddr);
			return new Socket(proxy);
		}

	}


	static Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
			.register("http", PlainConnectionSocketFactory.INSTANCE)
			.register("https", new MyConnectionSocketFactory(SSLContexts.createSystemDefault()))
			.build();



	private static final int TIME_OUT = 1000 * 60;

	/**
	 * 请求头，默认设置，chrome头
	 * @return
	 * @author huangkai
	 * @date 2018/08/07
	 *
	 */
	public static Map<String, String> headers(){
		Map<String, String> headers = new HashMap<>();
		headers.put("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
		return headers;
	}
	
	public static String doGet(String url) {
		return doGet(url, headers());
	}


	public static String doPostMap(String url, Map<String,String> params,Map<String, String> headers) {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setConnectionManager(cm)
				.build();
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
				.build();

		try {
			HttpPost post = new HttpPost(url);


			post.setConfig(requestConfig);

			//
			if(params!=null){
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
	            for (String name : params.keySet()) {
	    			nvps.add(new BasicNameValuePair(name, params.get(name)));
	    		}
	            post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

			}

            //
			if (headers != null) {
				for (String key : headers.keySet()) {
					post.addHeader(key, headers.get(key));
				}
			}
			CloseableHttpResponse response = httpclient.execute(post);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity, Consts.UTF_8);
		} catch (Exception e) {
			System.err.println("url=" + url + " , params=" + params+" , hearders="+headers + "\n" + e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}


	public static String doPostCookie(String url, Map<String,String> params,Map<String, String> headers) {
		CookieStore cookieStore = new BasicCookieStore();
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);

		// 创建HttpClient上下文
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
				.build();
		CloseableHttpClient httpclient = HttpClients.custom()
				.setConnectionManager(cm)
				.setDefaultCookieStore(cookieStore).setDefaultRequestConfig(requestConfig)
				.build();


		try {
			HttpPost post = new HttpPost(url);

			//
			if(params!=null){
				List<NameValuePair> nvps = new ArrayList<NameValuePair>();
				for (String name : params.keySet()) {
					nvps.add(new BasicNameValuePair(name, params.get(name)));
				}
				post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

			}

			//
			if (headers != null) {
				for (String key : headers.keySet()) {
					post.addHeader(key, headers.get(key));
				}
			}
			CloseableHttpResponse response = httpclient.execute(post);
			Header[] allHeaders = response.getAllHeaders();
			List<Cookie> cookies = cookieStore.getCookies();
			String cookie = cookies.stream().map(it -> it.getName() + "=" + it.getValue()).collect(Collectors.joining(";"));
			return cookie;
		} catch (Exception e) {
			System.err.println("url=" + url + " , params=" + params+" , hearders="+headers + "\n" + e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * download file
	 * @param url image url
	 * @param headers headers
	 * @return file content length
	 */
	public static long download(String url, Map<String, String> headers){
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setConnectionManager(cm)
				.build();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
				.build();
		HttpGet httpGet = new HttpGet(url);

		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				httpGet.setHeader(header.getKey(), header.getValue());
			}
		}

		httpGet.setConfig(requestConfig);
		try {
			CloseableHttpResponse response = httpclient.execute(httpGet);
			return response.getEntity().getContentLength();
		} catch (SSLHandshakeException e){
			return -1;
		}catch (Exception e) {
			System.err.println("url=" + url + "\n" + e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public static int doGetStatus(String url, Map<String, String> headers) {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setConnectionManager(cm)
				.build();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
				.build();
		HttpGet httpGet = new HttpGet(url);

		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				httpGet.setHeader(header.getKey(), header.getValue());
			}
		}

		httpGet.setConfig(requestConfig);
		try {
			CloseableHttpResponse response = httpclient.execute(httpGet);
			return response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			System.err.println("url=" + url + "\n" + e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 500;
	}


	public static String doGet(String url, Map<String, String> headers) {
		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setConnectionManager(cm)
				.build();
		RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
				.build();
		HttpGet httpGet = new HttpGet(url);

		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				httpGet.setHeader(header.getKey(), header.getValue());
			}
		}

		httpGet.setConfig(requestConfig);
		try {
			CloseableHttpResponse response = httpclient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity, Consts.UTF_8);
		} catch (Exception e) {
			System.err.println("url=" + url + "\n" + e);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
