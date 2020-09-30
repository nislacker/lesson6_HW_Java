import java.sql.*;
import java.util.Scanner;

public class Customer extends User {

    private double balance;
    private static long lastCustomerId = 1;

    private boolean insertIsCorrect = false;

    public Customer(String name, int age, double balance) {
        super(name, age);
        this.balance = balance;
    }

    public boolean isDataCorrect() {
        return getName().trim().length() > 0 && getAge() > 0 && balance >= 0.0;
    }

    public boolean addCustomerToDB() {

        Connection conn = null;
        Savepoint sp1 = null;

        try {
            conn = DriverManager.getConnection(DBConnData.url, DBConnData.username, DBConnData.password);

            conn.setAutoCommit(false); // for transactions -- commit, rollback, savepoint

            sp1 = conn.setSavepoint("sp1");
            System.out.println("Savepoint #1 created.");

            String sql = "INSERT `persons` (`name`, `age`, `balance`) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, getName());
            preparedStatement.setInt(2, getAge());
            preparedStatement.setDouble(3, balance);
            preparedStatement.executeUpdate();

            ResultSet rs = preparedStatement.getGeneratedKeys();

            if (rs.next()) {
                lastCustomerId = rs.getLong(1);
            }

            insertIsCorrect = true;

            System.out.println("Is everything OK with your data?");
            System.out.print("Enter without quotes 'y' to confirm or 'n' to come back to previous savepoint: ");
            String answer;
            Scanner scanner = new Scanner(System.in);
            while ((answer = scanner.nextLine()).trim().isEmpty()) {
            }
            answer = answer.trim().toLowerCase();

            if (answer.equals("y")) {
                conn.commit();
                System.out.println("You have been added to DB!\n");
            } else {
                insertIsCorrect = false;
            }

        } catch (SQLException e) {
            System.out.println("Something wrong with connection to DB..\n");
//            e.printStackTrace();
            insertIsCorrect = false;
        } catch (Exception e) {
            System.out.println("Something wrong with adding you to DB. Try again.\n");
//            e.printStackTrace();
            insertIsCorrect = false;
        }

        if (!insertIsCorrect) {
            assert conn != null;
            try {
                conn.rollback(sp1);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }

        return insertIsCorrect;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public static long getLastCustomerId() {
        return lastCustomerId;
    }

    public boolean isInsertIsCorrect() {
        return insertIsCorrect;
    }
}
