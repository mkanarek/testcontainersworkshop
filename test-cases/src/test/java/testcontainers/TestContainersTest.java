package testcontainers;

import org.junit.Before;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import static com.jayway.restassured.RestAssured.given;
import static org.junit.Assert.assertTrue;

public class TestContainersTest {

    private GenericContainer dslContainer;

    @Before
    public void setUp() {
        dslContainer = new GenericContainer(
                new ImageFromDockerfile("tcdockerfile/nginx", false).withDockerfileFromBuilder(builder -> {
                    builder
                            .from("alpine:3.2")
                            .run("apk add --update nginx")
                            .cmd("nginx", "-g", "daemon off;")
                            .build();
                }))
                .withExposedPorts(80);
        dslContainer.start();
    }

    @Test
    public void simpleDslTest() {
        String address = String.format("http://%s:%s", dslContainer.getContainerIpAddress(), dslContainer.getMappedPort(80));

        assertTrue("Status code is not 200",given().when().get(address).statusCode() == 200);
    }
}
