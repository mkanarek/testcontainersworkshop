package testcontainers;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;


public class TestContainersPhpHttpServerTest {

    private GenericContainer dslContainer;
    private String EnvironmentVariable = "Zmienna";

    private Logger LOGGER = LoggerFactory.getLogger("TestContainersPhpHttpServerTest");

    @Before
    public void setUp() {
        dslContainer = new GenericContainer(
                new ImageFromDockerfile("tcdockerfile/php", true).withDockerfileFromBuilder(builder -> {
                    builder
                            .from("php:7.2-apache")
                            .build();
                }))
                .withExposedPorts(80)
                .withEnv("YOLO", EnvironmentVariable)
                .withClasspathResourceMapping("/php_http_server/index.php","/var/www/html/index.php", BindMode.READ_WRITE)
                .withLogConsumer(new Slf4jLogConsumer(LOGGER));
        dslContainer.start();
    }

    @Test
    public void simpleDslTest() {
        String address = String.format("http://%s:%s", dslContainer.getContainerIpAddress(), dslContainer.getMappedPort(80));

        open(address);
        $("body > h2").shouldHave(text("Zmienna systemowa ma wartosc = "+EnvironmentVariable));
    }
}
