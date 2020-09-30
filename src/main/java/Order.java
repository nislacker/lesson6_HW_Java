import java.sql.*;
import java.util.Scanner;

public class Order {
    private long customer_id;
    private long product_id;
    private int count;

    private boolean isNotEnoughMoney = false;

    public Order(long customer_id, long product_id, int count) {
        this.customer_id = customer_id;
        this.product_id = product_id;
        this.count = count;
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

            conn.setAutoCommit(false); // for transactions -- commit, rollback, savepoint

            Savepoint sp2 = conn.setSavepoint("sp2");
            System.out.println("Savepoint #2 created.");

            String sql = "INSERT `orders` (`customer_id`, `product_id`, `count`) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setLong(1, customer_id);
            preparedStatement.setLong(2, product_id);
            preparedStatement.setInt(3, count);
            preparedStatement.executeUpdate();

            double decreasingValue = getOrderPrice();

            if (!decreaseCustomerBalance(customer_id, decreasingValue)) {
                if (isNotEnoughMoney) {
                    System.out.println("Not enough money on your balance...\n");
                }
                conn.rollback(sp2);
                return false;
            } else {
                conn.commit();
            }

            conn.releaseSavepoint(sp2);

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

        isNotEnoughMoney = false;

        try (Connection conn = DriverManager.getConnection(DBConnData.url, DBConnData.username, DBConnData.password)) {

            conn.setAutoCommit(false); // for transactions -- commit, rollback, savepoint

            Savepoint sp3 = conn.setSavepoint("sp3");
            System.out.println("Savepoint #3 created.");

            String sql = "UPDATE `persons` SET balance = balance - ? WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setDouble(1, decreasingValue);
            preparedStatement.setLong(2, customer_id);
            preparedStatement.executeUpdate();

            Customer lastCustomer = Customer.getLastCustomer();

            if (lastCustomer != null && ((lastCustomer.getBalance() - decreasingValue) < 0)) {
                conn.rollback(sp3);
                return false;
            } else {

                System.out.println("Is everything OK in order?");
                System.out.print("Enter without quotes 'y' to confirm or 'n' to come back to previous savepoint: ");
                String answer;
                Scanner scanner = new Scanner(System.in);
                while ((answer = scanner.nextLine()).trim().isEmpty()) {
                }

                if (answer.equals("y")) {
                    conn.commit();
                    System.out.println("Your balance has been updated.\n");
                } else {
                    conn.rollback(sp3);
                    return false;
                }
            }

            conn.releaseSavepoint(sp3);
            return true;

        } catch (SQLException e) {

            Customer lastCustomer = Customer.getLastCustomer();

            if (lastCustomer != null && ((lastCustomer.getBalance() - decreasingValue) < 0)) {
                isNotEnoughMoney = true;
                return false;
            }
//            e.printStackTrace();
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
