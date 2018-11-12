package asegroup1.api.daos.landregistry;

import asegroup1.api.Application;
import asegroup1.api.daos.DaoImpl;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

@RunWith(SpringRunner.class)
@WebAppConfiguration
@SpringBootTest(classes = Application.class)
class LandRegistryDaoImplTest {
    @Autowired
    private LandRegistryDaoImpl landRegistryDao;

    @Autowired
    private DaoImpl dao;

    @Test
    public void test() {

        assert dao != null;
//        landRegistryDao.searchForPostCodesInBoundaries(0,0,0,0);
        assert landRegistryDao != null;
    }

}