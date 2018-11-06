package asegroup1.api.services.landregistry;

import asegroup1.api.models.LandRegistryData;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class LandRegistryServiceImplTest {

    private static LandRegistryServiceImpl landRegistryService;

    @BeforeAll
    private static void setUpService() {
        try {
            landRegistryService = new LandRegistryServiceImpl();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testIfSPARQLExecutorReturnsRelevantResults() {
        try {
            List<LandRegistryData> landRegistryDataByPostCode = landRegistryService.getLandRegistryDataByPostCode("BH9 2SL");

            //Checking not only if results are returned but that results contain relevant data
            if (!landRegistryDataByPostCode.get(
                    new Random().nextInt(landRegistryDataByPostCode.size() - 1)
            ).getStreetName().equals("ENSBURY PARK ROAD")) {
                System.err.println("incorrect street name returned");
                assert false;
            }

            assert true;
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
            assert false;
        }
    }
}