package asegroup1.api.services.location;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.InvalidParameterException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import asegroup1.api.daos.location.LocationDaoImpl;
import asegroup1.api.models.LocationData;

class LocationServiceImplTest {

	@Test
	void testCreateLocationDataEmpty() {
		LocationDaoImpl mockedLocDao = mock(LocationDaoImpl.class);
		when(mockedLocDao.getLocationDataById(any(), any())).thenReturn(Arrays.asList());
		
		LocationData data = new LocationData();
		data.setUserId("testId");
		data.setTimelog(Timestamp.valueOf(LocalDateTime.now()));

		try {
			new LocationServiceImpl(mockedLocDao, null).create(data);
		} catch (InvalidParameterException e) {
			fail(e);
		}
		
	}
	
	@Test
	void testCreateLocationDataExsists() {
		LocationDaoImpl mockedLocDao = mock(LocationDaoImpl.class);
		when(mockedLocDao.getLocationDataById(any(), any())).thenReturn(Arrays.asList(new LocationData()));
		
		LocationData data = new LocationData();
		data.setUserId("testId");
		data.setTimelog(Timestamp.valueOf(LocalDateTime.now()));

		assertThrows(InvalidParameterException.class, () -> new LocationServiceImpl(mockedLocDao, null).create(data));
		
	}

}
