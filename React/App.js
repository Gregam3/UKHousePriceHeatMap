import React, {Component} from 'react';
import {Text, View, StyleSheet} from 'react-native';
//import BackgroundGeolocation from 'react-native-background-geolocation';
import {Location, Permissions, MapView} from 'expo';
import request from 'superagent';

/**
 * @author Greg Mitten, Rikkey Paal, Baher Elgalfy
 * gregoryamitten@gmail.com
 */

export default class App extends Component {
    // constructor(props){
    //     super(props);
    //     this.subscribeToLocationAsync();
    //     console.log("HELOOOOOOOOO????");
    // }

    state = {
        location: null,
        errorMessage: null
    };

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
            } else {
                this.setState({
                    errorMessage: 'Location could not be determined.'
                });
            }
        }
    };

    //
    subscribeToLocationAsync = async () => {
        // let location;

        // locationPromise = await Location.watchPositionAsync({
        //     enableHighAccuracy: true,
        //     timeInterval: 60000
        //     }, (errorCode) => {
        //     console.log('[watchPosition] ERROR -', errorCode);
        //     }, {
        //     interval: 1000
        //     }).then(function(location){
        //     console.log(location.coords.latitude + " " + location.coords.longitude);


        //     if (location) {
        //         this.setState({location});
        //     } else {
        //         this.setState({
        //             errorMessage: ')Location could not be determined.'
        //         });
        //     }
        //     }).catch(function(error) {
        //     console.log('There has been a problem with your fetch operation: ' + error.message);
        //         // ADD THIS THROW error
        //         throw error;
        //     });

        const getLocation = (location) => {
            console.log("Test Log " + location);

            request
            .post('http://192.168.0.10:8000/simulation/run/')
            .set('Content-Type', 'application/json')
            .send(location)
            .end(function(err, res){
                console.log(err.text);
                console.log(res.text);
            });
        }
          
        const locationPromise = await Location.watchPositionAsync({
            enableHighAccuracy: true,
            timeInterval: 15000
            }, getLocation);
    
        //location = locationPromise.resolve();

        // BackgroundGeolocation.onProviderChange(providerChangeEvent => {
        //     console.log('[providerchange] ', provider.enabled, provider.status, provider.network, provider.gps);
        // });

        // BackgroundGeolocation.ready({
        //     url: 'http://192.168.0.10:8000/simulation/run/'
        // });

        // BackgroundGeolocation.watchPosition((location) => {
        //     console.log('[watchPosition] -', location);
        // }, (errorCode) => {
        //     console.log('[watchPosition] ERROR -', errorCode);
        // }, {
        //     interval: 15000,
        //     desiredAccuracy: BackgroundGeolocation.DESIRED_ACCURACY_HIGH,
        //     persist: true,
        //     timeout: 60000
        // }, getLocation);
    };

    render() {
        let displayedText = 'Fetching position...';

        let latitude = null;
        let longitude = null;

        this.requestAndGetLocationAsync();
        this.subscribeToLocationAsync();

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
                        }}/>
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