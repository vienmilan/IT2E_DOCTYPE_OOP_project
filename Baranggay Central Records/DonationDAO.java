import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DonationDAO {

    public static List<Donation> fetchAll() throws Exception {
        List<Donation> list = new ArrayList<>();
        String sql = "SELECT * FROM donations";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Donation(
                    rs.getInt("id"),
                    rs.getString("donor"),
                    rs.getDouble("amount"),
                    rs.getString("description"),
                    rs.getString("type")
                ));
            }
        }
        return list;
    }

    public static void saveAll(List<Donation> donations) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try (Statement st = con.createStatement()) {
                st.executeUpdate("DELETE FROM donations");
            }

            String sql =
                "INSERT INTO donations(donor,amount,description,type) VALUES(?,?,?,?)";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                for (Donation d : donations) {
                    ps.setString(1, d.donor);
                    ps.setDouble(2, d.amount);
                    ps.setString(3, d.description);
                    ps.setString(4, d.type);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            con.commit();
        }
    }
}
