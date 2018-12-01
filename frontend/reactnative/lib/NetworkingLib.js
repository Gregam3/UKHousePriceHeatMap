import request from 'superagent';

const ip = 'http://192.168.0.10:8080/';

const networkLogging = true;

export function postJSON(extension, jsonFile) {
    if(networkLogging) log("Attempting to post to " + extension);

    //TODO change ip to reference your local/aws server
    request.post(ip + extension)
        .set('Content-Type', 'application/json')
        .send(jsonFile)
        .end(function (err, res) {
            if(networkLogging) log("Response: " + JSON.stringify((err) ? err : res.statusCode));
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
    if(networkLogging) log("Fetching display data from " + ip + 'land-registry/get-display-data');
    //returns test data, replace get-display-data-test with get-display-data
    return request
        .get(ip + 'land-registry/get-display-data')
        .query({"mapPosition": JSON.stringify(mapPosition)})
        .then(res => {
            if(networkLogging) log('Successfully fetched ' + res.body.length + ' elements');
            return res.body;
        })
        .catch(err => {
            if(networkLogging) log(JSON.stringify(err));
            return null;
        });
}

function log(message) {
    console.log('NETWORK LOGGING: ' + message)
}