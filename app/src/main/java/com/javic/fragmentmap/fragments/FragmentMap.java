package com.javic.fragmentmap.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.javic.fragmentmap.R;
import com.javic.fragmentmap.util.Constants;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class FragmentMap extends Fragment implements
        OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = FragmentMap.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    public static final int ALERT_ADDRESS_RESULT_RECIVER = 0;
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 1;

    private Context mContext;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LatLng userPosition;

    // FragmentMap UI
    private View mView;
    private GoogleMap mGoogleMap;
    private MapView mapView;


    private Snackbar mSnackBar;
    private Snackbar mSnackBarPermisions;

    public FragmentMap() {
        // Required empty public constructor
    }

    /**
     * @param
     * @return A new instance of fragment FragmentMap.
     */
    public static FragmentMap newInstance() {
        FragmentMap fragment = new FragmentMap();

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(mContext);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mapView = (MapView) mView.findViewById(R.id.map);

        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            //Biulding the GoogleMap
            if (mapView != null) {
                // Initialise the MapView
                mapView.onCreate(null);
                mapView.onResume();

                // Set the map ready callback to receive the GoogleMap object
                mapView.getMapAsync(this);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        //First we need to check if the GoogleMap was not created in OnCreate
        if (mGoogleMap == null) {

            // We need to check availability of play services
            if (checkPlayServices()) {

                if (mGoogleApiClient == null) {
                    // Building the GoogleApi client
                    buildGoogleApiClient();
                }


                //Biulding the GoogleMap
                if (mapView != null) {
                    // Initialise the MapView
                    mapView.onCreate(null);
                    mapView.onResume();

                    // Set the map ready callback to receive the GoogleMap object
                    mapView.getMapAsync(this);
                }
            }
        }

        //GoogleMap exist
        else {
            if (!mayRequestLocation()) {
                return;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mGoogleMap != null) {

            if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(false);
            }
        }
    }


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(mContext);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(getActivity(), result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(mContext);

        mGoogleMap = googleMap;

        setUpGoogleMap();

        mGoogleApiClient.connect();

    }


    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        Log.i(TAG, "Change Camera to Latitude:" + cameraPosition.target.latitude + " Longitude: " + cameraPosition.target.longitude);

        userPosition = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
    }

    /**
     * Method to display the location on UI
     */
    private void getUserLocation() {

        if (!mayRequestLocation()) {

            userPosition = new LatLng(34.0089919, -118.4996126);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

            return;
        }

        if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        if (mLastLocation != null) {

            userPosition = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, Constants.USER_ZOOM));

        } else {
            showAlert(ALERT_ADDRESS_RESULT_RECIVER);
        }

    }

    private boolean mayRequestLocation() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            if (!mGoogleMap.isMyLocationEnabled()) {
                mGoogleMap.setMyLocationEnabled(true);
            }

            return true;
        }

        if (ContextCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (!mGoogleMap.isMyLocationEnabled()) {
                mGoogleMap.setMyLocationEnabled(true);
            }
            return true;
        }

        if (shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {

            if (mSnackBarPermisions == null) {
                mSnackBarPermisions = Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });
                mSnackBarPermisions.show();
            } else {
                if (!mSnackBarPermisions.isShown()) {
                    mSnackBarPermisions.show();
                }
            }

        } else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_ACCESS_COARSE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            }
        } else {
            if (mSnackBarPermisions == null) {
                mSnackBarPermisions = Snackbar.make(mView, R.string.permission_access_coarse_location, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.action_snack_permission_access_coarse_location, new View.OnClickListener() {
                            @Override
                            @TargetApi(Build.VERSION_CODES.M)
                            public void onClick(View v) {
                                requestPermissions(new String[]{ACCESS_COARSE_LOCATION}, REQUEST_PERMISSION_ACCESS_COARSE_LOCATION);
                            }
                        });

            }
        }


    }

    /**
     * Google api callback methods
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Once connected with google api, get the location
        getUserLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    public void setUpGoogleMap() {

        if (mGoogleMap != null) {

            mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
            mGoogleMap.getUiSettings().setCompassEnabled(false);
            mGoogleMap.setOnCameraChangeListener(this);
        }
    }

    public void showAlert(final int action) {

        String title, message, positive_btn_title, negative_btn_title;

        if (action == ALERT_ADDRESS_RESULT_RECIVER) {
            title = mContext.getResources().getString(R.string.location_alert_title);
            message = mContext.getResources().getString(R.string.location_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.location_alert_pos_btn);
            negative_btn_title = mContext.getResources().getString(R.string.location_alert_neg_btn);
        } else {
            title = mContext.getResources().getString(R.string.permission_alert_title);
            message = mContext.getResources().getString(R.string.permission_alert_message);
            positive_btn_title = mContext.getResources().getString(R.string.open_location_settings);
            negative_btn_title = mContext.getResources().getString(R.string.location_alert_neg_btn);
        }

        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setPositiveButton(positive_btn_title, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

                if (action == 0) {
                    getUserLocation();
                } else {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(myIntent);
                }

            }
        });

        dialog.setNegativeButton(negative_btn_title, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
            }
        });
        dialog.show();
    }


}