package testcontainers;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.Testcontainers;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;

import static java.net.URLDecoder.decode;


public class TestContainersTestDataBase {

    private GenericContainer dslContainer;
    private GenericContainer dslContainerClient;

    private static Logger LOGGER = LoggerFactory.getLogger("TestContainersTestDataBase");

    File directory = new File(decodePath("/database_scripts"));
    public static Network network = Network.SHARED;


    @Before
    public void setUp() {
//        Testcontainers.exposeHostPorts(32847);
        dslContainer = new GenericContainer(
                new ImageFromDockerfile("tcdockerfile/db", true).withDockerfileFromBuilder(builder -> {
                    builder
                            .from("percona:5.6")
                            .env("MYSQL_DATABASE", "testBase")
                            .env("MYSQL_USER", "testUser")
                            .env("MYSQL_PASSWORD", "admin")
                            .env("MYSQL_ROOT_PASSWORD", "rootadmin")
                            .copy("/tmp/foo", "/docker-entrypoint-initdb.d")
                            .build();
                })
                        .withFileFromFile("/tmp/foo",directory))
                .withNetwork(network)
                .withNetworkAliases("dbhost")
                .waitingFor(Wait.forListeningPort())
                .withClasspathResourceMapping("/database_scripts","/docker-entrypoint-initdb.d",BindMode.READ_ONLY)
//                .withExposedPorts(3306)
//        )
//                .withEnv("MYSQL_DATABASE", "testBase")
//                .withEnv("MYSQL_USER", "testUser")
//                .withEnv("MYSQL_PASSWORD", "admin")
//                .withEnv("MYSQL_ROOT_PASSWORD", "admin")
//                .withClasspathResourceMapping("/database_configuration/","/etc/mysql/conf.d/", BindMode.READ_WRITE)
                .withLogConsumer(new Slf4jLogConsumer(LOGGER));
        dslContainer.start();
    }

    @Test
    public void simpleDslTest() {
        File tempDir = null;
        try {
            tempDir = Files.createTempDirectory(Paths.get("/tmp"), "testDataBase").toFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String temDirPath = tempDir.toString() + File.separator;
        String address = String.format("%s:%s", dslContainer.getContainerIpAddress(), dslContainer.getMappedPort(3306));
        System.out.println("Address is "+ address);
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        dslContainerClient = new GenericContainer(
//                    new ImageFromDockerfile("tcdockerfile/dbclient", false).withDockerfileFromBuilder(builder -> {
//                        builder
//                                .from("alpine:latest")
//                                .run("apk update && apk add --update mysql-client")
////                                .run("apk-install mysql-client")
////                                .entryPoint("mysql --host=dbhost --user=root --password=rootadmin --database=testBase")
////                                .entryPoint("mysql")
////                                .entryPoint("mysql --host=dbhost --user=testUser --password=admin")
//                                .build();
//                    }))
//                .withNetwork(network)
//                .withClasspathResourceMapping("/query_script", "/sql",BindMode.READ_ONLY)
//                .withNetworkAliases("dbclient")
////                .withCommand("source /sql/query.sql")
////                .withCommand("select * from testBase.tables;")
//                .withCommand("mysql --host=dbhost --user=root --password=rootadmin --database=testBase --table=trackingtype --force")
////                .withCommand("mysql select * from testBase.tables;")
//                .withCommand("source /sql/query.sql")
////                .withCommand("select * from testBase.tables;")//--execute='select * from testBase.tables;'
////                    .withCommand("mysql --host=host.testcontainers.internal --port=32847 --user=root --password=rootadmin --database=testBase --execute='select * from testBase.tables;'")
////        .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
//                    .withLogConsumer(new Slf4jLogConsumer(LOGGER));

        dslContainerClient = new GenericContainer(
                new ImageFromDockerfile("tcdockerfile/dbclient", false).withDockerfileFromBuilder(builder -> {
                    builder
                            .from("test:mysqlClientTest")
//                            .run("java -server -jar application.jar dbhost:3306/testBase root rootadmin trackingtype")
//                                .run("apk-install mysql-client")
//                                .entryPoint("mysql --host=dbhost --user=root --password=rootadmin --database=testBase")
//                                .entryPoint("mysql")
//                                .entryPoint("mysql --host=dbhost --user=testUser --password=admin")
                            .build();
                }))
                .withNetwork(network)
                .withCommand("java -server -jar application.jar dbhost:3306/testBase root rootadmin trackingtype")
                .withFileSystemBind(temDirPath, "/home/data/",BindMode.READ_WRITE)
//                .withClasspathResourceMapping("/query_script", "/sql",BindMode.READ_ONLY)
//                .withNetworkAliases("dbclient")
//                .withCommand("source /sql/query.sql")
//                .withCommand("select * from testBase.tables;")
//                .withCommand("mysql --host=dbhost --user=root --password=rootadmin --database=testBase --table=trackingtype --force")
//                .withCommand("mysql select * from testBase.tables;")
//                .withCommand("source /sql/query.sql")
//                .withCommand("select * from testBase.tables;")//--execute='select * from testBase.tables;'
//                    .withCommand("mysql --host=host.testcontainers.internal --port=32847 --user=root --password=rootadmin --database=testBase --execute='select * from testBase.tables;'")
//        .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(30)))
                .withLogConsumer(new Slf4jLogConsumer(LOGGER));

        dslContainerClient.start();
//        try {
//            Thread.sleep(30000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        try {
//            String response = dslContainerClient.execInContainer("source /sql/query.sql").getStdout();//Select * from testBase.trackingtype;--socket=/var/run/mysqld/mysqld.sock
////            String response = dslContainerClient.execInContainer("select * from testBase.tables;").getStdout();//Select * from testBase.trackingtype;--socket=/var/run/mysqld/mysqld.sock
//            System.out.println("Response is " +response);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        System.out.println("Here I am");
    }

    public static String decodePath(String path) {
        String returnString = ".";
        try {
            returnString = decode((getRootPath() + path), "utf-8");
            return returnString;
        } catch (UnsupportedEncodingException e) {
            LOGGER.info("Wrong encoding return root folder path");
            return returnString;
        }
    }

    public static String getRootPath() {
        return Thread.currentThread().getContextClassLoader().getResource(".").getPath();
    }
}
