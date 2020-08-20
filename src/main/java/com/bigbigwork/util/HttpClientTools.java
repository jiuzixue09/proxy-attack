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

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Supplier;
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


	static final int TIME_OUT = 1000 * 60;
	static Supplier<RequestConfig> requestConfig = () ->  RequestConfig.custom()
			.setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
			.setConnectionRequestTimeout(10000)
			.build();

	private static final CloseableHttpClient httpClient;
	static PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
	static {

		cm.setMaxTotal(20);//多线程调用注意配置，根据线程数设定
		cm.setDefaultMaxPerRoute(cm.getMaxTotal());
		httpClient = HttpClients.custom().setConnectionManager(cm).build();
	}


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

		try {
			HttpPost post = new HttpPost(url);
			post.setConfig(requestConfig.get());

			if(params!=null){
				List<NameValuePair> nvps = new ArrayList<>();
	            for (String name : params.keySet()) {
	    			nvps.add(new BasicNameValuePair(name, params.get(name)));
	    		}
	            post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));

			}
			if (headers != null) {
				for (String key : headers.keySet()) {
					post.addHeader(key, headers.get(key));
				}
			}
			try(CloseableHttpResponse response = httpClient.execute(post)){
				HttpEntity entity = response.getEntity();
				return EntityUtils.toString(entity, Consts.UTF_8);
			}
		} catch (Exception e) {
			System.err.println("url=" + url + " , params=" + params+" , hearders="+headers + "\n" + e);
		}
		return null;
	}


	public static String doPostCookie(String url, Map<String,String> params,Map<String, String> headers) {
		CookieStore cookieStore = new BasicCookieStore();

		RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD)
				.setSocketTimeout(TIME_OUT).setConnectTimeout(TIME_OUT)
				.setConnectionRequestTimeout(10000)
				.build();
		// 创建HttpClient上下文
		HttpClientContext context = HttpClientContext.create();
		context.setCookieStore(cookieStore);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setConnectionManager(cm)
				.setConnectionManagerShared(true) //设置共享连接池
				.setDefaultCookieStore(cookieStore).setDefaultRequestConfig(requestConfig)
				.build();


		try {
			HttpPost post = new HttpPost(url);

			if(params!=null){
				List<NameValuePair> nvps = new ArrayList<>();
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
			try(CloseableHttpResponse response = httpclient.execute(post)){
				List<Cookie> cookies = cookieStore.getCookies();
				String cookie = cookies.stream().map(it -> it.getName() + "=" + it.getValue()).collect(Collectors.joining(";"));
				return cookie;
			}

		} catch (Exception e) {
			System.err.println("url=" + url + " , params=" + params+" , hearders="+headers + "\n" + e);
		}
		return null;
	}

	/**
	 * download file
	 * @param url image url
	 * @param headers headers
	 * @param f
	 * @return file content length
	 */
	public static long download(String url, Map<String, String> headers, File f){
		HttpGet httpGet = new HttpGet(url);

		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				httpGet.setHeader(header.getKey(), header.getValue());
			}
		}

		httpGet.setConfig(requestConfig.get());
		try (CloseableHttpResponse response = httpClient.execute(httpGet)){
			HttpEntity entity = response.getEntity();
			try{
				ImageIO.write(ImageIO.read(entity.getContent()),"jpg", new File(f.getAbsolutePath(), System.nanoTime() + ".jpg"));
			}catch (Exception ignored){}
			return entity.getContentLength();
		} catch (SSLHandshakeException e){
			return -1;
		}catch (Exception e) {
			System.err.println("url=" + url + "\n" + e);
		}
		return 0;
	}

	public static int doGetStatus(String url, Map<String, String> headers) {
		HttpGet httpGet = new HttpGet(url);

		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				httpGet.setHeader(header.getKey(), header.getValue());
			}
		}

		httpGet.setConfig(requestConfig.get());
		try (CloseableHttpResponse response = httpClient.execute(httpGet)){
			return response.getStatusLine().getStatusCode();
		} catch (Exception e) {
			System.err.println("url=" + url + "\n" + e);
		}
		return 500;
	}


	public static String doGet(String url, Map<String, String> headers) {
		HttpGet httpGet = new HttpGet(url);

		if (headers != null) {
			for (Entry<String, String> header : headers.entrySet()) {
				httpGet.setHeader(header.getKey(), header.getValue());
			}
		}
		httpGet.setConfig(requestConfig.get());
		try (CloseableHttpResponse response = httpClient.execute(httpGet)){
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity, Consts.UTF_8);
		} catch (Exception e) {
			System.err.println("url=" + url + "\n" + e);
		}
		return null;
	}
}
