import Forge from 'node-forge';

import * as Config from './Config.js';
import * as NetLib from './NetworkingLib.js';

let userKey;

export function wipeUserId(){
	Config.eraseSetting("userKey").then(function(result) {
		// log if erase was sucessful or not
		console.log("Erase sucess: " + result);
	});
}
	
export async function loadUserId(){
	// load key
	var keyNeedsGenning = true;
	await Config.retrieveSetting("userKey").then(function(result){
		// if no key is stored locally
		if(result){
			console.log("Key found");
			userKey = result;
			keyNeedsGenning = false;
		}
	});
	if(keyNeedsGenning){
		console.log("No key found, generating one");
		await genKey();
	}
	
	console.log("Key: " + userKey);
}

async function genKey(){
	var key;
	var loopVar = false;
	do{
		key = genUniqueString();
		let keyFree = await isKeyFree(key);
		var loopVar = keyFree;
	}while(loopVar == false);
	// save valid key to phone
	Config.storeSetting("userKey", key).then(function(result) {
		if(!result){
			console.error("failed to store userKey");
		}
	});
	
	userKey = key;
	return;
}
	
function genUniqueString(){
	//TODO: pad with userAccountInfo
	return getSha256(new Date().getTime());
}
	
// Adapted from https://www.npmjs.com/package/node-forge#sha256
function getSha256(key){
	var messageDigest = Forge.md.sha256.create();
	messageDigest.update(key);
	return messageDigest.digest().toHex();
}
	
async function isKeyFree(key){
	let res = await NetLib.get("user/check-user-exsists/", key);
	var free = (res == "false");
	if(!free){
		return free;
	} else {
		let res = await NetLib.get("user/add-user-data/", key);
		res = res.text;
		return (res == "User Added");
	}
}

export function getUserKey(){
	if(!userKey){
		//console.log("UserKey is not loaded");
		return null;
	}
	return userKey;
}