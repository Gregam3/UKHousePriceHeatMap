import React, {Component} from 'react';
import {Text, View, StyleSheet, Button} from 'react-native';
import {Location, Permissions, MapView} from 'expo';

// import Toast from 'react-native-smart-toast';

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

export default class App extends Component {
    state = {
        location: null,
        errorMessage: null,
        useDarkTheme: true
    };

    //Must be asynchronous as it has to wait for permissions to be accepted
    requestAndGetLocationAsync = async () => {
        //This lines generates the permission pop-up
        let {status} = await Permissions.askAsync(Permissions.LOCATION);
        if (status !== 'granted') {
            this.setState({
                errorMessage: 'Location access must be granted.',
            });
        }

        let location = await Location.getCurrentPositionAsync();
        //All "state" in react must be in {} I believe
        this.setState({location});
    };

    // swapTheme() {
    //     this.state.useDarkTheme =
    //     render();
    // }

    render() {
        let displayedText = 'Fetching position....';

        let latitude = null;
        let longitude = null;

        this.requestAndGetLocationAsync();

        //For those unfamiliar with JS:
        // if you just put a variable name in a predicate asserts whether it's not null (does not work with 'undefined')
        if (this.state.errorMessage) {
            displayedText = this.state.errorMessage;
        } else if (this.state.location) {
            latitude = this.state.location.coords.latitude;
            longitude = this.state.location.coords.longitude;

            displayedText =
                '\t\n Longitude: ' + this.state.location.coords.longitude +
                '\t\n Latitude: ' + this.state.location.coords.latitude +
                '\t\n Altitude: ' + this.state.location.coords.altitude;
        }


        return (
            (latitude && longitude) ?
                <View style={{
                    marginTop:20,
                    flex: 1,
                    backgroundColor: (this.state.useDarkTheme) ? '#263c3f' : '#F2F1EF'
                }}>
                    <View style={{flex: 1, flexDirection: 'row'}}>
                        <Text style={{
                            flex: 5,
                            fontSize: 18,
                            fontWeight: 'bold',
                            color: (this.state.useDarkTheme) ? 'white' : 'black'
                        }}>{displayedText}</Text>
                        {/*<Button*/}
                            {/*onPress={this.swapTheme}*/}
                            {/*title={"Swap to" + (this.state.useDarkTheme) ? "Light Mode" : "Dark Mode"}*/}
                            {/*style={{*/}
                            {/*}}*/}
                        {/*/>*/}
                    </View>
                    <MapView
                        style={{flex: 7}}
                        showsMyLocationButton={true}
                        showsUserLocation={true}
                        provider={MapView.PROVIDER_GOOGLE}
                        customMapStyle={(this.state.useDarkTheme) ? darkMapStyle : lightMapStyle}
                        initialRegion={{
                            longitude: longitude,
                            latitude: latitude,
                            latitudeDelta: 0.0007,
                            longitudeDelta: 0.0025
                        }}/>
                </View> :
                <Text style={{
                    marginTop: 300,
                    marginLeft: 120,
                    fontSize: 40,
                }}>{displayedText}</Text>
        );
    }
}

const styles = StyleSheet.create({
    container: {},
    heading: {
        margin: 5,
        fontSize: 28,
        fontWeight: 'bold',
        textDecorationLine: 'underline',

    },
    paragraph: {},
    button: {
        fontSize: 38
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

//Empty style creates default style
const lightMapStyle = [];