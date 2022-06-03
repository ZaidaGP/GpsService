package cr.ac.gpsservice.service


import android.annotation.SuppressLint
import android.app.IntentService
import android.content.Intent
import android.os.Looper


import com.google.android.gms.location.*
import cr.ac.gpsservice.db.LocationDatabase
import cr.ac.gpsservice.entity.Location

class GpsService : IntentService("GpsService") {
    lateinit var locationCallback: LocationCallback
    lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationDatabase: LocationDatabase
    private lateinit var locationRequest : LocationRequest

    companion object{
        var GPS= "cr.ac.GpsService.GPS_EVENT"

    }

    override fun onHandleIntent(intent: Intent?) {
        locationDatabase = LocationDatabase.getInstance(this)

        getLocation()
    }


    /**
     * Inicializa los atributos locationCallback y fusedLocationClient
     * Coloca un intervalo de actualización de 1000 y una prioridad de PRIORITY_HIGH_ACCURACY
     * Recibe ubicación de gps mediante un onLocationResult
     * Envía un broadcast con una instancia de localización y la acción gps (cr.ac.gpsservice.GPS.EVENT
     * Guarda la localización en la BD)
     */
    @SuppressLint("MissingPermission")
    fun getLocation(){

        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 10000 // If not here
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        locationCallback = object : LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult) {
                if (locationRequest==null) {
                    return
                }
                for (location in locationResult.locations) {
                    val ubicacion= Location(null,location.latitude,location.longitude)

                    val bcIntent=Intent()
                    bcIntent.setAction(GPS)
                    bcIntent.putExtra("Localizacion", ubicacion)
                    sendBroadcast(bcIntent)

                    locationDatabase.locationDao.insert(
                        Location(null,
                            ubicacion.latitude,
                            ubicacion.longitude))

                    LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
        Looper.loop()

    }

}