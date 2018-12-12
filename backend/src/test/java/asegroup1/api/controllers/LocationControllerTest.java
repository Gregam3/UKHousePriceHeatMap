package asegroup1.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import asegroup1.api.models.LocationData;
import asegroup1.api.models.UserData;
import asegroup1.api.services.location.LocationServiceImpl;
import asegroup1.api.services.user.UserServiceImpl;

class LocationControllerTest {

	@Test
	void testPostAddLocationDataNoUser() {
		UserServiceImpl userServiceImpl = mock(UserServiceImpl.class);
		when(userServiceImpl.get(any())).thenReturn(null);

		LocationData data = new LocationData();
		data.setUserId("test");

		assertEquals(HttpStatus.BAD_REQUEST, new LocationController(null, userServiceImpl).postAddLocationData(data).getStatusCode());
	}

	@Test
	void testPostAddLocationDataUser() {
		UserServiceImpl userServiceImpl = mock(UserServiceImpl.class);
		when(userServiceImpl.get(any())).thenReturn(new UserData());
		doNothing().when(userServiceImpl).create(any());

		LocationServiceImpl locationService = mock(LocationServiceImpl.class);
		doNothing().when(locationService).create(any());

		LocationData data = new LocationData();
		data.setUserId("test");

		assertEquals(HttpStatus.OK, new LocationController(locationService, userServiceImpl).postAddLocationData(data).getStatusCode());
	}

}
