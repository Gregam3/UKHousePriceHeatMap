package asegroup1.api.daos.landregistry;

import asegroup1.api.Application;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.applet.AppletContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
class LandRegistryDaoImplTest {
    @Autowired
    LandRegistryDaoImpl landRegistryDao;

    @Test
    public void test() {
        landRegistryDao.searchForPostCodesInBoundaries(0,0,0,0);
    }

}