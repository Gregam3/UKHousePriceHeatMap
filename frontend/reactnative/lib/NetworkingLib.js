import request from 'superagent';

export function postJSON(extension, jsonFile) {

    console.log("Attempting to post to " + extension);

    //TODO change ip to reference your local/aws server
    request.post('http://192.168.0.2:8080/' + extension)
        .set('Content-Type', 'application/json')
        .send(jsonFile)
        .end(function (err, res) {
            console.log("Response: " + JSON.stringify((err) ? err : res.statusCode));
        });
}