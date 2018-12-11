import 'global'
import {Constants, Location, MapView, Permissions} from 'expo';
import React, {Component} from 'react';
import {Button, Platform, ProgressBarAndroid, ProgressViewIOS, StyleSheet, Text, View, StatusBar} from 'react-native';
import {Overlay} from 'react-native-elements';
import {SearchBar} from 'react-native-elements';

import * as Auth from './lib/Auth.js';
import * as NetLib from './lib/NetworkingLib.js';


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
    heatMap: 500
};

//Turn to false to disable logging
const logging = true;

// number used to scale up the minimum size of the circles in the heat map
const heatMapScaleFactor = 100;

export default class App extends Component {
    state = {
        location: null,
        errorMessage: null,
        markers: [],
        circleSize: heatMapScaleFactor,
        loadingMarkers: false
    };

    constructor(props) {
        super(props);
		StatusBar.setHidden(true);
        this.searchText = null;
        this.displayedText = 'Fetching position...';
        this.lastPosition = {
            latitude: null,
            longitude: null,
            latitudeDelta: startingDeltas.latitude,
            longitudeDelta: startingDeltas.longitude
        };

        Auth.loadUserId();
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
                if (!this.mapBounds) {
                    this.mapBounds = {
                        top: location.coords.longitude + (startingDeltas.longitude / 2),
                        bottom: location.coords.longitude - (startingDeltas.longitude / 2),
                        right: location.coords.latitude + (startingDeltas.latitude / 2),
                        left: location.coords.latitude - (startingDeltas.latitude / 2),
                        delta: startingDeltas.longitude * 500
                    };
                }
            } else {
                this.setState({
                    errorMessage: 'Location could not be determined.'
                });
            }
        }
    };

    _getDisplayData = async () => {
        this.setState({loadingMarkers: true});

        if (this.mapBounds) {
            let markers = await NetLib.getLandRegistryData(this.mapBounds);

            if (markers) {
                this.setState({
                        markers,
                        circleSize: heatMapScaleFactor * (this.mapBounds.delta / 30),
                        //Location must be updated in state here to avoid position reverted to last searched position, alternate solution leads to scrolling on the map
                        location: {
                            coords: this.lastPosition
                        },
                        loadingMarkers: false
                    }
                );
            }
        }
    };

    handleMapRegionChange = mapRegion => {
        //If zoom level does not change mapRegion does not contain these, latitude and longitude always change.
        if (mapRegion.longitudeDelta && mapRegion.latitudeDelta) {
            this.lastPosition.longitudeDelta = mapRegion.longitudeDelta;
            this.lastPosition.latitudeDelta = mapRegion.latitudeDelta;
        }

        this.lastPosition.latitude = mapRegion.latitude;
        this.lastPosition.longitude = mapRegion.longitude;

        this.mapBounds = {
            top: mapRegion.longitude + (this.lastPosition.longitudeDelta / 2),
            bottom: mapRegion.longitude - (this.lastPosition.longitudeDelta / 2),
            right: mapRegion.latitude + (this.lastPosition.latitudeDelta / 2),
            left: mapRegion.latitude - (this.lastPosition.latitudeDelta / 2),
            delta: this.lastPosition.longitudeDelta * 500
        };
    };

    goToLocation = async () => {
        NetLib.getWithPathVariable('location/get-address-coordinates', this.searchText)
            .then(response => {
                let parsedResponse = JSON.parse(response);

                let searchedLocation = {
                    coords: {
                        latitude: parsedResponse.lat,
                        longitude: parsedResponse.lng
                    }
                };

                this.handleMapRegionChange(searchedLocation.coords);

                this.setState({location: searchedLocation});
            }, error => {
                if (logging) log(error);
            });
    };

    updateSearchText = newSearchText => {
        this.searchText = newSearchText;
    };

    render() {
        let latitude = null;
        let longitude = null;

        if (logging) log(JSON.stringify(this.state.location));

        if (this.state.errorMessage) {
            this.displayedText = this.state.errorMessage;
        } else if (this.state.location) {
            latitude = this.state.location.coords.latitude;
            longitude = this.state.location.coords.longitude;
        }

        return (
            (latitude && longitude) ?
                this.drawMapWithData(longitude, latitude)
                :
                <View>
                    <View style={styles.background}/>
                    <Text style={styles.centerText}>{this.displayedText}</Text>
                    {(Platform.OS === 'ios') ?
                        <ProgressViewIOS style={styles.coordinatesLoadStyle}/> :
                        <ProgressBarAndroid style={styles.coordinatesLoadStyle}/>}
                    <View style={styles.background}/>
                </View>
        );
    }

    drawMapWithData(longitude, latitude) {
        return <View style={{flex: 1, backgroundColor: '#242f3e', flexDirection: 'column'}}>
            <View style={{flexDirection: 'row', height:47}}>
                <View style={{flex: 4}}>
                    <SearchBar
                        darkTheme
                        round
                        onChangeText={this.updateSearchText}
                    />
                </View>
                <View style={{flex: 1, backgroundColor: '#841584'}}>
                    <View style={{marginTop: 6}}>
                        <Button
                            styles={{fontSize: 20}}
                            onPress={this.goToLocation}
                            title="→"
                            color="#841584"
                        />
                    </View>
                </View>
            </View>

            <MapView
                style={{flex: 22}}
                showsMyLocationButton={true}
                showsUserLocation={true}
                customMapStyle={darkMapStyle}
                initialRegion={{
                    longitude: longitude,
                    latitude: latitude,
                    latitudeDelta: startingDeltas.latitude,
                    longitudeDelta: startingDeltas.longitude
                }}
                region={{
                    longitude: longitude,
                    latitude: latitude,
                    latitudeDelta: this.lastPosition.latitudeDelta,
                    longitudeDelta: this.lastPosition.longitudeDelta
                }}
                onRegionChange={this.handleMapRegionChange}
            >
                {
                    this.state.markers.length > AGGREGATION_LEVELS.heatMap ? (
                        this.drawHeatMap()
                    ) : (
                        this.drawMarkers()
                    )}

            </MapView>
            <Button
                onPress={this._getDisplayData}
                title="Load elements"
                color="#841584"
            />
            <Overlay
                overlayBackgroundColor='#242f3e'
                height={'18.5%'}
                width={'35%'}
                isVisible={this.state.loadingMarkers}>
                <Text style={styles.loadingText}>Loading elements...</Text>
                {(Platform.OS === 'ios') ?
                    <ProgressViewIOS style={styles.dataLoadStyle}/> :
                    <ProgressBarAndroid style={styles.dataLoadStyle}/>}
            </Overlay>
        </View>
    }

    drawHeatMap() {
        if (logging) log("Rendering " + this.state.markers.length + " heatMap data points");

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
        textAlign: 'center',
        fontSize: 40,
        height: 100,
        color: '#ffffff',
        backgroundColor: '#242f3e',
    },
    loadingText: {
        textAlign: 'center',
        color: '#ffffff'
    },
    background: {
        backgroundColor: '#242f3e',
        height: 275
    },
    dataLoadStyle: {
        backgroundColor: '#242f3e',
        height: '90%'
    },
    coordinatesLoadStyle: {
        height: '15%',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#242f3e'
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
