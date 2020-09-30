import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {


//        1.	Создание заказа и добавление заказчика.

        Customer customer;
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DBConnData.url, DBConnData.username, DBConnData.password)) {

            conn.setAutoCommit(false); // for transactions -- commit, rollback, savepoint

            do {
                // добавление заказчика
                System.out.println("\t\tEnter info about yourself:");
                System.out.print("Name: ");
                String name;
                while ((name = scanner.nextLine()).trim().isEmpty()) {
                }
                System.out.print("Age: ");
                int age = scanner.nextInt();
                System.out.print("Balance: ");
                double balance = scanner.nextDouble();
                customer = new Customer(name, age, balance);

            } while (!customer.isDataCorrect() || !customer.addCustomerToDB());

            Order order;

            do {
                Product.showAllProducts();

//         Создание заказа
//
//        2.	Выбор товара
//        3.	Выбор количества товара
//        4.	Снятие денег со счёта пользователя и коммит.

//         Попросить выбрать товар по id и ввести кол-во

                int product_id, count;

                while (true) {

                    System.out.println("\nEnter info about your order:");
                    System.out.print("Product \"id\" (1st number in every line) to add in cart: ");
                    product_id = scanner.nextInt();
                    System.out.print("Count: ");
                    count = scanner.nextInt();

                    if ((product_id > 0) && (count > 0)) {
                        break;
                    }
                }

                long customer_id = Customer.getLastCustomerId();

                order = new Order(customer_id, product_id, count);
                order.show();

            } while (!order.addOrderToDB());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
