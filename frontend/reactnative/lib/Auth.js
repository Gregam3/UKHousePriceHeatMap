import Forge from 'node-forge';

import * as Config from './Config.js';

let userKey;

export function wipeUserId(){
	Config.eraseSetting("userKey").then(function(result) {
		// log if erase was sucessful or not
		console.log("Erase sucess: " + result);
	});
}
	
export async function loadUserId(){
	// load key
	Config.retrieveSetting("userKey").then(function(result){
		// if no key is stored locally
		if(!result){
			
			console.log("no key found, generating a new one");
			// keep generating keys untill a valid one is found
			var key;
			do{
				key = genUniqueString();
			}while(!isKeyFree(key));
			
			// save valid key to phone
			Config.storeSetting("userKey", key).then(function(result) {
				if(!result){
					console.error("failed to store userKey");
				}
			});
			
			userKey = key;
		}else{ 
			userKey = result;
		}
	});
	
}
	
export function genUniqueString(){
	//TODO: pad with userAccountInfo
	return getSha256(new Date().getTime());
}
	
// Adapted from https://www.npmjs.com/package/node-forge#sha256
export function getSha256(key){
	var messageDigest = Forge.md.sha256.create();
	messageDigest.update(key);
	return messageDigest.digest().toHex();
}
	
export function isKeyFree(key){
	//TODO: check the key in the java dbs
	return true;
}

export function getUserKey(){
	if(!userKey){
		throw "UserKey is not loaded";
	}
	return userKey;
}