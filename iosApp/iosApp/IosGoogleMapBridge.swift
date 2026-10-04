import GoogleMaps
import Shared
import UIKit

/// Google Maps on iOS for the shared PlatformMap (shared/src/iosMain/.../ui/map/PlatformMap.ios.kt).
final class IosGoogleMapBridge: IosMapBridge {

    func createMapView(listener: IosMapListener) -> UIView {
        let map = AppMapView(options: GMSMapViewOptions())
        map.listener = listener
        map.delegate = map
        map.settings.compassButton = true
        map.settings.myLocationButton = false
        return map
    }

    func updateMapView(
        view: UIView,
        pins: [IosMapPin],
        latitude: Double,
        longitude: Double,
        zoom: Float,
        cameraId: Int64,
        showMyLocation: Bool,
        styleJson: String
    ) {
        guard let map = view as? AppMapView else { return }

        if map.cameraId != cameraId {
            let target = GMSCameraPosition(latitude: latitude, longitude: longitude, zoom: zoom)
            if map.cameraId == nil { map.camera = target } else { map.animate(to: target) }
            map.cameraId = cameraId
        }
        map.isMyLocationEnabled = showMyLocation
        if map.styleJson != styleJson {
            map.mapStyle = try? GMSMapStyle(jsonString: styleJson)
            map.styleJson = styleJson
        }

        // Markers: keep the ones still shown, add new ones, remove the rest
        let ids = Set(pins.map { $0.id })
        for (id, marker) in map.markers where !ids.contains(id) {
            marker.map = nil
            map.markers[id] = nil
        }
        for pin in pins {
            let marker = map.markers[pin.id] ?? GMSMarker()
            marker.userData = pin.id
            marker.position = CLLocationCoordinate2D(latitude: pin.latitude, longitude: pin.longitude)
            if marker.icon !== pin.icon { marker.icon = pin.icon }
            marker.groundAnchor = CGPoint(x: pin.anchorX, y: pin.anchorY)
            if marker.map == nil { marker.map = map }
            map.markers[pin.id] = marker
        }
    }
}

/// The map view plus what the bridge tracks for it
private final class AppMapView: GMSMapView, GMSMapViewDelegate {
    var listener: IosMapListener?
    var markers: [String: GMSMarker] = [:]
    var cameraId: Int64?
    var styleJson: String?

    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        if let id = marker.userData as? String { listener?.onPinClick(pinId: id) }
        return true // handled: no default info window
    }

    func mapView(_ mapView: GMSMapView, idleAt position: GMSCameraPosition) {
        listener?.onZoomChanged(zoom: position.zoom)
    }
}
