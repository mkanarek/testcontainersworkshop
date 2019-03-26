import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class MysqlCon {
    private static Logger LOGGER = LoggerFactory.getLogger("MysqlCon");

    public static void main(String args[]) {
        try {
            List<String> resultSetArray=new ArrayList<>();
            String address = args[0];
            String user = args[1];
            String password = args[2];
            String tableName = args[3];
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection(
                    "jdbc:mysql://"+address, user, password);
            LOGGER.info("JDBC driver reached");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from "+ tableName);
            int numCols = rs.getMetaData().getColumnCount();
            LOGGER.info("Query done");
            while(rs.next()) {
                StringBuilder sb = new StringBuilder();

                for (int i = 1; i <= numCols; i++) {
                    sb.append(String.format(String.valueOf(rs.getString(i))) + " ");

                }
                resultSetArray.add(sb.toString());

            }

            File csvOutputFile = new File("/home/data/tableData.txt");
            FileWriter fileWriter = new FileWriter(csvOutputFile, false);


            for(String mapping : resultSetArray) {
                fileWriter.write(mapping + "\n");
            }

            fileWriter.close();
            con.close();
        } catch (Exception e) {
            LOGGER.error("Error from client" + e.getMessage());
        }
    }
}
