import React, {Component} from 'react';
import {Text, View, StyleSheet} from 'react-native';
import {Location, Permissions} from 'expo';

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
        let displayedText = 'Loading...';

        this.requestAndGetLocationAsync();

        //In js if you just put a variable name in a predicate asserts where it's null or not (not undefined!)
        if (this.state.errorMessage) {
            displayedText = this.state.errorMessage;
        } else if (this.state.location) {


            displayedText =
                '\t\n Longitude: ' + this.state.location.coords.longitude +
                '\t\n Latitude: ' + this.state.location.coords.latitude +
                '\t\n Altitude: ' + this.state.location.coords.altitude;

        }

        return (
            <View style={styles.container}>
                <Text style={styles.heading}> Co-ordinates </Text>
                <Text style={[styles.paragraph, styles.blue]}>{displayedText}</Text>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        alignItems: 'center',
        justifyContent: 'center'
    },
    heading: {
        margin: 5,
        fontSize: 28,
        textAlign: 'center',
        fontWeight: 'bold',
        textDecorationLine: 'underline'

    },
    paragraph: {
        margin: 5,
        fontSize: 18,
        textAlign: 'center',
        fontWeight: 'bold'
    },
});
