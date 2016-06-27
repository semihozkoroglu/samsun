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
import com.alper.samsun.network.ServiceProvider;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends ActionBarActivity implements SamsunMapFragment.OnLocationFoundListener {

    private AutoCompleteTextView tvAutoComplete;
    private SamsunMapFragment frMap;
    private HoloCircularProgressBar progressBar;

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

        ServiceProvider.getProvider().getSearchResult(Constant.getSearchUrl(searchedKey))
                .enqueue(new Callback<SearchResult>() {
                    @Override
                    public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                        mSearchResult = response.body();

                        /**
                         * Search bitince progress'i gizleyelim
                         */
                        progressBar.setVisibility(View.GONE);

                        setAutoCompleteAdapter();
                    }

                    @Override
                    public void onFailure(Call<SearchResult> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Bağlantı Hatası")
                                .setMessage("Bir hata oluştu \n" + t.getMessage())
                                .create().show();
                    }
                });
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

        ServiceProvider.getProvider().getPlaceDetail(Constant.getPlaceDetailUrl(placeId))
                .enqueue(new Callback<PlaceDetail>() {
                    @Override
                    public void onResponse(Call<PlaceDetail> call, Response<PlaceDetail> response) {
                        /**
                         * Search geldiginde class'i dolduralim
                         */
                        mPlaceDetail = response.body();

                        /**
                         * Search bitince progress'i gizleyelim
                         */
                        progressBar.setVisibility(View.GONE);

                        createPollylineRequest(destinationDesription);
                    }

                    @Override
                    public void onFailure(Call<PlaceDetail> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Bağlantı Hatası")
                                .setMessage("Bir hata oluştu \n" + t.getMessage())
                                .create().show();
                    }
                });
    }

    @Override
    public void onLocationFounded(Location location) {
        /**
         * Sayfa acildiginda location'in bulunmasi ile progress'i gizleyelim.
         */
        progressBar.setVisibility(View.GONE);

        ServiceProvider.getProvider().getCurrentPlaceDetail(Constant.getCurrentLocationDetail(
                location.getLatitude() + "", location.getLongitude() + ""))
                .enqueue(new Callback<CurrentPlaceDetail>() {
                    @Override
                    public void onResponse(Call<CurrentPlaceDetail> call, Response<CurrentPlaceDetail> response) {
                        CurrentPlaceDetail currentLocationDetail = response.body();

                        if (currentLocationDetail.results != null && currentLocationDetail.results.size() > 0) {
                            mCurrentLocationDescription = currentLocationDetail.results.get(0).formatted_address;
                        }
                        /**
                         * Search bitince progress'i gizleyelim
                         */
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(Call<CurrentPlaceDetail> call, Throwable t) {
                        /**
                         * Search bitince progress'i gizleyelim
                         */
                        progressBar.setVisibility(View.GONE);

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Bağlantı Hatası")
                                .setMessage("Bir hata oluştur \n" + t.getMessage())
                                .create().show();
                    }
                });
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

        ServiceProvider.getProvider().getPointDistanceInfo(Constant.getPointDistanceInfo(
                mCurrentLocationDescription,
                destinationDesription))
                .enqueue(new Callback<Way>() {
                    @Override
                    public void onResponse(Call<Way> call, Response<Way> response) {
                        /**
                         * Search geldiginde class'i dolduralim
                         */
                        mWayResponse = response.body();

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

                    @Override
                    public void onFailure(Call<Way> call, Throwable t) {
                        /**
                         * Search bitince progress'i gizleyelim
                         */
                        progressBar.setVisibility(View.GONE);

                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Bağlantı Hatası")
                                .setMessage("Bir hata oluştur \n" + t.getMessage())
                                .create().show();
                    }
                });
    }
}
