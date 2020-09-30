import java.sql.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {


//        1.	Создание заказа и добавление заказчика.

        Customer customer;
        Scanner scanner = new Scanner(System.in);

        try (Connection conn = DriverManager.getConnection(DBConnData.url, DBConnData.username, DBConnData.password)) {

            conn.setAutoCommit(false); // for transactions -- commit, rollback, savepoint

            Savepoint sp1 = conn.setSavepoint("sp1");
            System.out.println("Savepoint #1 created.");

            savepoint1:
            {
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

                } while (!customer.isDataCorrect());

                System.out.println("Is everything OK with your data?");
                System.out.print("Enter without quotes 'y' to confirm or 'n' to come back to previous savepoint: ");
                String answer;
                while ((answer = scanner.nextLine()).trim().isEmpty()) {
                }
                answer = answer.trim().toLowerCase();

                if (answer.equals("y")) {
                    customer.addCustomerToDB();
                    conn.commit();
                } else {
                    conn.rollback(sp1);
                    break savepoint1;
                }

                Savepoint sp2 = conn.setSavepoint("sp2");
                System.out.println("Savepoint #2 created.");

                savepoint2:
                {
                    // Отобразить таблицу всех товаров

                    System.out.println("\n\t\tProducts table\n");

                    Statement statement = conn.createStatement();
                    ResultSet resultSet = statement.executeQuery("SELECT * FROM `products`");

                    while (resultSet.next()) {
                        int id = resultSet.getInt("Id");
                        String label = resultSet.getString("label");
                        Double price = resultSet.getDouble("price");

                        System.out.printf("%d. %s: %.2f\n", id, label, price);
                    }

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

                    Savepoint sp3 = conn.setSavepoint("sp3");
                    System.out.println("Savepoint #3 created.");

                    savepoint3:
                    {
                        long customer_id = Customer.getLastCustomerId();

                        Order order = new Order(customer_id, product_id, count);
                        order.show();

                        System.out.println("Is everything OK in order?");
                        System.out.print("Enter without quotes 'y' to confirm or 'n' to come back to previous savepoint: ");
                        while ((answer = scanner.nextLine()).trim().isEmpty()) {
                        }

                        if (answer.equals("y")) {
                            order.addOrderToDB();
//                            conn.commit();
                        } else {
                            int savepointNumber = 0;

                            while (!(savepointNumber >= 2 && savepointNumber <= 3)) {

                                System.out.print("Enter savepoint number (from 2 to 3) to come back: ");
                                savepointNumber = scanner.nextInt();

                                if (savepointNumber >= 2 && savepointNumber <= 3) {
                                    switch (savepointNumber) {
                                        case 2:
                                            conn.rollback(sp2);
                                            break savepoint2;
                                        case 3:
                                            conn.rollback(sp3);
                                            break savepoint3;
                                    }
                                }
                            }
                        }
                    }
                }
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
