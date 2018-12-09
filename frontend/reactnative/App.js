import React, {Component} from 'react';
import {Text, View, StyleSheet} from 'react-native';
import {Location, Permissions, MapView} from 'expo';
import {Button} from 'react-native';
import { Constants, WebBrowser } from 'expo';

import 'global'

import * as NetLib from './lib/NetworkingLib.js';
import * as Auth from './lib/Auth.js';


/**
 * @author Greg Mitten, Rikkey Paal, Josh Hasan, Antonis Droussiotis
 * gregoryamitten@gmail.com
 */

const startingDeltas = {
    latitude: 0.012,
    longitude: 0.04,
};

const AGGREGATION_LEVELS = {
    addresses: 0,
    postcodes: 15,
    heatmap: 500
};

//Turn to false to disable logging
const logging = true;

// min time without map movement before map will update
const waitTimeBeforeUpdate = 2000;
// number used to scale up the minimum size of the circlesin the heatmap
const heatmapScaleFactor = 100;
export default class App extends Component {
    state = {
        location: null,
        errorMessage: null,
        markers: [],
        circleSize: heatmapScaleFactor
    };

    constructor(props) {
        super(props);
        Auth.loadUserId();
        this.currentMapCoordinates = null;
        this._requestAndGetLocationAsync();
    }

    //Must be asynchronous as it has to wait for permissions to be accepted
    _requestAndGetLocationAsync = async () => {
        let location;

        let {status} = await Permissions.askAsync(Permissions.LOCATION);
        if (status !== 'granted') {
            this.setState({
                errorMessage: 'Location access must be granted.'
            });
        } else {
            location = await Location.getCurrentPositionAsync();

            if (location) {
                this.setState({location});

                if (!this.currentMapCoordinates) {


                    let currentMapCoordinates = {
                        top: location.coords.longitude + startingDeltas.longitude,
                        bottom: location.coords.longitude - startingDeltas.longitude,
                        right: location.coords.latitude + startingDeltas.latitude,
                        left: location.coords.latitude - startingDeltas.latitude,
                        delta: startingDeltas.longitude * 500,
                        mapLat: location.coords.latitude,
                        mapLon: location.coords.longitude
                    };

                    this.currentMapCoordinates = currentMapCoordinates;
                }
            } else {
                this.setState({
                    errorMessage: 'Location could not be determined.'
                });
            }
        }
    };

    _getDisplayData = async () => {
        if (this.currentMapCoordinates) {
            let markers = await NetLib.getLandRegistryData(this.currentMapCoordinates);

            if (markers) {
                let circleSize = heatmapScaleFactor * (this.currentMapCoordinates.delta / 30);

                log('Set state for new markers and new circleSize');
                this.setState({markers, circleSize});
            }
        }
    };

    _handleStreetViewButtonPress = async () => {

        let streetResult = await WebBrowser.openBrowserAsync('https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=' + this.currentMapCoordinates.mapLat + ',' + this.currentMapCoordinates.mapLon);
        this.setState({streetResult});
    };

    handleMapRegionChange = mapRegion => {
        let currentMapCoordinates = {
            top: mapRegion.longitude + (mapRegion.longitudeDelta / 2),
            bottom: mapRegion.longitude - (mapRegion.longitudeDelta / 2),
            right: mapRegion.latitude + (mapRegion.latitudeDelta / 2),
            left: mapRegion.latitude - (mapRegion.latitudeDelta / 2),
            delta: mapRegion.longitudeDelta * 500,
            mapLat: mapRegion.latitude,
            mapLon: mapRegion.longitude
        };
        this.currentMapCoordinates = currentMapCoordinates;

    };

    render() {
        let displayedText = 'Fetching position...';

        let latitude = null;
        let longitude = null;

        if (this.state.errorMessage) {
            displayedText = this.state.errorMessage;
        } else if (this.state.location) {
            latitude = this.state.location.coords.latitude;
            longitude = this.state.location.coords.longitude;
        }

        return (
            (latitude && longitude) ?
                this.drawMapWithData(longitude, latitude)
                :
                <Text style={styles.centerText}>{displayedText}</Text>
        );
    }

    drawMapWithData(longitude, latitude) {
        return <View style={{marginTop: 0, flex: 1, backgroundColor: '#242f3e'}}>
            <MapView
                style={{flex: 1}}
                showsMyLocationButton={true}
                showsUserLocation={true}
                provider={MapView.PROVIDER_GOOGLE}
                customMapStyle={darkMapStyle}
                initialRegion={{
                    longitude: longitude,
                    latitude: latitude,
                    latitudeDelta: startingDeltas.latitude,
                    longitudeDelta: startingDeltas.longitude
                }}
                onRegionChange={this.handleMapRegionChange}
            >

                {
                    this.state.markers.length > AGGREGATION_LEVELS.heatmap ? (
                        this.drawHeatmap()
                    ) : (
                        this.drawMarkers()
                    )}

            </MapView>
            <Button
                onPress={this._handleStreetViewButtonPress}
                title="Street View"
            />
            <Button
                onPress={this._getDisplayData}
                title="Load elements"
                color="#841584"
            />
        </View>
    }

    drawHeatmap() {
        if (logging) log("Rendering " + this.state.markers.length + " heatmap data points");

        return this.state.markers.map(marker => (
            <MapView.Circle
                key={marker.id}
                center={{longitude: marker.longitude, latitude: marker.latitude}}
                radius={Math.max(this.state.circleSize, marker.radius)}
                strokeColor={marker.colour.hex}
                fillColor={marker.colour.rgba}
            />
        ))
    }

    drawMarkers() {
        if (logging) log("Rendering " + this.state.markers.length + " markers");

        return this.state.markers.map(marker => (
            <MapView.Marker
                key={marker.id}
                coordinate={{
                    longitude: parseFloat(marker.mappings.longitude),
                    latitude: parseFloat(marker.mappings.latitude)
                }}
                title={(!marker.mappings.street) ?
                    "Average Price: £" + marker.mappings.pricePaid :
                    "£" + marker.mappings.pricePaid + " on " + marker.mappings.transactionDate}

                description={(!marker.mappings.street) ?
                    marker.mappings.postcode :
                    marker.mappings.paon + " " + marker.mappings.street + " " + marker.mappings.town}
                pinColor={marker.colour.hex}
            />
        ))
    }
}

function log(message) {
    console.log('APP LOGGING: ' + message)
}

const styles = StyleSheet.create({
    centerText: {
        marginTop: 300,
        marginLeft: 120,
        fontSize: 40,
    },
    coordinatesText: {
        flex: 5,
        margin: 5,
        fontSize: 18,
        fontWeight: 'bold',
        color: 'white',
        textAlign: 'center'
    }
});

//Can easily be customised here https://mapstyle.withgoogle.com/, dump generated JSON in array
const darkMapStyle = [
    {
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#242f3e"
            }
        ]
    },
    {
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "color": "#746855"
            }
        ]
    },
    {
        "elementType": "labels.text.stroke",
        "stylers": [
            {
                "color": "#242f3e"
            }
        ]
    },
    {
        "featureType": "administrative.locality",
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "color": "#d59563"
            }
        ]
    },
    {
        "featureType": "poi",
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "color": "#d59563"
            }
        ]
    },
    {
        "featureType": "poi.park",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#263c3f"
            }
        ]
    },
    {
        "featureType": "poi.park",
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "color": "#6b9a76"
            }
        ]
    },
    {
        "featureType": "road",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#38414e"
            }
        ]
    },
    {
        "featureType": "road",
        "elementType": "geometry.stroke",
        "stylers": [
            {
                "color": "#212a37"
            }
        ]
    },
    {
        "featureType": "road",
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "color": "#9ca5b3"
            }
        ]
    },
    {
        "featureType": "road.highway",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#746855"
            }
        ]
    },
    {
        "featureType": "road.highway",
        "elementType": "geometry.stroke",
        "stylers": [
            {
                "color": "#1f2835"
            }
        ]
    },
    {
        "featureType": "road.highway",
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "color": "#f3d19c"
            }
        ]
    },
    {
        "featureType": "transit",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#2f3948"
            }
        ]
    },
    {
        "featureType": "transit.station",
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "color": "#d59563"
            }
        ]
    },
    {
        "featureType": "water",
        "elementType": "geometry",
        "stylers": [
            {
                "color": "#17263c"
            }
        ]
    },
    {
        "featureType": "water",
        "elementType": "labels.text.fill",
        "stylers": [
            {
                "color": "#515c6d"
            }
        ]
    },
    {
        "featureType": "water",
        "elementType": "labels.text.stroke",
        "stylers": [
            {
                "color": "#17263c"
            }
        ]
    }
];
