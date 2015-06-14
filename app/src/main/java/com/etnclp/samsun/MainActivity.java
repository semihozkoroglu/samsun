package com.etnclp.samsun;

import android.graphics.Color;
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

import com.etnclp.samsun.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.etnclp.samsun.component.HoloCircularProgressBar;
import com.etnclp.samsun.data.LegStep;
import com.etnclp.samsun.data.Options;
import com.etnclp.samsun.data.Place;
import com.etnclp.samsun.data.PlaceDetail;
import com.etnclp.samsun.data.SearchResult;
import com.etnclp.samsun.data.Step;
import com.etnclp.samsun.data.Way;
import com.google.gson.Gson;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.nispok.snackbar.listeners.ActionClickListener;
import com.nispok.snackbar.listeners.EventListener;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements SamsunMapFragment.OnLocationFoundListener, RouteDialog.OnItemSelectedListener {

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
    private Way mWayResponse;
    /**
     * Local servis sonucunda durak koordinatlari ve bilgileri
     */
    private Options mWayOptions;

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
                    createSearh();
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

    private void createSearh() {
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
            }
        });

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

                getBusRequest();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                /**
                 * Search bitince progress'i gizleyelim
                 */
                progressBar.setVisibility(View.GONE);
            }
        });

        /**
         * Burda request baslar
         */
        mRequestQueue.add(request);
    }

    private void getBusRequest() {
        progressBar.setVisibility(View.VISIBLE);

        Location location = frMap.getLocation();

        StringRequest request = new StringRequest(Request.Method.GET, Constant.getBusService(location.getLatitude(), location.getLongitude(),
                mPlaceDetail.result.geometry.location.lat, mPlaceDetail.result.geometry.location.lng), new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                mWayOptions = new Gson().fromJson(s, Options.class);
                /**
                 * Search geldiginde class'i dolduralim
                 */
                RouteDialog dialog = new RouteDialog(MainActivity.this, getRouteSelectionList(), MainActivity.this);
                dialog.show();

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
            }
        });

        mRequestQueue.add(request);
    }

    @Override
    public void onLocationFounded() {
        /**
         * Sayfa acildiginda location'in bulunmasi ile progress'i gizleyelim.
         */
        progressBar.setVisibility(View.GONE);
    }

    private ArrayList<String> getRouteSelectionList() {
        ArrayList<String> items = new ArrayList<>();

        for (ArrayList<Step> innerItems : mWayOptions.Option) {
            if (innerItems.size() > 1)
                items.add(innerItems.get(0).ToVehicle + " (Aktarmalı)");
            else
                items.add(innerItems.get(0).ToVehicle);
        }

        return items;
    }

    /**
     * Route dialog'dan secilen guzargah icin bu method tetikleniyor
     */

    @Override
    public void onItemSelected(int position) {
        ArrayList<Step> selectedWay = mWayOptions.Option.get(position);

        if (selectedWay.size() > 0) {
            /**
             * Gidecegi duragin uzakligini cizdirmek icin request atak
             */
            createPollylineRequest(selectedWay.get(0));

            frMap.setStationMarker(selectedWay);

            showedItemPos = 0;
            showWayOptionTexts(selectedWay, 0);
        }
    }

    int showedItemPos = 0;

    private void showWayOptionTexts(final ArrayList<Step> selectedWay, int pos) {
        if (selectedWay.size() > pos) {
            if (selectedWay.size() - 1 == pos) {
                showSnackBar(selectedWay.get(showedItemPos).FromStation + " in " + selectedWay.get(showedItemPos).ToStation + " " + selectedWay.get(showedItemPos).ToVehicle, ActionType.DONE);
            } else {
                showSnackBar(selectedWay.get(showedItemPos).FromStation + " in " + selectedWay.get(showedItemPos).ToStation + " " + selectedWay.get(showedItemPos).ToVehicle, ActionType.NEXT, new SnackbarActionListener() {
                    @Override
                    public void onNextActionReady() {
                        showedItemPos++;

                        showWayOptionTexts(selectedWay, showedItemPos);
                    }
                });
            }
        }
    }

    /**
     * Yol cizmek icin detay koordinat listesinin google 'dan almak icin
     * request atalim.
     */
    private void createPollylineRequest(Step step) {
        /**
         * Search esnasinda progress'i gosterelim
         */
        progressBar.setVisibility(View.VISIBLE);

        Location location = frMap.getLocation();

        StringRequest request = new StringRequest(Request.Method.GET, Constant.getPointDistanceInfo(
                location.getLatitude() + "", location.getLongitude() + "", step.ToLat, step.ToLong
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
            }
        });

        mRequestQueue.add(request);
    }

    public enum ActionType {
        NEXT,
        DONE
    }

    public interface SnackbarActionListener {
        void onNextActionReady();
    }

    private void showSnackBar(String messageText, final ActionType actionType) {
        showSnackBar(messageText, actionType, null);
    }

    private void showSnackBar(String messageText, final ActionType actionType, final SnackbarActionListener listener) {
        Snackbar mSnackbar = Snackbar.with(getApplicationContext());
        mSnackbar.color(Color.WHITE);
        mSnackbar.swipeToDismiss(false);
        mSnackbar.actionColor(getResources().getColor(R.color.red));
        mSnackbar.textColor(getResources().getColor(R.color.material_deep_teal_500));
        mSnackbar.duration(10000);
        mSnackbar.type(SnackbarType.MULTI_LINE);
        mSnackbar.text(messageText);
        mSnackbar.eventListener(new EventListener() {
            @Override
            public void onShow(Snackbar snackbar) {

            }

            @Override
            public void onShowByReplace(Snackbar snackbar) {

            }

            @Override
            public void onShown(Snackbar snackbar) {

            }

            @Override
            public void onDismiss(Snackbar snackbar) {
                if (listener != null)
                    listener.onNextActionReady();
            }

            @Override
            public void onDismissByReplace(Snackbar snackbar) {

            }

            @Override
            public void onDismissed(Snackbar snackbar) {
            }
        });

        if (actionType == ActionType.NEXT)
            mSnackbar.actionLabel("Devam");
        else if (actionType == ActionType.DONE)
            mSnackbar.actionLabel("Tamam");

        mSnackbar.actionListener(new ActionClickListener() {
            @Override
            public void onActionClicked(Snackbar snackbar) {
                if (actionType == ActionType.NEXT) {

                }
            }
        });

        SnackbarManager.show(mSnackbar, this);
    }
}
