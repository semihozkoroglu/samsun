package com.alper.samsun;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alper.samsun.component.HoloCircularProgressBar;
import com.alper.samsun.data.CurrentPlaceDetail;
import com.alper.samsun.data.LegStep;
import com.alper.samsun.data.Place;
import com.alper.samsun.data.PlaceDetail;
import com.alper.samsun.data.SearchResult;
import com.alper.samsun.data.Way;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements SamsunMapFragment.OnLocationFoundListener {

    private AutoCompleteTextView tvAutoComplete;
    private SamsunMapFragment frMap;
    private HoloCircularProgressBar progressBar;

    /**
     * Request'leri bu queue uzerinden gonderiyoruz
     */
    private RequestQueue mRequestQueue;

    /**
     * Search sonucunda donen cevabi bu class'da tutuyoruz
     */
    private SearchResult mSearchResult;
    private PlaceDetail mPlaceDetail;
    private String mCurrentLocationDescription;
    private Way mWayResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * search esnasinda gosterilen progress
         */
        progressBar = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar);

        /**
         * Search yapilan edittex
         */
        tvAutoComplete = (AutoCompleteTextView) findViewById(R.id.tvAutoComplete);
        /**
         * Klavyeden search'e tikladi ise aramayi baslatalim.
         */
        tvAutoComplete.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    /**
                     * Request'i atmak icin google'in volley servisi kullanildi.
                     */
                    createSearch();
                }
                return false;
            }
        });

        tvAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * Koordinat alma islemi yapiliyor
                 */
                getSelectedPlaceLocation(position);
            }
        });

        /**
         * Haritanin yerlestirilecegi container
         */
        FrameLayout flMapContainer = (FrameLayout) findViewById(R.id.flMapContainer);

        /**
         * Haritayi ekrana yerlestirelim
         */
        frMap = new SamsunMapFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(flMapContainer.getId(), frMap)
                .commit();

        mRequestQueue = Volley.newRequestQueue(getBaseContext());

        /**
         * Location'in bulunmasi sirasinda progress'i gosterelim.
         */
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();

        frMap.stopListener();
    }

    @Override
    protected void onResume() {
        super.onResume();

        frMap.startListener();
    }

    @Override
    protected void onStart() {
        super.onStart();

        frMap.connectApi();
    }

    @Override
    protected void onStop() {
        super.onStop();

        frMap.disconnectApi();
    }

    private void createSearch() {
        /**
         * Search esnasinda progress'i gosterelim
         */
        progressBar.setVisibility(View.VISIBLE);

        String searchedKey = tvAutoComplete.getText().toString();

        StringRequest request = new StringRequest(Request.Method.GET, Constant.getSearchUrl(searchedKey), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                /**
                 * Search geldiginde class'i dolduralim
                 */
                mSearchResult = new Gson().fromJson(s, SearchResult.class);

                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);

                setAutoCompleteAdapter();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Bağlantı Hatası")
                        .setMessage("Bir hata oluştur \n" + volleyError.getMessage())
                        .setCancelable(false)
                        .create().show();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                180000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(request);
    }

    private void setAutoCompleteAdapter() {
        ArrayList<String> searchedList = new ArrayList<>();

        for (Place item : mSearchResult.predictions) {
            searchedList.add(item.description);
        }

        ArrayAdapter adapter = new ArrayAdapter(getBaseContext(), R.layout.spinner_item, searchedList);

        tvAutoComplete.setAdapter(adapter);
        tvAutoComplete.showDropDown();
    }

    /**
     * Autocomplete spinner'indan secilen item'in detay bilgilerin cekmek icin
     * kullanilan servis
     */
    private void getSelectedPlaceLocation(int selectedPos) {
        String placeId = mSearchResult.predictions.get(selectedPos).place_id;

        final String destinationDesription = mSearchResult.predictions.get(selectedPos).description;
        /**
         * Search esnasinda progress'i gosterelim
         */
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.GET, Constant.getPlaceDetailUrl(placeId), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                /**
                 * Search geldiginde class'i dolduralim
                 */
                mPlaceDetail = new Gson().fromJson(s, PlaceDetail.class);

                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);

                createPollylineRequest(destinationDesription);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Bağlantı Hatası")
                        .setMessage("Bir hata oluştur \n" + volleyError.getMessage())
                        .setCancelable(false)
                        .create().show();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                180000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        /**
         * Burda request baslar
         */
        mRequestQueue.add(request);
    }

    @Override
    public void onLocationFounded(Location location) {
        /**
         * Sayfa acildiginda location'in bulunmasi ile progress'i gizleyelim.
         */
        progressBar.setVisibility(View.GONE);

        StringRequest request = new StringRequest(Request.Method.GET, Constant.getCurrentLocationDetail(
                location.getLatitude() + "", location.getLongitude() + ""
        ), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {

                CurrentPlaceDetail currentLocationDetail = new Gson().fromJson(s, CurrentPlaceDetail.class);

                if (currentLocationDetail.results != null && currentLocationDetail.results.size() > 0) {
                    mCurrentLocationDescription = currentLocationDetail.results.get(0).formatted_address;
                }
                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Bağlantı Hatası")
                        .setMessage("Bir hata oluştur \n" + volleyError.getMessage())
                        .setCancelable(false)
                        .create().show();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                180000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(request);
    }

    /**
     * Yol cizmek icin detay koordinat listesinin google 'dan almak icin
     * request atalim.
     */
    private void createPollylineRequest(String destinationDesription) {
        /**
         * Search esnasinda progress'i gosterelim
         */
        progressBar.setVisibility(View.VISIBLE);

        frMap.setStationMarker(new LatLng(mPlaceDetail.result.geometry.location.lat.doubleValue(),
                mPlaceDetail.result.geometry.location.lng.doubleValue()));

        StringRequest request = new StringRequest(Request.Method.GET, Constant.getPointDistanceInfo(
                mCurrentLocationDescription,
                destinationDesription
        ), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                /**
                 * Search geldiginde class'i dolduralim
                 */
                mWayResponse = new Gson().fromJson(s, Way.class);

                /**
                 * Eger yol geldiyse cizdirelim
                 */
                if (mWayResponse.routes != null && mWayResponse.routes.size() > 0) {
                    for (LegStep legStep : mWayResponse.routes.get(0).legs.get(0).steps) {
                        frMap.drawPath(legStep.polyline.points);
                    }
                }
                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Bağlantı Hatası")
                        .setMessage("Bir hata oluştur \n" + volleyError.getMessage())
                        .setCancelable(false)
                        .create().show();
            }
        });

        request.setRetryPolicy(new DefaultRetryPolicy(
                180000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        mRequestQueue.add(request);
    }
}
