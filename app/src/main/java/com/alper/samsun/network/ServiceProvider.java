package com.alper.samsun.network;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by semihozkoroglu on 11/06/16.
 */
public class ServiceProvider {

    private static RequestInterface sProvider;

    /**
     * Initializes {@link RequestInterface}
     */
    public static void initialize() {

        if (sProvider != null) {
            throw new IllegalStateException("You already called initialize method");
        }

        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        /**
         * Retrofit 2.0 uses okHttp3
         * Legacy methods like setRequestInterceptor and setLogLevel are removed, so interception operations
         * should be in okHttp client, and this client is later set to retrofit
         */
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request originalRequest = chain.request();

                        Request newRequest = originalRequest.newBuilder()
                                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.111 Safari/537.36")
                                .build();

                        return chain.proceed(newRequest);
                    }
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        /**
         * Adding converter factory is vital
         * We are using GsonConverterFactory, to serialize responses to our relevant response objects
         */
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://www.google.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        sProvider = retrofit.create(RequestInterface.class);
    }

    /**
     * Returns initialized {@link RequestInterface} object for method calls
     *
     * @return {@link RequestInterface} object
     * @throws NullPointerException if {@link #initialize()} is not called
     */
    public static synchronized RequestInterface getProvider() {
        if (sProvider == null) {
            throw new NullPointerException("You must call ServiceProvider.init() on application class before calling getProvider()");
        }

        return sProvider;
    }
}
