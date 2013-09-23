import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.*;

public class AndroidSMSExtract {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:mmssms.db");
        Statement stat = conn.createStatement();

        ResultSet allMessages = stat.executeQuery("" +
                "SELECT _id, thread_id, address, person, date, protocol, read, " +
                "status, type, reply_path_present, subject, body, service_center, locked, " +
                "error_code, seen, deletable FROM sms ORDER BY date DESC");

        String outputFile = args[0];
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        writer.write("Address,Person,Date,Protocol,Read,Status,type, reply_path_present, subject, " +
                "body, service_center, locked, error_code, seen, deletable");
        writer.newLine();

        while (allMessages.next()) {

            int id = allMessages.getInt("_id");
            String displayName =  allMessages.getString("display_name").trim().replaceAll(".","");
            System.out.print("Id: " + id + ", ");

            names.setInt(1, id);
            ResultSet nameData = names.executeQuery();
            String firstName=null, middleName=null, lastName = null;
            while (nameData.next()) {
                firstName = nameData.getString("data2");
                middleName = nameData.getString("data5");
                lastName = nameData.getString("data3");
            }
            writer.write(firstName!=null?firstName:"");
            writer.write(",");
            writer.write(middleName!=null?middleName:"");
            writer.write(",");
            writer.write(lastName!=null?lastName:"");
            writer.write(",");
            System.out.print("Name: " + firstName + " " + (middleName != null ? middleName + " " : "") + lastName + ", ");
            nameData.close();

            writer.newLine();
        }
        writer.close();
        allMessages.close();
        conn.close();
    }
}
