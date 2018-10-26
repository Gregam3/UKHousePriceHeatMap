import { AsyncStorage } from "react-native"

	
export async function storeSetting(key, value) {
  try {
	await AsyncStorage.setItem(key, value);
	return true; // check this only runs if sucessful
  } catch (error) {
	  console.log("Error saving setting: " + error);
	  return false;
  }
}

export async function retrieveSetting(key) {
  try {
	const value = await AsyncStorage.getItem(key);
	return value;
   } catch (error) {
	 console.log("Error loading setting: " + error);
	 return null;
   }
}

export async function eraseSetting(key) {
  try {
	await AsyncStorage.removeItem(key);
	return true; // check this only runs if sucessful
   } catch (error) {
	 console.log("Error erasing setting: " + error);
	 return false;
   }
}
