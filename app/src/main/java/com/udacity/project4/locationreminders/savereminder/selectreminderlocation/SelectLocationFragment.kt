package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
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
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private val FINE_LOCATION_PERMISSION_REQUEST_CODE = 100
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var marker: Marker? = null
    private lateinit var fusedLocationProvider: FusedLocationProviderClient
    private final var poi : PointOfInterest? = null

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
        mapFragment.getMapAsync(this)

        fusedLocationProvider = LocationServices.getFusedLocationProviderClient(requireContext())

        binding.myButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        if(poi == null){
            Toast.makeText(context,requireActivity().getString(R.string.select_poi),Toast.LENGTH_LONG).show()
        }
        else{
            _viewModel.savePOI(poi)
            findNavController().popBackStack()

        }

//        marker.let {
//            if (it != null) {
//                _viewModel.reminderSelectedLocationStr.value = it.title
//                _viewModel.latitude.value = it.position.latitude
//                _viewModel.longitude.value = it.position.longitude
//            }
//        }
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
            var location = Geocoder(context,Locale.getDefault()).getFromLocation(it.latitude,it.longitude,1)
           if(location.isNotEmpty()){
               val thisLocation: String = location[0].getAddressLine(0)
               val locationPoi = PointOfInterest(it, null, thisLocation)
               addMarkerHere(it)
               marker!!.showInfoWindow()
               poi = locationPoi
           }

        }
        // on POI clicked
        map.setOnPoiClickListener {
            addPoiMarker(it)
            marker!!.showInfoWindow()

        }
        // map style
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
        enableMyCurrentLocation()
    }

    private fun enableMyCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true
            zoomToMyCurrentLocation()
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


    @SuppressLint("MissingPermission")
    private fun zoomToMyCurrentLocation() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                fusedLocationProvider.lastLocation.addOnSuccessListener {
                    if (it != null) {
                        val latLang = LatLng(it.latitude, it.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLang, 15f))
                    }
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {

                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {

                    return
                }
                map.isMyLocationEnabled = true
                zoomToMyCurrentLocation()

            } else {
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            }
        }
    }


}
