import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Order {
    private long customer_id;
    private long product_id;
    private int count;

    public Order(long customer_id, long product_id, int count) {
        this.customer_id = customer_id;
        this.product_id = product_id;
        this.count = count;

//        this.addOrderToDB(customer_id, product_id, count);
    }

    public void show() {
        Product product = Product.getProductFromDB(product_id);
        System.out.println("\n\t\tYour order\n");
        System.out.println("id\tproduct\tprice\tcount");
        System.out.printf("%d\t%s\t%.2f\t%d\n", product_id, product.getLabel(), product.getPrice(), count);
        System.out.println(new String(new char[25]).replace("\0", "-"));
        System.out.printf("Total cost: %.2f\n", getOrderPrice());
    }

    private double getOrderPrice() throws NullPointerException {
        double productPrice = Product.getProductFromDB(product_id).getPrice();
        return productPrice * count;
    }

    public boolean addOrderToDB() {

        try (Connection conn = DriverManager.getConnection(DBConnData.url, DBConnData.username, DBConnData.password)) {

//            conn.setAutoCommit(false); // for transactions -- commit, rollback, savepoint

            String sql = "INSERT `orders` (`customer_id`, `product_id`, `count`) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1, customer_id);
            preparedStatement.setLong(2, product_id);
            preparedStatement.setInt(3, count);
            preparedStatement.executeUpdate();

            double decreasingValue = getOrderPrice();

            if (!decreaseCustomerBalance(customer_id, decreasingValue)) {
                System.out.println("Something wrong with customer balance...\n");
            }

            System.out.println("Your order have been added to DB!\n");
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("There is no price for this product.\n");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return false;
    }

    private boolean decreaseCustomerBalance(long customer_id, double decreasingValue) {
        try (Connection conn = DriverManager.getConnection(DBConnData.url, DBConnData.username, DBConnData.password)) {

//            conn.setAutoCommit(false); // for transactions -- commit, rollback, savepoint

            String sql = "UPDATE `persons` SET balance = balance - ? WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setDouble(1, decreasingValue);
            preparedStatement.setLong(2, customer_id);
            preparedStatement.executeUpdate();
//            System.out.println("Your balance has been updated.\n");

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public long getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(long customer_id) {
        this.customer_id = customer_id;
    }

    public long getProduct_id() {
        return product_id;
    }

    public void setProduct_id(long product_id) {
        this.product_id = product_id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
