package asegroup1.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Objects;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import asegroup1.api.daos.user.UserDaoImpl;
import asegroup1.api.models.UserData;
import asegroup1.api.services.user.UserServiceImpl;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

class UserControllerTest {

    @Test
    void testIfUserControllerSendsRequestThroughToDao() {
        String userId = "test";

        UserData userData = new UserData();
        userData.setUserId(userId);

        UserDaoImpl userDaoMock = mock(UserDaoImpl.class);
        when(userDaoMock.get(userId)).thenReturn(userData);


        UserServiceImpl userService = new UserServiceImpl(userDaoMock);
        UserController userController = new UserController(userService);

        ResponseEntity<UserData> response = userController.getUserData("test");

        assert (response.getStatusCode().is2xxSuccessful() &&  Objects.requireNonNull(userController.getUserData("test").getBody()).getUserId().equals(userId));
    }

	@Test
	void testAddUserDataExsists() {
		UserDaoImpl userDaoMock = mock(UserDaoImpl.class);
		when(userDaoMock.get(any())).thenReturn(new UserData());
		doNothing().when(userDaoMock).add(any());
		assertEquals(HttpStatus.BAD_REQUEST, new UserController(new UserServiceImpl(userDaoMock)).addUserData("test").getStatusCode());
	}

	@Test
	void testAddUserDataFail() {
		UserDaoImpl userDaoMock = mock(UserDaoImpl.class);
		when(userDaoMock.get(any())).thenReturn(null);
		doNothing().when(userDaoMock).add(any());
		assertEquals(HttpStatus.BAD_REQUEST, new UserController(new UserServiceImpl(userDaoMock)).addUserData("test").getStatusCode());
	}

	@Test
	void testAddUserDataSucess() {
		UserDaoImpl userDaoMock = mock(UserDaoImpl.class);
		when(userDaoMock.get(any())).thenReturn(null).thenReturn(new UserData());
		doNothing().when(userDaoMock).add(any());
		assertEquals(HttpStatus.OK, new UserController(new UserServiceImpl(userDaoMock)).addUserData("test").getStatusCode());
	}
}