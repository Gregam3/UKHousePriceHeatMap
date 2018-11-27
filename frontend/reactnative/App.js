import React, {Component} from 'react';
import {Text, View, StyleSheet} from 'react-native';
import {Location, Permissions, MapView} from 'expo';
import 'global'

import * as NetLib from './lib/NetworkingLib.js';
import * as Auth from './lib/Auth.js';


/**
 * @author Greg Mitten, Rikkey Paal, Josh Hasan, Antonis Droussiotis
 * gregoryamitten@gmail.com
 */

const startingDeltas = {
    latitude: 0.0006,
    longitude: 0.002,
};

const AGGREGATION_LEVELS = {
    addresses: 0,
    postcodes: 15,
    heatmap: 300
};

export default class App extends Component {

    state = {
        location: null,
        errorMessage: null,
        markers: [],
        currentMapRegion: null
    };

    constructor(props) {
        super(props);
        this.lastSent = new Date() - 15000;
        Auth.loadUserId();
    }

    //Must be asynchronous as it has to wait for permissions to be accepted
    requestAndGetLocationAsync = async () => {
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

                if (!this.state.currentMapCoordinates) {
                    let currentMapCoordinates = {
                        top: location.coords.longitude + startingDeltas.longitude,
                        bottom: location.coords.longitude - startingDeltas.longitude,
                        right: location.coords.latitude + startingDeltas.latitude,
                        left: location.coords.latitude - startingDeltas.latitude
                    };

                    this.setState({currentMapCoordinates});
                }

                let timeDiff = new Date() - this.lastSent;
                if (timeDiff >= 15000) {
                    this.getLocation(location);
                    this.lastSent = new Date();

                }
            } else {
                this.setState({
                    errorMessage: 'Location could not be determined.'
                });
            }
        }
    };

    getLocation = (location) => {
        let locationData = {
            latitude: location.coords.latitude,
            longitude: location.coords.longitude,
            altitude: location.coords.altitude,
            userId: Auth.getUserKey(),
            timelog: location.timestamp,
            delivered: true
        };
        if (Auth.getUserKey()) {
            NetLib.postJSON('location/add-location-data/', locationData);
        }
    };

    getMarkersAsync = async () => {
        let markers = await NetLib.getLandRegistryData(this.state.currentMapCoordinates);

        if (markers) {
            console.log('Marker size = ' + markers.length);
            this.setState({markers});
        }
    };

    _handleMapRegionChange = mapRegion => {
        let currentMapCoordinates = {
            top: mapRegion.longitude + mapRegion.longitudeDelta,
            bottom: mapRegion.longitude - mapRegion.longitudeDelta,
            right: mapRegion.latitude + mapRegion.latitudeDelta,
            left: mapRegion.latitude - mapRegion.latitudeDelta,
        };

        this.setState({currentMapCoordinates});
    };

    render() {
        let displayedText = 'Fetching position...';

        let latitude = null;
        let longitude = null;

        this.requestAndGetLocationAsync();

        let timeDiff = new Date() - this.lastSent;

        if (timeDiff > 15000) {
            this.getMarkersAsync();
            this.lastSent = new Date();
        }

        if (this.state.errorMessage) {
            displayedText = this.state.errorMessage;
        } else if (this.state.location) {
            latitude = this.state.location.coords.latitude;
            longitude = this.state.location.coords.longitude;
        }

        return (
            (latitude && longitude) ?
                <View style={{marginTop: 0, flex: 1, backgroundColor: '#242f3e'}}>
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
                        onRegionChange={this._handleMapRegionChange}
                    >

                        {this.state.markers.map(marker => (
                            this.state.markers.length > AGGREGATION_LEVELS.heatmap ? (
                                    <MapView.Circle
                                        key={marker.id}
                                        center={{longitude: marker.longitude, latitude: marker.latitude}}
                                        radius={100}
                                        strokeColor={'#FF0000'}
                                        fillColor={'rgba(255,0,0,0.5)'}
                                    />)
                                : (<MapView.Marker
                                    key={marker.id}
                                    coordinate={{
                                        longitude: parseFloat(marker.mappings.longitude),
                                        latitude: parseFloat(marker.mappings.latitude)
                                    }}
                                    title={(this.state.markers.length > AGGREGATION_LEVELS.postcodes) ?
                                        "Average Price: £" + marker.mappings.pricePaid :
                                        marker.mappings.pricePaid + " on " + marker.mappings.transactionDate}

                                    description={(this.state.markers.length > AGGREGATION_LEVELS.postcodes) ?
                                        marker.mappings.postcode :
                                        marker.mappings.paon + " " + marker.mappings.street + " " + marker.mappings.town}
                                />)
                        ))}

                    </MapView>
                </View> :
                <Text style={styles.centerText}>{displayedText}</Text>
        );
    }
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
