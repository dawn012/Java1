package ticket_managemnet;

import Connect.DatabaseUtils;
import booking_management.Booking;
import hall_management.Hall;
import movie_management.Movie;
import seat_management.Seat;
import timetable_management.TimeTable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Ticket {
    private int ticket_id;
    private Seat seat;
    private Booking booking;
    private String ticketType;
    private double price_rate;
    private int ticketStatus;

    public Ticket() {
        this.ticketStatus=1;
    }

//    public Ticket(int ticket_id, Seat seat, Booking booking) {
//        this.ticket_id = ticket_id;
//        this.seat = seat;
//        this.booking = booking;
//    }
    public Ticket(int ticket_id, Seat seat, Booking booking, TimeTable timeTable) {
        this.ticket_id = ticket_id;
        this.seat = seat;
        this.booking = booking;
        this.timeTable = timeTable;
        this.ticketStatus=1;
    }
    public Ticket(int ticket_id, Seat seat, Booking booking, String ticketType, double price_rate, TimeTable timeTable) {
        this.ticket_id = ticket_id;
        this.seat = seat;
        this.booking = booking;
        this.ticketType = ticketType;
        this.price_rate = price_rate;
        this.timeTable = timeTable;
    }

    private TimeTable timeTable;
    //Getter
    public int getTicket_id() {
        return ticket_id;
    }
    public Seat getSeat() {
        return seat;
    }
    public TimeTable getTimeTable() {
        return timeTable;
    }
    public Booking getBooking() {
        return booking;
    }
    public String getTicketType() {
        return ticketType;
    }
    public double getPrice_rate() {
        return price_rate;
    }
    //Setter
    public void setTicket_id(int ticket_id) {
        this.ticket_id=ticket_id;
    }
    public int countTicket_id(int count) {
        this.ticket_id=1;
        //this.ticket_id = ticket_id;
        ArrayList<Ticket> tickets=Ticket.getBookedTicketList();
        for(Ticket t:tickets){
            this.ticket_id++;
        }
        this.ticket_id+=count;
        return this.ticket_id;
    }

    public void setTicketStatus(int ticketStatus) {
        this.ticketStatus = ticketStatus;
    }

    public int getTicketStatus() {
        return ticketStatus;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }
    public void setTimeTable(TimeTable timeTable) {
        this.timeTable = timeTable;
    }
    public void setBooking(Booking booking) {
        this.booking = booking;
    }
    public void setTicketType(String ticketType) {
        this.ticketType = ticketType;
    }
    public void setPrice_rate(double price_rate) {
        this.price_rate = price_rate;
    }
//--------------------------------------------------------------------------------------------------------------------------------
//    public void addTicket() throws Exception {
//        int rowAffected = 0;
//
//        try {
//            String insertSql = "INSERT INTO `ticket` (`ticket_id`,`booking_id`,``,`seat_id`,`schedule_id`,`ticket_type`,`price_rate`) value(?,?,?,?,?,?);";
//            Object[] params = {getSeat_id(),this.hall.getHallID(),getSeatRow(),getSeatCol(),getSeat_status()};
//            rowAffected = DatabaseUtils.insertQuery(insertSql, params);
//        }
//        catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        if (rowAffected > 0) {
//            System.out.println("\nSeat successfully added...");
//        }
//        else {
//            System.out.println("\nSomething went wrong!");
//        }
//    }
    public static ArrayList<Ticket> getBookedTicketList(){
        boolean error = false;
        ArrayList<Ticket> tickets = new ArrayList<>();

        try {
            Object[] params = { };
            ResultSet result = DatabaseUtils.selectQueryById("*", "ticket",null,null);
            //Ticket ticket = null;
            while (result.next()) {
//                String typeC="Child";
//                String typeA="Adult";
//                String type= result.getString("ticket_type");
                Ticket ticket = new Ticket();
//                if(type.equals(typeC)){
//                    ticket = new ChildTicket("Child");
//                }else{
//                    ticket = new AdultTicket("Adult");
//                }
                ticket.setTicketType(result.getString("ticket_type"));
                ticket.setTicket_id(result.getInt("ticket_id"));
                Seat seat = new Seat();
                seat.setSeat_id(result.getString("seat_id"));
                ticket.setSeat(seat);

                Booking booking =new Booking();
                booking.setBooking_id(result.getInt("booking_id"));
                ticket.setBooking(booking);
                //ticket.booking.setBooking_id(result.getInt("booking_id"));
                TimeTable timetable=new TimeTable();
                timetable.setTimetableID(result.getInt("schedule_id"));
                ticket.setBooking(booking);
                //ticket.timeTable.setTimetableID(result.getInt("schedule_id"));
                ticket.setPrice_rate(result.getDouble("price_rate"));
                tickets.add(ticket);

                //seats.add(seat);
            }

            result.close();
            //resultHall.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return tickets;
    }


    public static ArrayList<Ticket> getBookedTicketList(int schedule_id){
        boolean error = false;
        ArrayList<Ticket> tickets = new ArrayList<>();

        try {
            Object[] params = {schedule_id};
            ResultSet result = DatabaseUtils.selectQueryById("*", "ticket","schedule_id = ?",params);
            //Ticket ticket = null;
            while (result.next()) {
//                String typeC="Child";
//                String typeA="Adult";
//                String type= result.getString("ticket_type");
                Ticket ticket = new Ticket();
//                if(type.equals(typeC)){
//                    ticket = new ChildTicket("Child");
//                }else{
//                    ticket = new AdultTicket("Adult");
//                }
                ticket.setTicketType(result.getString("ticket_type"));
                ticket.setTicket_id(result.getInt("ticket_id"));
                Seat seat = new Seat();
                seat.setSeat_id(result.getString("seat_id"));
                ticket.setSeat(seat);
                //ticket.seat.setSeat_id(result.getString("seat_id"));
                Booking booking =new Booking();
                booking.setBooking_id(result.getInt("booking_id"));
                ticket.setBooking(booking);
                //ticket.booking.setBooking_id(result.getInt("booking_id"));
                TimeTable timetable=new TimeTable();
                timetable.setTimetableID(result.getInt("schedule_id"));
                ticket.setBooking(booking);
                //ticket.timeTable.setTimetableID(result.getInt("schedule_id"));
                ticket.setPrice_rate(result.getDouble("price_rate"));
                tickets.add(ticket);

                //seats.add(seat);
            }

            result.close();
            //resultHall.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        return tickets;
    }
    public static void insertTicket(Ticket ticket) throws Exception {
        int rowAffected = 0;

        try {
            String insertSql = "INSERT INTO `ticket` (`ticket_id`,`booking_id`,`seat_id`,`schedule_id`,`ticket_type`,`price_rate`,`ticket_status`) value(?,?,?,?,?,?,?);";
            Object[] params = {ticket.getTicket_id(),ticket.getBooking().getBooking_id(),ticket.getSeat().getSeat_id(),ticket.timeTable.getTimetableID(),ticket.getTicketType(),ticket.getPrice_rate(),ticket.getTicketStatus()};
            rowAffected = DatabaseUtils.insertQuery(insertSql, params);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (rowAffected > 0) {
            System.out.println("\nTicket successfully added...");
        }
        else {
            System.out.println("\nSomething went wrong!");
        }
    }
    public double calculateTicketPrice(){
        return this.timeTable.getMovie().getBasicTicketPrice()*this.price_rate;
        //return movie.getBasicTicketPrice()*this.price_rate;

    }
}
