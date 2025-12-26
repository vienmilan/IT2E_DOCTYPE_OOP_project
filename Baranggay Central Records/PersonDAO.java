import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PersonDAO {

    public static List<Person> fetchAll() throws Exception {
        List<Person> list = new ArrayList<>();
        String sql = "SELECT * FROM persons";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Person(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("address"),
                    rs.getString("needs"),
                    rs.getInt("age"),
                    rs.getString("contact"),
                    rs.getString("category")
                ));
            }
        }
        return list;
    }

    public static void saveAll(List<Person> persons) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (Statement st = con.createStatement()) {
                st.executeUpdate("DELETE FROM persons");
            }

            String sql =
                "INSERT INTO persons(name,address,needs,age,contact,category) VALUES(?,?,?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Person p : persons) {
                    ps.setString(1, p.name);
                    ps.setString(2, p.address);
                    ps.setString(3, p.needs);
                    ps.setInt(4, p.age);
                    ps.setString(5, p.contact);
                    ps.setString(6, p.category);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
        }
    }
}
