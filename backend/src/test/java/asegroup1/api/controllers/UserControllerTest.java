package asegroup1.api.controllers;

import asegroup1.api.controllers.UserController;
import asegroup1.api.daos.user.UserDaoImpl;
import asegroup1.api.models.UserData;
import asegroup1.api.services.user.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
}