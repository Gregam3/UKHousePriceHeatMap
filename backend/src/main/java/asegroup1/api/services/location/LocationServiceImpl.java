package asegroup1.api.services.location;

import asegroup1.api.daos.landregistry.LandRegistryDaoImpl;
import asegroup1.api.daos.location.LocationDaoImpl;
import asegroup1.api.models.LocationData;
import asegroup1.api.services.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@Service
public class LocationServiceImpl extends ServiceImpl<LocationData> {

	private LocationDaoImpl locationDao;
	private LandRegistryDaoImpl landRegistryDao;

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Autowired
	public LocationServiceImpl(LocationDaoImpl locationDao, LandRegistryDaoImpl landRegistryDao) {
        super(locationDao);
        this.locationDao = locationDao;
        this.landRegistryDao = landRegistryDao;
    }

	@Override
	public void create(LocationData t) {
		if (locationDao.getLocationDataById(t.getUserId(), t.getTimelog()).size() == 0) {
			super.create(t);
		}else {
			throw new InvalidParameterException("Entry already exists");
		}
		
	}

	public List<LocationData> getLocationData(String userID){
		return locationDao.getUserLocations(userID);
    }

	public JsonNode getAddressCoordinates(String address) throws UnirestException, IOException {
    	//"United Kingdom" appended to help google infer what data is wanted, as we don't support any other country.
		return convertJSONObjectToObjectNode(landRegistryDao.getGeoLocationData(address.replace(" ", "+") + "United+Kingdom"));
	}

	private JsonNode convertJSONObjectToObjectNode(JSONObject jsonObject) throws IOException {
		return OBJECT_MAPPER.readTree(jsonObject.toString());

	}
}
