import java.sql.*;

public class Product {
    private long id;
    private String label;
    private double price;

    public Product(long id, String label, double price) {
        this.id = id;
        this.label = label;
        this.price = price;
    }

    public static Product getProductFromDB(long id) {

        try (Connection conn = DriverManager.getConnection(DBConnData.url, DBConnData.username, DBConnData.password)) {

            conn.setAutoCommit(false); // for transactions -- commit, rollback, savepoint

            String sql = "SELECT label, price FROM products WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String label = resultSet.getString("label");
                double price = resultSet.getDouble("price");
                return new Product(id, label, price);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
