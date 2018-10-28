import Forge from 'node-forge';

import * as Config from './Config.js';


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
			
			return key;
		}else{ 
			return result;
		}
	});
	
}
	
export function genUniqueString(){
	//TODO: pad with userAccountInfo
	return getSha256(new Date().getTime());
}
	
export function getSha256(key){
	var md = Forge.md.sha256.create();
	md.update(key);
	return md.digest().toHex();
}
	
export function isKeyFree(key){
	//TODO: check the key in the java dbs
	return true;
}