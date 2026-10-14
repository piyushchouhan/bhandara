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
        styleJson: String,
        topPadding: Double,
        bottomPadding: Double,
        zones: [IosMapZone]
    ) {
        guard let map = view as? AppMapView else { return }

        // Before moving the camera, so it centres in the part of the map that isn't covered
        let padding = UIEdgeInsets(top: topPadding, left: 0, bottom: bottomPadding, right: 0)
        if map.padding != padding { map.padding = padding }

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

        // Shop zones: the same, for shapes under the pins
        let zoneIds = Set(zones.map { $0.id })
        for (id, polygon) in map.polygons where !zoneIds.contains(id) {
            polygon.map = nil
            map.polygons[id] = nil
        }
        for zone in zones {
            let path = GMSMutablePath()
            for i in 0..<min(zone.latitudes.count, zone.longitudes.count) {
                path.add(CLLocationCoordinate2D(latitude: zone.latitudes[i].doubleValue, longitude: zone.longitudes[i].doubleValue))
            }
            let polygon = map.polygons[zone.id] ?? GMSPolygon()
            polygon.userData = zone.id
            if polygon.path?.encodedPath() != path.encodedPath() { polygon.path = path }
            polygon.fillColor = UIColor(argb: zone.fillArgb)
            polygon.strokeColor = UIColor(argb: zone.strokeArgb)
            polygon.strokeWidth = zone.strokeWidth
            polygon.isTappable = true
            polygon.zIndex = -1 // under the markers
            if polygon.map == nil { polygon.map = map }
            map.polygons[zone.id] = polygon
        }
    }
}

private extension UIColor {
    /// From 0xAARRGGBB
    convenience init(argb: Int64) {
        self.init(
            red: CGFloat((argb >> 16) & 0xFF) / 255,
            green: CGFloat((argb >> 8) & 0xFF) / 255,
            blue: CGFloat(argb & 0xFF) / 255,
            alpha: CGFloat((argb >> 24) & 0xFF) / 255
        )
    }
}

/// The map view plus what the bridge tracks for it
private final class AppMapView: GMSMapView, GMSMapViewDelegate {
    var listener: IosMapListener?
    var markers: [String: GMSMarker] = [:]
    var polygons: [String: GMSPolygon] = [:]
    var cameraId: Int64?
    var styleJson: String?

    func mapView(_ mapView: GMSMapView, didTap marker: GMSMarker) -> Bool {
        if let id = marker.userData as? String { listener?.onPinClick(pinId: id) }
        return true // handled: no default info window
    }

    func mapView(_ mapView: GMSMapView, didTap overlay: GMSOverlay) {
        if let id = overlay.userData as? String { listener?.onZoneClick(zoneId: id) }
    }

    func mapView(_ mapView: GMSMapView, idleAt position: GMSCameraPosition) {
        listener?.onZoomChanged(zoom: position.zoom)
    }
}
