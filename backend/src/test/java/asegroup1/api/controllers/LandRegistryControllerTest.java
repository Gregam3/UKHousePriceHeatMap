package asegroup1.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.services.landregistry.LandRegistryServiceImpl;

class LandRegistryControllerTest {

	@Test
	void testGetDataToDisplayOnMapValidParameter() {
		try {
			LandRegistryServiceImpl mockedService = mock(LandRegistryServiceImpl.class);

			List<String> items = Arrays.asList("item1", "item2");
			when(mockedService.getPositionInsideBounds(any())).thenReturn(items);
			
			JSONObject object = new JSONObject();
			object.put("top", 1);
			object.put("bottom", 1);
			object.put("left", 1);
			object.put("right", 1);

			assertEquals(items, new LandRegistryController(mockedService).getDataToDisplayOnMap(object).getBody());
			
		} catch (UnirestException | IOException | JSONException e) {
			fail(e);
		}
	}

	@Test
	void testGetDataToDisplayOnMapInvalidParameter() {
		try {
			LandRegistryServiceImpl mockedService = mock(LandRegistryServiceImpl.class);

			List<String> items = Arrays.asList("item1", "item2");
			when(mockedService.getPositionInsideBounds(any())).thenReturn(items);

			// try every possible combination not including all
			for (int i = 0; i < 15; i++) {
				JSONObject object = new JSONObject();
				int tmp = i;
				if (tmp >= 8) {
					object.put("top", 1);
					tmp -= 8;
				}
				if (tmp >= 4) {
					object.put("bottom", 1);
					tmp -= 4;
				}
				if (tmp >= 2) {
					object.put("left", 1);
					tmp -= 2;
				}
				if (tmp >= 1) {
					object.put("right", 1);
				}
				assertEquals(HttpStatus.BAD_REQUEST, new LandRegistryController(mockedService).getDataToDisplayOnMap(object).getStatusCode());
			}

			assertEquals(HttpStatus.BAD_REQUEST, new LandRegistryController(mockedService).getDataToDisplayOnMap(null).getStatusCode());

		} catch (UnirestException | IOException | JSONException e) {
			fail(e);
		}
	}

	@Test
	void testUpdateTransactionValidParameter() {
		LandRegistryServiceImpl mockedService = mock(LandRegistryServiceImpl.class);
		try {
			doNothing().when(mockedService).updatePostcodeDatabase(any());
			// check every valid postcode format
			String[] validPostcode = new String[] { "AA9A 9AA", "A9A 9AA", "A9 9AA", "A99 9AA", "AA9 9AA", "AA99 9AA" };
			for (String postcode : validPostcode) {
				String acc = "";
				for (char c : postcode.toCharArray()) {
					acc += c;
					assertEquals(HttpStatus.OK, new LandRegistryController(mockedService).updateTransactionData(acc).getStatusCode());
				}
			}
			assertEquals(HttpStatus.OK, new LandRegistryController(mockedService).updateTransactionData(null).getStatusCode());
		} catch (IOException | UnirestException e) {
			fail(e);
		}
	}

	@Test
	void testUpdateTransactionInvalidParameter() {
		LandRegistryServiceImpl mockedService = mock(LandRegistryServiceImpl.class);
		try {
			doNothing().when(mockedService).updatePostcodeDatabase(any());
			String[] invalidPostcode = new String[] { "9", ".", "*", "%", "\"", "AA999AA" };
			for (String postcode : invalidPostcode) {
				assertEquals(HttpStatus.BAD_REQUEST, new LandRegistryController(mockedService).updateTransactionData(postcode).getStatusCode());
			}

		} catch (IOException | UnirestException e) {
			e.printStackTrace();
		}
	}


}
