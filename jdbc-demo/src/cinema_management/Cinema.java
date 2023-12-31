package cinema_management;

import Connect.DatabaseUtils;
import Driver.CrudOperations;
import Driver.Name;
import hall_management.Hall;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cinema implements CrudOperations {
    private Hall hall;
    private int cinemaID;
    private Name cinemaName;
    private Address cinemaAddress;
    private String cinemaPhone;
    private int status;
    private static ArrayList<Cinema> cinemas = new ArrayList<>();
    private static final String OFFICE_PHONE_REGEX = "^(01[023456789]-[0-9]{7}|011-[0-9]{8}|03-[0-9]{8}|08[0-9]-[0-9]{6}|0[12456789]-[0-9]{7})$";

    public Cinema(){
    }

    public Cinema(int cinemaID, Name cinemaName, Address cinemaAddress, String cinemaPhone) {
        this.cinemaID = cinemaID;
        this.cinemaName = cinemaName;
        this.cinemaAddress = cinemaAddress;
        this.cinemaPhone = cinemaPhone;
    }

    public Cinema(Hall hall, int cinemaID, Name cinemaName, Address cinemaAddress, String cinemaPhone) {
        this.hall = hall;
        this.cinemaID = cinemaID;
        this.cinemaName = cinemaName;
        this.cinemaAddress = cinemaAddress;
        this.cinemaPhone = cinemaPhone;
    }

    static {
        try {
            ResultSet result = DatabaseUtils.selectQueryById("*", "cinema", null, null);

            while (result.next()) {
                Cinema cinema = new Cinema();

                cinema.setCinemaID(result.getInt("cinema_id"));
                cinema.setCinemaName(new Name(result.getString("cinema_name")));
                cinema.setCinemaAddress(new Address(result.getString("cinema_address")));
                cinema.setCinemaPhone(result.getString("cinema_phone"));

                cinemas.add(cinema);
            }

            result.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method
    public static ArrayList<Cinema> viewCinemaList(int status) throws SQLException {
        ArrayList<Cinema> cinemas = new ArrayList<>();

        try {
            Object[] params = {status};
            ResultSet result = DatabaseUtils.selectQueryById("*", "cinema", "cinema_status = ?", params);

            while (result.next()) {
                Cinema cinema = new Cinema();

                cinema.setCinemaID(result.getInt("cinema_id"));
                cinema.setCinemaName(new Name(result.getString("cinema_name")));
                cinema.setCinemaAddress(new Address(result.getString("cinema_address")));
                cinema.setCinemaPhone(result.getString("cinema_phone"));
                cinema.setStatus(result.getInt("cinema_status"));

                cinemas.add(cinema);
            }

            result.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.printf("\n%-5s %s\n", "No", "Cinema Name");
        for (int i = 0; i < cinemas.size(); i++) {
            System.out.printf("%-5d %s\n", (i + 1), cinemas.get(i).getCinemaName().getName());
        }

        return cinemas;
    }

    public void viewCinemaDetails() throws SQLException {
        System.out.printf("\nCinema Detail:\n");
        System.out.println("Cinema Name: " + getCinemaName().getName());
        System.out.println("Cinema Address: " + getCinemaAddress().getAddress());
        System.out.println("Cinema Phone: " + getCinemaPhone());
    }

    public boolean isValidOfficePhoneNumber() {
        Pattern pattern = Pattern.compile(OFFICE_PHONE_REGEX);
        Matcher matcher = pattern.matcher(cinemaPhone);

        return matcher.matches();
    }

    public void add() throws SQLException {
        int rowAffected = 0;

        try {
            Object[] params = {getCinemaName().getName(), getCinemaAddress().getAddress(), getCinemaPhone()};
            String sql = "INSERT INTO `cinema` (`cinema_name`, `cinema_address`, `cinema_phone`) VALUES (?, ?, ?)";
            rowAffected = DatabaseUtils.insertQuery(sql, params);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (rowAffected > 0) {
            System.out.println("\nCinema successfully added...");
        }
        else {
            System.out.println("\nSomething went wrong...");
        }
    }

    public int modifyCinemaDetails(Scanner sc) {
        boolean error = true;

        do {
            try {
                int count = 1;
                System.out.printf("\nCinema Detail:\n");
                System.out.println(count + ". Cinema Name: " + getCinemaName().getName());
                count++;
                System.out.println(count + ". Cinema Address: " + getCinemaAddress().getAddress());
                count++;
                System.out.println(count + ". Cinema Phone: " + getCinemaPhone());

                System.out.print("\nEnter the serial number of the cinema information you want to change (0 - Stop): ");
                int serialNum = sc.nextInt();
                sc.nextLine();

                if (serialNum < 0 || serialNum > count) {
                    System.out.println("Your choice is not among the available options! PLease try again.");
                } else {
                    return serialNum;
                }
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter a valid choice!");
                sc.nextLine();
                error = true;
            }
        } while (error);

        return 0;
    }

    public void modify() throws SQLException {
        try {
            String updateSql = "UPDATE `cinema` SET `cinema_name`= ?, `cinema_address`= ?, `cinema_phone`= ? WHERE cinema_id = ?";
            Object[] params = {getCinemaName().getName(), getCinemaAddress().getAddress(), getCinemaPhone(), getCinemaID()};
            int rowAffected = DatabaseUtils.updateQuery(updateSql, params);
            if (rowAffected > 0) {
                System.out.println("\nThe changes have been saved.");
            } else {
                System.out.println("\nSomething went wrong...");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete() throws SQLException {
        try {
            Object[] params = {getCinemaID()};
            int rowAffected = DatabaseUtils.deleteQueryById("cinema", "cinema_status", "cinema_id", params);

            if (rowAffected > 0) {
                System.out.println("\nThe cinema has been deleted.");
            } else {
                System.out.println("\nSomething went wrong...");
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Hall> getHallList(int status) throws SQLException {
        ArrayList<Hall> halls = new ArrayList<>();

        try {
            Object[] params = {cinemaID, status};
            ResultSet result = DatabaseUtils.selectQueryById("*", "hall", "cinema_id = ? AND hall_status = ?", params);

            while (result.next()) {
                Hall hall = new Hall();

                hall.setHallID(result.getInt("hall_id"));
                hall.setHallName(new Name(result.getString("hall_name")));
                hall.setHallType(result.getString("hall_type"));
                hall.calHallCapacity();
                hall.setStatus(result.getInt("hall_status"));

                halls.add(hall);
            }

            result.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return halls;
    }

    public boolean addHall(Hall hall){
        int rowAffected = 0;
        boolean insertError;

        try {
            Object[] params = {cinemaID, hall.getHallName().getName(), hall.getHallType(), hall.getHallCapacity()};
            String sql = "INSERT INTO `hall` (`cinema_id`, `hall_name`, `hall_type`, `hall_capacity`) VALUES (?, ?, ?, ?)";
            rowAffected = DatabaseUtils.insertQuery(sql, params);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (rowAffected > 0) {
            System.out.println("\nHall successfully added...");
            insertError = false;
        }
        else {
            System.out.println("\nSomething went wrong...");
            insertError = true;
        }
        return insertError;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public void setCinemaID(int cinemaID) {
        this.cinemaID = cinemaID;
    }

    public void setCinemaName(Name cinemaName) {
        this.cinemaName = cinemaName;
    }

    public void setCinemaAddress(Address cinemaAddress) {
        this.cinemaAddress = cinemaAddress;
    }

    public void setCinemaPhone(String cinemaPhone) {
        this.cinemaPhone = cinemaPhone;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Hall getHall() {
        return hall;
    }

    public int getCinemaID() {
        return cinemaID;
    }

    public Name getCinemaName() {
        return cinemaName;
    }

    public Address getCinemaAddress() {
        return cinemaAddress;
    }

    public String getCinemaPhone() {
        return cinemaPhone;
    }

    public int getStatus() {
        return status;
    }

    public static ArrayList<Cinema> getCinemas() {
        return cinemas;
    }
}