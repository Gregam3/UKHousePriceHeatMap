import request from 'superagent';

export function postJSON(extention, jsonFile){
    console.log("POST TRIGGERED");
    request
    //.post('http://192.168.0.10:8000/'+extention)
    .post('localhost:8080/'+extention)
    .set('Content-Type', 'application/json')
    .send(jsonFile)
    .end(function(err, res){
        //console.log(err.text);
        //console.log(res.text);
        console.log("THIS SHIT AINT WORKING, OR IS IT?!?!?");
    });
}