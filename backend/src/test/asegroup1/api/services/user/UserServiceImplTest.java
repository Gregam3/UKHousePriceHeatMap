package asegroup1.api.services.user;

import asegroup1.api.controllers.UserController;
import asegroup1.api.daos.user.UserDaoImpl;
import asegroup1.api.models.UserData;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    @Test
    void testIfUserControllerSendsRequestThroughToDao() {
        String userId = "test";

        UserData userData = new UserData();
        userData.setUserId(userId);

        UserDaoImpl UserDaoImplMock = mock(UserDaoImpl.class);
        when(UserDaoImplMock.get(userId)).thenReturn(userData);

        UserServiceImpl userService = new UserServiceImpl(UserDaoImplMock);
        UserController userController = new UserController(userService);

        ResponseEntity<UserData> response = userController.get("test");

        assert (response.getStatusCode().is2xxSuccessful() &&  userController.get("test").getBody().getUserId().equals(userId));
    }
}