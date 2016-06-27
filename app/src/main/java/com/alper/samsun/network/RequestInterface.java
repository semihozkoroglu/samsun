package com.alper.samsun.network;

import com.alper.samsun.data.CurrentPlaceDetail;
import com.alper.samsun.data.PlaceDetail;
import com.alper.samsun.data.SearchResult;
import com.alper.samsun.data.Way;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by semihozkoroglu on 08/06/16.
 */
public interface RequestInterface {

    @GET
    Call<SearchResult> getSearchResult(@Url String url);

    @GET
    Call<PlaceDetail> getPlaceDetail(@Url String url);

    @GET
    Call<CurrentPlaceDetail> getCurrentPlaceDetail(@Url String url);

    @GET
    Call<Way> getPointDistanceInfo(@Url String url);
}