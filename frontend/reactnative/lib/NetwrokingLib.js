import request from 'superagent';

export function postJSON(extention, jsonFile){


    request
    //.post('http://192.168.43.8:8080/'+extention)
    .post('http://192.168.0.2:8080/'+extention)
    .set('Content-Type', 'application/json')
    .send(jsonFile)
    .end(function(err, res){
        console.log("Response " + JSON.stringify(res));
        console.log("Error " +JSON.stringify(err));
        //console.log("THIS SHIT AINT WORKING, OR IS IT?!?!?");
    }); 
}