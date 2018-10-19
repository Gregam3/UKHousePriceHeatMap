import React, {Component} from 'react';
import {Text, View, StyleSheet} from 'react-native';
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
    };

    //Must be asynchronous as it has to wait for permissions to be accepted
    requestAndGetLocationAsync = async () => {
        //This lines generates the permission pop-up
        let {status} = await Permissions.askAsync(Permissions.LOCATION);
        if (status !== 'granted') {
            this.setState({
                errorMessage: 'Permission to access location was denied',
            });
        }

        let location = await Location.getCurrentPositionAsync();
        //All "state" in react must be in {} I believe
        this.setState({location});
    };

    render() {
        let displayedText = 'Fetching position...';

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
            <View style={styles.container}>
                <Text style={styles.paragraph}>{displayedText}</Text>
                <MapView
                    style={{ flex: 7 }}
                    showsMyLocationButton={true}
                    showsUserLocation={true}
                    initialRegion={{
                        longitude: longitude,
                        latitude: latitude,
                        latitudeDelta: 0.00922,
                        longitudeDelta: 0.00421
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
    container: {
        flex: 1,
        backgroundColor: '#F2F1EF'
    },
    heading: {
        margin: 5,
        fontSize: 28,
        fontWeight: 'bold',
        textDecorationLine: 'underline',

    },
    paragraph: {
        margin: 5,
        fontSize: 18,
        fontWeight: 'bold',
        flex:1
    },
});
