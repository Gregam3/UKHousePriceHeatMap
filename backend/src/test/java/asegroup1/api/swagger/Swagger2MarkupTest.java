//package asegroup1.api.swagger;
//
//import asegroup1.api.Application;
//import asegroup1.api.configs.SwaggerConfig;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.web.WebAppConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import java.io.BufferedWriter;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebAppConfiguration
//@RunWith(SpringJUnit4ClassRunner.class)
//@SpringBootTest(classes = {Application.class, SwaggerConfig.class})
//@AutoConfigureMockMvc
//public class Swagger2MarkupTest {
//
//  @Autowired private MockMvc mockMvc;
//
//  /**
//   * This Generates the swagger.json file in a unit tests and stores it in
//   * target/swagger/swagger.json so that it can be converted at compile time to
//   * static documentation
//   *
//   * @throws Exception
//   */
////  @Test
//  public void generateSwaggerJson() throws Exception {
//    String outputDir = "target/swagger/";
//    MvcResult mvcResult =
//        this.mockMvc
//            .perform(get("/v2/api-docs").accept(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andReturn();
//
//    MockHttpServletResponse response = mvcResult.getResponse();
//    String swaggerJson = response.getContentAsString();
//    Files.createDirectories(Paths.get(outputDir));
//    try (BufferedWriter writer = Files.newBufferedWriter(
//             Paths.get(outputDir, "swagger.json"), StandardCharsets.UTF_8)) {
//      writer.write(swaggerJson);
//    }
//  }
//}
