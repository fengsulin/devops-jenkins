package com.aspire.devops.common.utils;

import com.aspire.devops.common.vo.HttpResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.*;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @description：<br>
 * @author: caiyaming
 * @date: 2018-07-31 10:07
 * @since: V1.0.0
 */
@Slf4j
public class HttpUtils {
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static PoolingHttpClientConnectionManager clientConnectionManager = null;
    private static CloseableHttpClient httpClient = null;
    //private static RequestConfig config = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
    private static RequestConfig config = null;
    private static HttpRequestRetryHandler httpRequestRetryHandler = null;
    /**
     * 双检锁/双重校验锁, 防止httpClient实例多次
     */
    public static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (HttpUtils.class) {
                if (httpClient == null) {
                    init();
                    httpClient = HttpClients.custom()
                            .setConnectionManager(clientConnectionManager) //连接管理器
                            //.setProxy(new HttpHost("myproxy", 8080))     //设置代理
                            .setDefaultRequestConfig(config)               //默认请求配置
                            .setRetryHandler(httpRequestRetryHandler)      //重试策略
                            .setDefaultCredentialsProvider(getCredentialsProvider()) // basic认证
                            .build();
                }
            }
        }
        return httpClient;
    }

    /**
     * Basic认证
     * @return
     */
    private static CredentialsProvider getCredentialsProvider(){
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        AuthScope scope = new AuthScope("10.8.17.221", 8080);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("fengsulin","11def70e932f4aa54fbbc69d0cc5d0e89a");
        // Inject the credentials
        provider.setCredentials(scope, credentials);
        return provider;
    }


    /**
     * 创建httpclient连接池并初始化
     */
    private static void init() {
        try {
            //添加对https的支持，该sslContext没有加载客户端证书
            // 如果需要加载客户端证书，请使用如下sslContext,其中KEYSTORE_FILE和KEYSTORE_PASSWORD分别是你的证书路径和证书密码
            //KeyStore keyStore  =  KeyStore.getInstance(KeyStore.getDefaultType()
            //FileInputStream instream =   new FileInputStream(new File(KEYSTORE_FILE));
            //keyStore.load(instream, KEYSTORE_PASSWORD.toCharArray());
            //SSLContext sslContext = SSLContexts.custom().loadKeyMaterial(keyStore,KEYSTORE_PASSWORD.toCharArray())
            // .loadTrustMaterial(null, new TrustSelfSignedStrategy())
            //.build();

            //这里设置信任所有证书
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustStrategy() {
                // 信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            SSLConnectionSocketFactory sslSf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", sslSf)
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .build();
            //配置连接池
            clientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            //最大连接
            clientConnectionManager.setMaxTotal(50);
            //默认的每个路由的最大连接数
            clientConnectionManager.setDefaultMaxPerRoute(25);
            //设置到某个路由的最大连接数，会覆盖defaultMaxPerRoute
            clientConnectionManager.setMaxPerRoute(new HttpRoute(new HttpHost("127.0.0.1", 80)), 150);


            /**
             * socket配置（默认配置 和 某个host的配置）
             */
            SocketConfig socketConfig = SocketConfig.custom()
                    .setTcpNoDelay(true)     //是否立即发送数据，设置为true会关闭Socket缓冲，默认为false
                    .setSoReuseAddress(true) //是否可以在一个进程关闭Socket后，即使它还没有释放端口，其它进程还可以立即重用端口
                    .setSoTimeout(500)       //接收数据的等待超时时间，单位ms
//                    .setSoLinger(6)         //关闭Socket时，要么发送完所有数据，要么等待60s后，就关闭连接，此时socket.close()是阻塞的
                    .setSoKeepAlive(true)    //开启监视TCP连接是否有效
                    .build();
            clientConnectionManager.setDefaultSocketConfig(socketConfig);
            clientConnectionManager.setSocketConfig(new HttpHost("localhost", 80), socketConfig);


            /**
             * HTTP connection相关配置（默认配置 和 某个host的配置）
             * 一般不修改HTTP connection相关配置，故不设置
             */
            //消息约束
            MessageConstraints messageConstraints = MessageConstraints.custom()
                    .setMaxHeaderCount(200)
                    .setMaxLineLength(2000)
                    .build();
            //Http connection相关配置
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setMalformedInputAction(CodingErrorAction.IGNORE)
                    .setUnmappableInputAction(CodingErrorAction.IGNORE)
                    .setCharset(Consts.UTF_8)
                    .setMessageConstraints(messageConstraints)
                    .build();
            //一般不修改HTTP connection相关配置，故不设置
            //connManager.setDefaultConnectionConfig(connectionConfig);
            //connManager.setConnectionConfig(new HttpHost("somehost", 80), ConnectionConfig.DEFAULT);


            // 配置请求的超时设置
            config = RequestConfig.custom()
                    .setConnectTimeout(2 * 1000)         //连接超时时间
                    .setSocketTimeout(2 * 1000)          //读超时时间（等待数据超时时间）
                    .setConnectionRequestTimeout(500)    //从池中获取连接超时时间
                    .setStaleConnectionCheckEnabled(true)//检查是否为陈旧的连接，默认为true，类似testOnBorrow
                    .build();


            /**
             * 重试处理
             * 默认是重试3次
             */
            //禁用重试(参数：retryCount、requestSentRetryEnabled)
            HttpRequestRetryHandler requestRetryHandler = new DefaultHttpRequestRetryHandler(0, false);
            //自定义重试策略
            httpRequestRetryHandler = new HttpRequestRetryHandler() {
                public boolean retryRequest(IOException exception,
                                            int executionCount, HttpContext context) {
                    if (executionCount >= 3) {// 如果已经重试了3次，就放弃
                        return false;
                    }
                    if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                        return true;
                    }
                    if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                        return false;
                    }
                    if (exception instanceof InterruptedIOException) {// 超时
                        return false;
                    }
                    if (exception instanceof UnknownHostException) {// 目标服务器不可达
                        return false;
                    }
                    if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                        return false;
                    }
                    if (exception instanceof SSLException) {// SSL握手异常
                        return false;
                    }
                    HttpClientContext clientContext = HttpClientContext
                            .adapt(context);
                    HttpRequest request = clientContext.getRequest();
                    //Retry if the request is considered idempotent
                    //如果请求类型不是HttpEntityEnclosingRequest，被认为是幂等的，那么就重试
                    //HttpEntityEnclosingRequest指的是有请求体的request，比HttpRequest多一个Entity属性
                    //而常用的GET请求是没有请求体的，POST、PUT都是有请求体的
                    //Rest一般用GET请求获取数据，故幂等，POST用于新增数据，故不幂等
                    if (!(request instanceof HttpEntityEnclosingRequest)) {
                        return true;
                    }
                    return false;
                }
            };

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get请求(1.处理http请求;2.处理https请求,信任所有证书)[默认编码:UTF-8]
     * @param url (参数直接拼接到URL后面，即http://test.com?a=1&b=2的形式)
     * @return
     */
    public static HttpResponseVo get(String url,Map<String,String> reqHeader){
        return get(url, null,reqHeader, DEFAULT_ENCODING);
    }

    /**
     * get请求(1.处理http请求;2.处理https请求,信任所有证书)[默认编码:UTF-8]
     * @param url (url不带参数，例：http://test.com)
     * @param reqMap (参数放置到一个map中)
     * @return
     */
    public static HttpResponseVo get(String url, Map<String, String> reqMap,Map<String,String> reqHeader){
        return get(url, reqMap, reqHeader,DEFAULT_ENCODING);
    }

    /**
     * get请求(1.处理http请求;2.处理https请求,信任所有证书)
     * @param url (只能是http或https请求)
     * @param encoding
     * @return
     */
    public static HttpResponseVo get(String url, Map<String, String> reqMap,Map<String,String> reqHeader, String encoding){
        HttpResponseVo result = new HttpResponseVo();
        if (StringUtils.isBlank(url)) {
            log.info(">>>>>>>>>>>url为空<<<<<<<<<<<<<<");
            return result;
        }
        // 处理参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (reqMap != null && reqMap.keySet().size() > 0) {
            Iterator<Map.Entry<String, String>> iter = reqMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entity = iter.next();
                params.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
            }
        }

        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
        HttpGet httpGet = null;
        try {
            if (params != null && params.size() > 0) {
                URIBuilder builder = new URIBuilder(url);
                builder.setParameters(params);
                httpGet = new HttpGet(builder.build());
            }else {
                httpGet = new HttpGet(url);
            }
            if (reqHeader != null && reqHeader.size() > 0){
                List<Header> headerList = handleHeader(reqHeader);
                httpGet.setHeaders(headerList.toArray(new Header[reqHeader.size()]));
            }
            // 发送请求，并接收响应
            response = httpClient.execute(httpGet);
            result = handleResponse(url, encoding, response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    /**
     * post请求(1.处理http请求;2.处理https请求,信任所有证书)[默认编码:UTF-8]
     * @param url
     * @param reqMap
     * @return
     */
    public static HttpResponseVo post(String url, Map<String, String> reqMap,Map<String,String> reqHeader) throws IOException {
        return post(url, reqMap, reqHeader,DEFAULT_ENCODING);
    }

    /**
     * post请求(1.处理http请求;2.处理https请求,信任所有证书)
     * @param url
     * @param reqMap 入参是个map
     * @param encoding
     * @return
     */
    public static HttpResponseVo post(String url, Map<String, String> reqMap,Map<String,String> reqHeader, String encoding) throws IOException {
        HttpResponseVo result = new HttpResponseVo();
        if (StringUtils.isBlank(url)) {
            log.info(">>>>>>>>>>>>>>>url为空<<<<<<<<<<<<<<<");
            return result;
        }
        // 添加参数
        List<NameValuePair> params = new ArrayList<>();
        if (reqMap != null && reqMap.keySet().size() > 0) {
            Iterator<Map.Entry<String, String>> iter = reqMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entity = iter.next();
                params.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
            }
        }

        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;
            HttpPost httpPost = new HttpPost(url);
            if (reqHeader != null && reqHeader.size() > 0){
                List<Header> headerList = handleHeader(reqHeader);
                httpPost.setHeaders(headerList.toArray(new Header[reqHeader.size()]));
            }
            httpPost.setEntity(new UrlEncodedFormEntity(params, encoding));

            // 发送请求，并接收响应
            response = httpClient.execute(httpPost);
            result = handleResponse(url, encoding, response);
        return result;
    }

    /**
     * 请求体为xml
     * @param url
     * @param param
     * @param dataXml
     * @return
     */
    public static HttpResponseVo postX(String url,Map<String,String> param,Map<String,String> reqHeader,String dataXml) throws IOException, URISyntaxException {
        HttpResponseVo result = new HttpResponseVo();
        if (StringUtils.isBlank(url)){
            log.info(">>>>>>>>>>>url为空<<<<<<<<<<<<<<<");
            return result;
        }
        // 处理参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        if (param != null && param.keySet().size() > 0) {
            Iterator<Map.Entry<String, String>> iter = param.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, String> entity = iter.next();
                params.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
            }
        }

        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;

            URIBuilder uriBuilder = new URIBuilder(url);
            uriBuilder.setParameters(params);
            HttpPost httpPost = new HttpPost(uriBuilder.build());
            httpPost.setHeader("Content-Type","text/xml");
            if (reqHeader != null && reqHeader.size() > 0){
                List<Header> headerList = handleHeader(reqHeader);
                httpPost.setHeaders(headerList.toArray(new Header[reqHeader.size()]));
            }
        if (dataXml != null){
                httpPost.setEntity(new StringEntity(dataXml, ContentType.create("application/xml", Charset.forName("utf-8"))));
            }
            // 发送请求，并接收响应
            response = httpClient.execute(httpPost);
            result = handleResponse(url, "UTF-8", response);

        return result;

    }
    /**
     * post请求(1.处理http请求;2.处理https请求,信任所有证书)
     * @param url
     * @param jsonParams 入参是个json字符串
     * @param encoding
     * @return
     */
    public static HttpResponseVo postJ(String url, String jsonParams,Map<String,String> reqHeader, String encoding){
        HttpResponseVo result = new HttpResponseVo();
        if (StringUtils.isBlank(url)) {
            log.error(">>>>>>>>>>url为空<<<<<<<<<<<<");
            return result;
        }

        CloseableHttpClient httpClient = getHttpClient();
        CloseableHttpResponse response = null;

        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-Type","application/json");
            if (reqHeader != null && reqHeader.size() > 0){
                List<Header> headerList = handleHeader(reqHeader);
                httpPost.setHeaders(headerList.toArray(new Header[reqHeader.size()]));
            }
            httpPost.setEntity(new StringEntity(jsonParams, ContentType.create("application/json", encoding)));

            // 发送请求，并接收响应
            response = httpClient.execute(httpPost);
            result = handleResponse(url, encoding, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    /**
     * 处理响应，获取响应报文
     * 如果希望将连接释放到连接池中，就不能使用close()方法关闭，而是调用EntityUtils.consume()方法
     * @param url
     * @param encoding
     * @param response
     * @return
     * @throws IOException
     */
    private static HttpResponseVo handleResponse(String url, String encoding, CloseableHttpResponse response) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        HttpResponseVo httpResponseVo = new HttpResponseVo();

        try {
            if (response != null) {
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    // 获取响应实体
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        br = new BufferedReader(new InputStreamReader(entity.getContent(), encoding));
                        String s = null;
                        while ((s = br.readLine()) != null) {
                            sb.append(s);
                        }
                    }
                    // 释放entity
                    EntityUtils.consume(entity);
                }
                httpResponseVo.setCode(response.getStatusLine().getStatusCode());
                httpResponseVo.setContent(sb.toString());
                httpResponseVo.setHeaders(response.getAllHeaders());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return httpResponseVo;
    }

    /**
     * 释放资源,连接池管理的连接关闭不用调用该方法，否则会导致连接重用失效
     * @param httpClient
     * @param response
     */
    private static void closeResource(CloseableHttpClient httpClient, CloseableHttpResponse response) {
        if (response != null) {
            try {
                response.close();
            } catch (IOException e) {
                log.error("-----> 释放response资源异常:" + e.getMessage());
                e.printStackTrace();
            }
        }

        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                log.error("-----> 释放httpclient资源异常:" + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @description: 处理header参数
     * @author: FSL
     * @date: 2023/2/17
     * @param reqHeader
     * @return: java.util.List<org.apache.http.Header>
     */
    private static List<Header> handleHeader(Map<String,String> reqHeader){
        List<Header> headerList = new ArrayList<>(reqHeader.size());
        if (reqHeader != null && reqHeader.keySet().size() > 0){
            for (Map.Entry<String, String> entry : reqHeader.entrySet()) {
                BasicHeader basicHeader = new BasicHeader(entry.getKey(),entry.getValue());
                headerList.add(basicHeader);
            }
        }
        return headerList;
    }
}
