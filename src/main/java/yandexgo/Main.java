package yandexgo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public final class Main {
    private final RideDao dao;
    private final Repository<Ride> repository = new Repository<>();
    private final Analytics analytics = new Analytics();
    private final ReportExporter exporter = new CsvReportExporter();
    private Main(String database) throws java.sql.SQLException {
        dao = new SqliteRideDao("jdbc:sqlite:" + database);
    }
    private final Scanner input = new Scanner(System.in);
    private final Passenger passenger = new Passenger("Пассажир");
    private final RideService service = new RideService();
    private final RatingService ratings = new RatingService();
    private final List<Ride> rides = new ArrayList<>();
    private final List<Driver> drivers = Arrays.asList(
            new Driver("Али", new Car("Hyundai Accent", "101 AAA", Tariff.ECONOMY)),
            new Driver("Данияр", new Car("Toyota Camry", "202 BBB", Tariff.COMFORT)),
            new Driver("Арман", new Car("Mercedes E-Class", "303 CCC", Tariff.BUSINESS)));

    public static void main(String[] args) throws Exception {
        Main app = new Main(System.getProperty("taxi.db", "taxi.db"));
        try (RideService service = app.service) {
            if (args.length > 0 && "--demo".equals(args[0])) app.demo();
            else app.run();
        }
    }

    private void save(Ride ride) throws java.sql.SQLException {
        dao.save(RideRecord.from(ride));
        repository.save(ride);
    }

    private void report() throws java.sql.SQLException {
        List<RideRecord> history = dao.findAll();
        System.out.println("Поездки по тарифам: " + analytics.countByTariff(history));
        System.out.println("Доход завершённых поездок: " + analytics.completedRevenue(history) + " тг");
        System.out.println("Обслуженные пассажиры: " + analytics.servedPassengers(history));
    }

    private void export() throws Exception {
        java.nio.file.Path path = java.nio.file.Path.of("rides.csv");
        exporter.export(dao.findAll(), path);
        System.out.println("CSV: " + path.toAbsolutePath());
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        if (!input.hasNextLine()) throw new java.util.NoSuchElementException();
        return input.nextLine().trim();
    }

    private Ride selectRide() {
        if (rides.isEmpty()) throw new IllegalStateException("Сначала создайте заказ.");
        showRides();
        int index = Integer.parseInt(ask("Номер поездки: ")) - 1;
        if (index < 0 || index >= rides.size()) throw new IllegalArgumentException("Нет такой поездки.");
        return rides.get(index);
    }

    private void showRides() {
        List<Ride> saved = repository.findAll();
        if (saved.isEmpty()) System.out.println("Поездок в текущей сессии пока нет.");
        for (int i = 0; i < saved.size(); i++) System.out.println((i + 1) + ". " + saved.get(i));
    }

    private void order() throws Exception {
        String origin = ask("Откуда: ");
        String destination = ask("Куда: ");
        BigDecimal distance = new BigDecimal(ask("Расстояние, км: ").replace(',', '.'));
        int choice = Integer.parseInt(ask("Тариф: 1 — Economy, 2 — Comfort, 3 — Business: "));
        if (choice < 1 || choice > 3) throw new IllegalArgumentException("Выберите тариф от 1 до 3.");
        Ride ride = new Ride(passenger, Tariff.values()[choice - 1], origin, destination, distance);
        rides.add(ride);
        save(ride);
        System.out.println("Поиск водителя в фоновом потоке...");
        boolean assigned = service.assignAsync(ride, drivers).join();
        save(ride);
        System.out.println(assigned ? "Водитель назначен: " + ride : "Подходящий автомобиль не найден.");
    }

    private void showDrivers() {
        for (Driver driver : drivers) {
            System.out.printf("%s | %s | %s | %s | рейтинг: %s%n", driver.getName(),
                    driver.getCar(), driver.getCar().getTariff(),
                    driver.isAvailable() ? "свободен" : "занят",
                    driver.getRatingCount() == 0 ? "нет оценок" : String.format("%.1f", driver.getAverageRating()));
        }
    }

    private void run() throws Exception {
        System.out.println("Yandex GO . Цены условные, в тенге.");
        while (true) {
            System.out.println("\n1. Заказать поездку\n2. Поездки\n3. Начать поездку (водитель)"
                    + "\n4. Завершить поездку (водитель)\n5. Оценить поездку\n6. Водители\n7. Аналитика всех сессий\n8. Экспорт CSV\n9. История из SQLite\n0. Выход");
            try {
                String command = ask("> ");
                Ride ride;
                switch (command) {
                    case "0": return;
                    case "1": order(); break;
                    case "2": showRides(); break;
                    case "3":
                        ride = selectRide();
                        service.startRide(ride, ride.getDriver());
                        save(ride);
                        System.out.println("Поездка началась."); break;
                    case "4":
                        ride = selectRide();
                        service.completeRide(ride, ride.getDriver());
                        save(ride);
                        System.out.println("Поездка завершена. Стоимость: " + ride.getPrice() + " тг"); break;
                    case "5":
                        ride = selectRide();
                        ratings.rateRide(ride, passenger, Integer.parseInt(ask("Оценка от 1 до 5: ")));
                        save(ride);
                        System.out.println("Спасибо за оценку!"); break;
                    case "6": showDrivers(); break;
                    case "7": report(); break;
                    case "8": export(); break;
                    case "9": dao.findAll().forEach(System.out::println); break;
                    default: System.out.println("Выберите пункт меню.");
                }
            } catch (IllegalArgumentException | IllegalStateException ex) {
                System.out.println("Ошибка: " + ex.getMessage());
            } catch (java.util.NoSuchElementException ex) {
                return;
            }
        }
    }

    private void demo() throws Exception {
        List<User> users = List.of(passenger, drivers.get(0));
        users.forEach(user -> System.out.println(user.describe())); // Полиморфизм.
        Ride ride = new Ride(passenger, Tariff.COMFORT, "Вокзал", "Университет", new BigDecimal("7.5"));
        rides.add(ride);
        System.out.println("Создан заказ: " + ride);
        save(ride);
        service.assignAsync(ride, drivers).join();
        save(ride);
        System.out.println("Назначение: " + ride);
        Ride second = new Ride(passenger, Tariff.COMFORT, "Парк", "Дом", BigDecimal.ONE);
        service.assignAsync(second, drivers).join();
        save(second);
        System.out.println("Повторный заказ при занятом водителе: " + second.getStatus());
        service.startRide(ride, ride.getDriver());
        save(ride);
        System.out.println("Начало: " + ride.getStatus());
        service.completeRide(ride, ride.getDriver());
        save(ride);
        ratings.rateRide(ride, passenger, 5);
        save(ride);
        System.out.println("Результат: " + ride);
        showDrivers();
        report();
        export();
    }
}
