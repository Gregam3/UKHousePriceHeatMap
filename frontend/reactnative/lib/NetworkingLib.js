import request from 'superagent';

const ip = 'http://192.168.0.10:8080/';

export function postJSON(extension, jsonFile) {
    console.log("Attempting to post to " + extension);

    //TODO change ip to reference your local/aws server
    request.post(ip + extension)
        .set('Content-Type', 'application/json')
        .send(jsonFile)
        .end(function (err, res) {
            console.log("Response: " + JSON.stringify((err) ? err : res.statusCode));
        });
}

export async function get(extension, data) {
    return await request.get(ip + extension + data).then(res => {
        if (res.body != null) {
            return JSON.stringify(res.body);
        } else {
            return res;
        }
    }).catch(err => {
        return err
    });
}

export function getLandRegistryData(mapPosition) {
    console.log("Attempting to get display data");

    console.log("coordinates: " + JSON.stringify(mapPosition));

    //returns test data, replace get-display-data-test with get-display-data
    return request
        .get(ip + 'land-registry/get-display-data')
        .query({"mapPosition": JSON.stringify(mapPosition)})
        .then(res => {
            return res.body;
        })
        .catch(err => {
            console.log(JSON.stringify(err));
            return null;
        });
}