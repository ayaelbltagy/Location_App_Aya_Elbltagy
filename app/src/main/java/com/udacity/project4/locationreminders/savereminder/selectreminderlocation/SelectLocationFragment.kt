package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private val FINE_LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var marker: Marker? = null
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private final var poi: PointOfInterest? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.googleMap) as SupportMapFragment
        mapFragment.let {
            mapFragment.getMapAsync(this)
        }


        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.myButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        if (poi == null) {
            Toast.makeText(
                context,
                requireActivity().getString(R.string.select_poi),
                Toast.LENGTH_LONG
            ).show()
        } else {
            _viewModel.savePOI(poi)
            findNavController().popBackStack()

        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NONE
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    private fun addMarkerHere(lat: LatLng) {
        val snippet = String.format(
            Locale.getDefault(),
            "Lat: %1$.5f, Long: %2$.5f",
            lat.latitude,
            lat.longitude
        )
        marker?.remove()
        marker = map.addMarker(
            MarkerOptions().position(lat)
                .title(getString(R.string.dropped_pin))
                .snippet(snippet)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

    }

    private fun addPoiMarker(poi: PointOfInterest) {
        marker?.remove()
        marker = map.addMarker(
            MarkerOptions()
                .position(poi.latLng)
                .title(poi.name)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )
        this.poi = poi
    }

    override fun OnMapReady(googleMap: GoogleMap) {

    }

    override fun onMapReady(p0: GoogleMap?) {
        map = p0!!
        // on map  marker clicked
        map.setOnMapLongClickListener {
            var location =
                Geocoder(context, Locale.getDefault()).getFromLocation(it.latitude, it.longitude, 1)
            if (location.isNotEmpty()) {
                val thisLocation: String = location[0].getAddressLine(0)
                val locationPoi = PointOfInterest(it, null, thisLocation)
                addMarkerHere(it)
//                marker?.remove()
//                marker = map.addMarker(
//                    MarkerOptions().position(locationPoi.latLng)
//                        .title(locationPoi.name)
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//                )
                marker!!.showInfoWindow()
                poi = locationPoi
            }

        }
        // on POI clicked
        map.setOnPoiClickListener {
            addPoiMarker(it)
            marker!!.showInfoWindow()
            poi = it
        }
        // map style
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        enableMyCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    private fun enableMyCurrentLocation() {
        if (isPremissionGrante()) {
            map.isMyLocationEnabled = true
            zoomToMyCurrentLocation(true)
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

            // if you fail to get my current location press retry will request permission again
            Snackbar.make(
                requireActivity().findViewById(R.id.content),
                R.string.location_required_error,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.retry) {
                    requestPermissions(
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                        FINE_LOCATION_PERMISSION_REQUEST_CODE
                    )
                }

        } else {

            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                FINE_LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun isPremissionGrante(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

    }

    @SuppressLint("MissingPermission")
    private fun zoomToMyCurrentLocation(isEnabled: Boolean) {
        var locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        // on failed
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && isEnabled) {
                startIntentSenderForResult(
                    exception.resolution.intentSender,
                    REQUEST_TURN_DEVICE_LOCATION_ON,
                    null,
                    0,
                    0,
                    0,
                    null
                )
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    zoomToMyCurrentLocation(true)
                }.show()
            }
        }
        // on Success
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {

                fusedLocationProvider.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latLang = LatLng(location.latitude, location.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 15f))
                    }
                }

            }
        }


    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                map.isMyLocationEnabled = true
                zoomToMyCurrentLocation(false)

            } else {
                Snackbar.make(
                    requireActivity().findViewById(android.R.id.content),
                    R.string.location_required_error,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings) {
                        startActivity(Intent().apply {
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        })
                    }.show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TURN_DEVICE_LOCATION_ON -> {
                zoomToMyCurrentLocation(false)
            }
        }
    }

    companion object {
        private const val REQUEST_TURN_DEVICE_LOCATION_ON = 1002
    }
}
