package asegroup1.api.services.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import asegroup1.api.daos.user.UserDaoImpl;
import asegroup1.api.models.UserData;

class UserServiceImplTest {

	@Test
	void testAddExists() {
		UserDaoImpl mockedDao = mock(UserDaoImpl.class);
		doNothing().when(mockedDao).add(any());
		when(mockedDao.get(any())).thenReturn(new UserData());
		assertEquals(-1, new UserServiceImpl(mockedDao).add("test"));
	}

	@Test
	void testAddNoAdded() {
		UserDaoImpl mockedDao = mock(UserDaoImpl.class);
		doNothing().when(mockedDao).add(any());
		when(mockedDao.get(any())).thenReturn(null);
		assertEquals(1, new UserServiceImpl(mockedDao).add("test"));
	}

	@Test
	void testAddAdded() {
		UserDaoImpl mockedDao = mock(UserDaoImpl.class);
		doNothing().when(mockedDao).add(any());
		when(mockedDao.get(any())).thenReturn(null).thenReturn(new UserData());
		assertEquals(0, new UserServiceImpl(mockedDao).add("test"));
	}

}
