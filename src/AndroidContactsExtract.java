import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.*;

public class AndroidContactsExtract {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:contacts2.db");
        Statement stat = conn.createStatement();
        ResultSet allIds = stat.executeQuery("SELECT _id, display_name from raw_contacts WHERE account_type like 'vnd.sec.contact.%'");
        //total 673 records

        // select id, mimetype from mimetypes 1=email, 3=address, 5=phone, 6=name

        PreparedStatement names = conn.prepareStatement(
                "SELECT data2, data3, data5 FROM data WHERE raw_contact_id=? AND mimetype_id=6;");
        //first, last and middle name

        PreparedStatement emails = conn.prepareStatement(
                        "SELECT data1 FROM data WHERE raw_contact_id=? AND mimetype_id=1;");

        PreparedStatement phones = conn.prepareStatement(
                        "SELECT data1, data2 FROM data WHERE raw_contact_id=? AND mimetype_id=5;");
        //data1 = number, data2 =type of number, H=1 M=2 W=3 O=7

        String outputFile = args[0];
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
//        writer.write("Given Name,Additional Name,Family Name,Email,Phone 1 - Type,Phone 1 - Value,Phone 2 - Type,Phone 2 - Value,Phone 3 - Type,Phone 3 - Value");
        writer.write("First Name,Middle Namme,Last Name,E-mail,Mobile Phone,Home Phone,Work Phone,Other Phone");
        writer.newLine();

        while (allIds.next()) {
            int id = allIds.getInt("_id");
            String displayName =  allIds.getString("display_name").trim().replaceAll(".","");
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

            emails.setInt(1, id);
            ResultSet emailData = emails.executeQuery();
            String email = null;
            while (emailData.next()) {
                email = emailData.getString("data1");
            }
            writer.write(email!=null?email:"");
            writer.write(",");
            System.out.print("Email: " + email+", ");
            emailData.close();

            phones.setInt(1, id);
            ResultSet phoneData = phones.executeQuery();
            String mobile = null, home = null, work = null, other = null;
            while (phoneData.next()) {
                String phoneType = phoneData.getString("data2");
                if(phoneType.equals("1")) home = phoneData.getString("data1");
                if(phoneType.equals("2")) mobile = phoneData.getString("data1");
                if(phoneType.equals("3")) work = phoneData.getString("data1");
                if(phoneType.equals("7")) other = phoneData.getString("data1");
            }
            writer.write(mobile!=null?mobile:"");
            writer.write(",");
            writer.write(home!=null?home:"");
            writer.write(",");
            writer.write(work != null ? work : "");
            writer.write(",");
            writer.write(other != null ? other : "");
            writer.write(",");

            phoneData.close();
            System.out.println();
            writer.newLine();
        }
        writer.close();
        names.close();
        allIds.close();
        conn.close();
    }
}