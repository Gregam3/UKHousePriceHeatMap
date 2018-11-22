import React, {Component} from 'react';
import {Text, View, StyleSheet} from 'react-native';
import {Location, Permissions, MapView} from 'expo';

import * as NetLib from './lib/NetworkingLib.js';
import * as Auth from './lib/Auth.js';


/**
 * @author Greg Mitten, Rikkey Paal, Josh Hasan, Antonis Droussiotis
 * gregoryamitten@gmail.com
 */

export default class App extends Component {
    state = {
        location: null,
        errorMessage: null,
        //Markers array filled with dummy data for display
        markers: [
            {
                id:0,
                longitude:-0.13104971498263165,
                latitude:50.84609893155363,
                title:'£100,000',
                description:'1 Brighton Street \n BN1 1AB \n Brighton'
            },
            {
                id:1,
                longitude:-0.133,
                latitude:50.84609893155363,
                title:'Hello',
                description:'World'
            },
            {
                id:2,
                longitude:-0.16,
                latitude:50.88,
                title:'Another',
                description:'One'
            }
        ]
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

                var timeDiff = new Date() - this.lastSent;
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

        NetLib.postJSON('location/add-location-data/', locationData);
    };

    render() {
        let displayedText = 'Fetching position...';

        let latitude = null;
        let longitude = null;

        this.requestAndGetLocationAsync();

        let longitudeDelta = this.state.longitudeDelta
        let latitudeDelta = this.state.latitudeDelta

        if (this.state.longitudeDelta && this.state.latitudeDelta <= 0.05 ){
          return <ZoomLow/>;
        }
        else  if (this.state.longitudeDelta && this.state.latitudeDelta > 0.05){
          return <ZoomMax/>
        }
        if (this.state.errorMessage) {
            displayedText = this.state.errorMessage;
        } else if (this.state.location) {
            latitude = this.state.location.coords.latitude;
            longitude = this.state.location.coords.longitude;

            displayedText =
                '\t\n Longitude: ' + longitude +
                '\t\n Latitude: ' + latitude
        }

        return (
            (latitude && longitude) ?
                <View style={{marginTop: 0, flex: 1, backgroundColor: '#242f3e'}}>
                    <View style={{flex: 1, flexDirection: 'row'}}>
                        <Text style={styles.coordinatesText}>{displayedText}</Text>
                    </View>
                    <MapView
                        style={{flex: 7}}
                        showsMyLocationButton={true}
                        showsUserLocation={true}
                        provider={MapView.PROVIDER_GOOGLE}
                        customMapStyle={darkMapStyle}
                        initialRegion={{
                            longitude: longitude,
                            latitude: latitude,
                            latitudeDelta: 0.0006,
                            longitudeDelta: 0.002
                        }}
                    >

                      {this.state.markers.map(marker => (
                        false ? (
                            <MapView.Circle
                                key={marker.id}
                                center={{longitude:marker.longitude, latitude:marker.latitude}}
                                radius={100}
                                strokeColor={'#FF0000'}
                                fillColor={'rgba(255,0,0,0.5)'}
                            />)
                        : (<MapView.Marker
                                key={marker.id}
                                coordinate={{longitude:marker.longitude, latitude:marker.latitude}}
                                title={marker.title}
                                description={marker.description}
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
