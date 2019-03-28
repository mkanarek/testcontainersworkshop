package testcontainers;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.net.URLDecoder.decode;
import static org.junit.Assert.assertEquals;


/**
 * We are testing simple-mysql-client, the client which is getting data from mysql database.
 * In test there is Thread.sleep() method used to wait for container service to be ready.
 * Replace Thread.sleep() method with proper testcontainers methods.
 * To prepare environment go through those steps:
 * 1. BUILD simple-mysql-client with "mvn clean install" command.
 * 2. Run docker_build.sh script from simple-mysql-client module (to create workshop:simplemysqlclient container).
 * 3. Run the test - it should pass.
 * 4. Get rid of Tread.sleep parts using testcontainers method with Wait startegy. Test should be passing.
 * If You have no idea solution is in Excerciese_solution.txt file.
 */
public class TestContainersDatabaseTest {

    private GenericContainer dbContainer;
    private GenericContainer dbClientContainer;

    private static Logger LOGGER = LoggerFactory.getLogger("TestContainersDatabaseTest");

    private File file1 = new File(decodePath("/database_scripts/database.sql"));
    private File file2 = new File(decodePath("/database_scripts/ingest.sql"));
    private static Network network = Network.SHARED;
    private static String OS_MAC_TMP_DIR = "/tmp";

    @Before
    public void setUp() {
        dbContainer = new GenericContainer(
                new ImageFromDockerfile("tcdockerfile/db", true).withDockerfileFromBuilder(builder -> {
                    builder
                            .from("percona:5.6")
                            .env("MYSQL_DATABASE", "testBase")
                            .env("MYSQL_USER", "testUser")
                            .env("MYSQL_PASSWORD", "admin")
                            .env("MYSQL_ROOT_PASSWORD", "rootadmin")
                            .copy("/tmp/file1.sql", "/docker-entrypoint-initdb.d/")
                            .copy("/tmp/file2.sql", "/docker-entrypoint-initdb.d/")
                            .build();
                }).withFileFromFile("/tmp/file1.sql", file1).withFileFromFile("/tmp/file2.sql", file2))
                .withNetwork(network)
                .withNetworkAliases("dbhost")
                .withLogConsumer(new Slf4jLogConsumer(LOGGER));

        dbContainer.start();
    }

    @Test
    public void simpleDslTest() {
        String temDirPath = getTempDir();

        //##########get rid off this Thread.sleep
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        dbClientContainer = new GenericContainer(
                new ImageFromDockerfile("tc/dbclient", true).withDockerfileFromBuilder(builder -> {
                    builder
                            .from("workshop:simplemysqlclient")
                            .build();
                }))
                .withNetwork(network)
                .withCommand("java -server -jar application.jar dbhost:3306/testBase?allowMultiQueries=true testUser admin trackingtype")
                .withFileSystemBind(temDirPath, "/opt/data", BindMode.READ_WRITE)
//                .waitingFor(Wait.forLogMessage(".*Data saved.*",1))// solution wait for proper log
                .withLogConsumer(new Slf4jLogConsumer(LOGGER));

        dbClientContainer.start();


        //##########get rid off this Thread.sleep
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String returnTableData = loadTableDataFromDirectory(temDirPath);

        assertEquals("Got wrong table data.", loadResourceFile("/database_data/expectedTableData.txt"), returnTableData);
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

    public static String loadTableDataFromDirectory(String temDirPath) {
        String returnTableData = null;
        try {
            returnTableData = new String(Files.readAllBytes(Paths.get(temDirPath + "tableData.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnTableData;
    }

    public static String loadResourceFile(String resourceFilePath) {
        String text = "";
        try {
            text = new String(Files.readAllBytes(Paths.get(decodePath(resourceFilePath))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String getRootPath() {
        return Thread.currentThread().getContextClassLoader().getResource(".").getPath();
    }

    public static String getTempDir() {
        File tempDir = null;
        if (SystemUtils.IS_OS_MAC) {
            try {
                tempDir = Files.createTempDirectory(Paths.get(OS_MAC_TMP_DIR), "testDataBase").toFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.createTempDirectory("testDataBase").toFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tempDir.toString() + File.separator;
    }
}
