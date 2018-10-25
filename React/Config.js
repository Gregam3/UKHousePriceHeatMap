import {AsyncStorage} from 'react-native'

class Config {
  storeSetting =
      async (key, value) => {
    try {
      await AsyncStorage.setItem(key, value);
      return true;  // check this only runs if sucessful
    } catch (error) {
      console.log('Error saving setting: ' + error);
      return false;
    }
  }

  retrieveSetting =
      async (key) => {
    try {
      const value = await AsyncStorage.getItem(key);
      return value;
    } catch (error) {
      console.log('Error loading setting: ' + error);
      return null;
    }
  }

  eraseSetting = async (key) => {
    try {
      await AsyncStorage.removeItem(key);
      return true;  // check this only runs if sucessful
    } catch (error) {
      console.log('Error erasing setting: ' + error);
      return false;
    }
  }
}
