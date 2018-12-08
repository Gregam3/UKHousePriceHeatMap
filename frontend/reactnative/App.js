import React, {Component} from 'react';
import {Text, View, StyleSheet, TouchableOpacity,LayoutAnimation, Animated} from 'react-native';
import {Location, Permissions, MapView} from 'expo';
import {Button} from 'react-native';

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

var appInstance;

export default class App extends Component {
    state = {
        location: null,
        errorMessage: null,
        markers: [],
        circleSize: heatmapScaleFactor,
		optionSelected: false
    };

    constructor(props) {
        super(props);
        Auth.loadUserId();
        this.currentMapCoordinates = null;
        this._requestAndGetLocationAsync();
		appInstance = this;
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
                        delta: startingDeltas.longitude * 500
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

    handleMapRegionChange = mapRegion => {
        let currentMapCoordinates = {
            top: mapRegion.longitude + (mapRegion.longitudeDelta / 2),
            bottom: mapRegion.longitude - (mapRegion.longitudeDelta / 2),
            right: mapRegion.latitude + (mapRegion.latitudeDelta / 2),
            left: mapRegion.latitude - (mapRegion.latitudeDelta / 2),
            delta: mapRegion.longitudeDelta * 500
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
			<View style={{flex: 1, zIndex: 0}}>
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
					onPress={this._getDisplayData}
					title="Load elements"
					color="#841584"
				/>
			</View>
			{this.state.optionSelected?
				<OptionWindow/>
				:
				<View style={{flex:1, top: 40, left: 10, position: 'absolute', zIndex:1}} >
				
					<Button
						onPress={() => {this.setState({optionSelected:true})}}
						title="O"
						color="#841584"
					/>
				</View>
			}
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

const slideDuration=500;

class OptionWindow extends Component{
	
	constructor(props){
		super(props);
		this.close = false;
	}
	
	state={
		transAnim: new Animated.Value(0.000001)
	}
	
	
	
	render(){
		return <View style={{flexDirection:'row', top: 0, bottom: 0, left: 0, right: 0, position: 'absolute', zIndex:1}}>
			<Animated.View style={{flex: this.state.transAnim, top: 0, bottom: 0, left: 0, backgroundColor: '#000000', opacity: 0.85}}>
			<OptionPane name="DaveyBoi"/>
			</Animated.View>
			<TouchableOpacity style={{flex: 1, right: 0,width:75}} onPress={this.onPress} ></TouchableOpacity>
			
		</View>
	}
	
	componentDidMount() {
		Animated.timing(
		  this.state.transAnim,
		  {
			toValue: 3,
			duration: slideDuration,
		  }
		).start();
	}
	
	onPress = () => {
		if (this.close){
			console.log("Pressed");
			return;
		} else {
			console.log("Leave");
			
			Animated.timing(
			  this.state.transAnim,
			  {
				toValue: 0,
				duration: slideDuration,
			  }
			).start(() => {appInstance.setState({optionSelected: false})});
			this.close = true;
		}
	}
	
}

class OptionPane extends Component{
	constructor(props){
		super(props);
		this.shouldUpdate = true;
		this.name = props.name;
		console.log(props.name);
		this.options = props.options?props.options: [];
	}
	
	shouldComponentUpdate(nextProps, nextState){
		return this.shouldUpdate;
	}
	
	render(){
		this.shouldUpdate = false;
		return (
			<View style={{flex:1}}>
				<View style={{borderBottomColor:'#FFFFFF', borderBottomWidth:1, left:5, width:250, top:20}}>
					<Text style={{color:'#FFFFFF', fontSize:20}}>{this.name}</Text>
				</View>
			</View>
		);
	}
	
}

class Option extends Component{
	
	constructor(props){
		super(props);
	}
	
	render(){
		
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
