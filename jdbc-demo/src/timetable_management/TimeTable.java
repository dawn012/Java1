package timetable_management;

import Connect.DatabaseUtils;
import Driver.CrudOperations;
import Driver.Name;
import cinema_management.Cinema;
import genre_management.Genre;
import hall_management.Hall;
import movie_management.Movie;
import movie_management.ShowDate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class TimeTable implements CrudOperations {
    private int timetableID;
    private Movie movie;
    private Hall hall;
    private ShowDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;

    // Constructor
    public TimeTable() {
    }

    public TimeTable(int timetableID, Movie movie, Hall hall, ShowDate showDate, LocalTime startTime) {
        this.timetableID = timetableID;
        this.movie = movie;
        this.hall = hall;
        this.showDate = showDate;
        this.startTime = startTime;
        calculateEndTime(movie, startTime);
    }

    public TimeTable(Movie movie, Hall hall, ShowDate showDate){
        this.movie = movie;
        this.hall = hall;
        this.showDate = showDate;
    }

    // Method
    public static ArrayList<TimeTable> viewSchedule(Scanner sc) throws Exception {
        // Cinema
        int cinemaNo = 0;
        boolean error = true;
        ArrayList<Cinema> cinemas = new ArrayList<>();
        do {
            try {
                System.out.print("\nSelect the cinema you want to view the schedule: ");
                cinemas = Cinema.viewCinemaList(1);
                System.out.print("\nEnter the cinema no: ");
                cinemaNo = sc.nextInt();
                sc.nextLine();

                if (cinemaNo > 0 && cinemaNo <= cinemas.size() && cinemas.get(cinemaNo - 1).getStatus() == 1) {
                    error = false;
                }
                else {
                    System.out.println("Your choice is not among the available options! PLease try again.");
                }
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter a valid cinema no!");
                sc.nextLine();
            }
        } while (error);

        // Hall
        int hallNo = 0;
        error = true;
        ArrayList<Hall> halls = new ArrayList<>();
        do {
            try {
                System.out.println("\nSelect the hall: ");
                halls = cinemas.get(cinemaNo - 1).getHallList(1);

                for (int i = 0; i < halls.size(); i++) {
                    System.out.println((i + 1) + ". " + halls.get(i).getHallName().getName());
                }

                System.out.print("\nEnter the hall no: ");
                hallNo = sc.nextInt();
                sc.nextLine();

                if (hallNo > 0 && hallNo <= halls.size() && halls.get(hallNo - 1).getStatus() == 1) {
                    error = false;
                }
                else {
                    System.out.println("Your choice is not among the available options! PLease try again.");
                }
            }
            catch (InputMismatchException e) {
                System.out.println("Please enter a valid hall no!");
                sc.nextLine();
            }
        } while (error);

        // Show Date
        error = true;
        String date = null;
        ShowDate viewDate = null;
        boolean validDate = false;
        do {
            System.out.print("\nEnter movie show date (YYYY-MM-DD): ");
            date = sc.nextLine();

            if (date.trim().isEmpty()) {
                System.out.println("Please enter the show date.");
            } else {
                try {
                    String[] parts = date.split("-");
                    int year = Integer.parseInt(parts[0]);  // Java's built-in method for converting strings to integers (int type)
                    int month = Integer.parseInt(parts[1]);
                    int day = Integer.parseInt(parts[2]);

                    // 验证日期是否 valid
                    viewDate = new ShowDate(year, month, day);
                    validDate = viewDate.isValidDate();

                    if (validDate == true) {
                        error = false;
                    } else {
                        System.out.println("Please enter a valid date!");
                        error = true;
                    }
                } catch (Exception e) {
                    System.out.println("The date format entered in wrong!");
                }
            }
        } while (error);

        ResultSet result = null;
        try {
            Object[] params2 = {halls.get(hallNo - 1).getHallID(), date, 1};
            result = DatabaseUtils.selectQueryById("*", "timeTable", "hall_id = ? AND movie_showDate = ? AND timeTable_status = ?", params2);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<TimeTable> schedules = new ArrayList<>();
        while (result.next()) {
            int timetableID = result.getInt("schedule_id");
            int movieID = result.getInt("movie_id");
            LocalTime startTime = result.getTime("movie_startTime").toLocalTime();

            // Movie
            ResultSet result2 = null;
            Movie movie = new Movie();

            try {
                Object[] params = {movieID};
                result2 = DatabaseUtils.selectQueryById("*", "movie", "movie_id = ? LIMIT 1", params);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            while (result2.next()) {
                movie.setMovieID(result2.getInt("movie_id"));
                movie.setGenre(new Genre(result2.getInt("genre_id")));
                movie.setMvName(new Name(result2.getString("mv_name")));
                movie.setReleaseDate(new ShowDate(result2.getDate("release_date").toLocalDate()));
                movie.setDuration(result2.getInt("duration"));
                movie.setLang(result2.getString("lang"));
                movie.setDirector(result2.getString("director"));
                movie.setWritter(result2.getString("writter"));
                movie.setStarring(result2.getString("starring"));
                movie.setMusicProvider(result2.getString("music"));
                movie.setCountry(result2.getString("country"));
                movie.setMetaDescription(result2.getString("meta_description"));
                movie.setBasicTicketPrice(result2.getDouble("basic_TicketPrice"));
            }

            TimeTable schedule = new TimeTable(timetableID, movie, halls.get(hallNo - 1), viewDate, startTime);
            schedules.add(schedule);
        }

        System.out.println("\nMovie Schedule List for " + date + " at Hall " + halls.get(hallNo - 1).getHallID() + ":\n");
        if (!schedules.isEmpty()) {
            System.out.printf("%-30s %15s %15s\n", "Movie Name", "Start Time", "End Time");
            for (int i = 0; i < schedules.size(); i++) {
                System.out.printf((i + 1) + ". %-20s %17s %17s\n", schedules.get(i).movie.getMvName().getName(), schedules.get(i).startTime, schedules.get(i).endTime);
            }
        }
        else {
            System.out.println("No schedules available for the selected date and hall!");
        }

        return schedules;
    }

    public void add() throws Exception {
        int rowAffected = 0;
        try {
            Object[] params = {hall.getHallID(), movie.getMovieID(), showDate.getDate(), startTime, endTime};
            String sql = "INSERT INTO `timeTable` (`hall_id`, `movie_id`, `movie_showDate`, `movie_startTime`, `movie_endTime`) VALUES (?, ?, ?, ?, ?)";
            rowAffected = DatabaseUtils.insertQuery(sql, params);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (rowAffected > 0) {
            System.out.println("\nSchedule successfully added...");
        }
        else {
            System.out.println("\nSomething went wrong...");
        }
    }

    public void modify() throws Exception {
        int rowAffected = 0;

        try {
            Object[] params = {getHall().getHallID(), getMovie().getMovieID(), String.valueOf(getShowDate().getDate()), startTime, endTime, getTimetableID()};
            String sql = "UPDATE `timeTable` SET `hall_id` = ?, `movie_id` = ?, `movie_showDate` = ?, `movie_startTime` = ?, `movie_endTime` = ? WHERE `schedule_id` = ?";
            rowAffected = DatabaseUtils.updateQuery(sql, params);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (rowAffected > 0) {
            System.out.println("\nSchedule successfully updated...");
        } else {
            System.out.println("\nSomething went wrong...");
        }
    }

    public void delete() throws Exception {
        int rowAffected = 0;

        try {
            Object[] params = {getTimetableID()};
            rowAffected = DatabaseUtils.deleteQueryById("timeTable", "timeTable_status", "schedule_id", params);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        if (rowAffected > 0) {
            System.out.println("\nThe schedule has been deleted.");
        } else {
            System.out.println("\nSomething went wrong...");
        }
    }

    public static ArrayList<LocalDate> generateOneWeekDateList() {
        ArrayList<LocalDate> dateList = new ArrayList<>();
        LocalDate currentDate = LocalDate.now(); // 获取当前日期

        // 生成一周内的日期
        for (int i = 0; i <= 6; i++) { // 一周有7天
            dateList.add(currentDate);  // 将日期添加到列表中
            System.out.println((i + 1) + ". " + dateList.get(i));
            currentDate = currentDate.plusDays(1); // 增加一天
        }

        return dateList;
    }

    public int showHallAndTime(int count, ArrayList<TimeTable> timeTables) throws SQLException {
        ResultSet result = null;
        try {
            Object[] params = {hall.getHallID(), String.valueOf(showDate.getDate()), 1};
            result = DatabaseUtils.selectQueryById("schedule_id, movie_startTime, movie_endTime", "timeTable", "hall_id = ? AND movie_showDate = ? AND timeTable_status = ?", params);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        while (result.next()) {
            TimeTable timeTable = new TimeTable();

            timeTable.timetableID = result.getInt("schedule_id");
            timeTable.startTime = result.getTime("movie_startTime").toLocalTime();
            timeTable.endTime = result.getTime("movie_endTime").toLocalTime();
            timeTable.setHall(hall);

            timeTables.add(timeTable);

            System.out.printf(count + ". %-20s %17s %17s\n", hall.getHallName().getName(), timeTable.startTime, timeTable.endTime);
            count++;
        }

        return count;
    }

    public void calculateEndTime(Movie movie, LocalTime startTime){
        // Change the duration's data type from int to Duration (in minutes)
        Duration duration = Duration.ofMinutes(movie.getDuration());

        endTime = startTime.plusMinutes(duration.toMinutes());
    }

    public static Duration roundUpToNearestFiveMinutes(int duration) {
        int hours = duration / 60;
        int minutes = duration % 60;
        LocalTime time = LocalTime.of(hours, minutes);

        int minute = time.getMinute();
        int roundUpMinute = ((int) Math.ceil(minute / 5.0)) * 5;

        if (roundUpMinute == 60) {
            hours++;
            roundUpMinute = 0;
        }

        return Duration.ofMinutes((hours * 60) + roundUpMinute);
    }

    public LocalTime[] availableTimeSlots(Scanner sc) throws SQLException {
        ResultSet result = null;
        String showDate = String.valueOf(getShowDate().getDate());

        try {
            Object[] params2 = {getHall().getHallID(), showDate};
            result = DatabaseUtils.selectQueryById("movie_startTime, movie_endTime", "timeTable", "hall_id = ? AND movie_showDate = ?", params2);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        ArrayList<LocalTime[]> timeSlots = new ArrayList<>();
        while (result.next()) {
            LocalTime startTime = result.getTime("movie_startTime").toLocalTime();
            LocalTime endTime = result.getTime("movie_endTime").toLocalTime();

            LocalTime[] timeSlot = {startTime, endTime};
            timeSlots.add(timeSlot);
        }

        // 把 duration round up to 5 minutes
        Duration duration = roundUpToNearestFiveMinutes(getMovie().getDuration());

        // 间隔时间 15分钟
        int interval = 5;

        // 营业时间 11:00:00 - 23:00:00
        LocalTime openingTime = LocalTime.of(11, 0, 0);
        LocalTime closingTime = LocalTime.of(23, 0, 0);

        ArrayList<LocalTime[]> availableTimeSlots = new ArrayList<>();
        LocalTime startTime = openingTime;

        while (startTime.plus(duration).isBefore(closingTime)) {
            boolean conflict = false;

            for (LocalTime[] timeSlot : timeSlots) {
                LocalTime scheduledStartTime = timeSlot[0]; // 13:00:00
                LocalTime scheduledEndTime = timeSlot[1]; // 15:10:00

                // 电影开始时间 小过 已经被安排时间表的结束时间 和 电影结束时间 大过 已经被安排时间表的开始时间 => 在它们之间
                if (startTime.isBefore(scheduledEndTime.plusMinutes(15)) && startTime.plus(duration).isAfter(scheduledStartTime.minusMinutes(15))) {
                    conflict = true;
                    break;
                }
            }

            if (!conflict) {
                availableTimeSlots.add(new LocalTime[]{startTime, startTime.plus(duration)});
                startTime = startTime.plusMinutes(10);
            }

            startTime = startTime.plusMinutes(interval);
        }

        boolean error = true;
        int choice = 0;
        do {
            try {
                System.out.println("\nSelect the available time slot: ");
                for (int i = 0; i < availableTimeSlots.size(); i++) {
                    LocalTime[] availableTimeSlot = availableTimeSlots.get(i);
                    System.out.println((i + 1) + ". Start Time: " + availableTimeSlot[0] + " | End Time: " + availableTimeSlot[1]);
                }

                System.out.print("\nEnter your selection: ");
                choice = sc.nextInt();
                sc.nextLine();

                if (choice > 0 && choice <= availableTimeSlots.size()) {
                    error = false;
                } else {
                    System.out.println("Your choice is not among the available options! PLease try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid choice!");
                sc.nextLine();
            }
        } while (error);

        return availableTimeSlots.get(choice - 1);
    }

    public String checkShowDate(){
        int comparison = showDate.getDate().compareTo(movie.getReleaseDate().getDate()); // Compare dates

        if (comparison <= 0) {
            return "The show date cannot be earlier than or equal to release date.";
        }
        else {
            return null;
        }
    }

    // Setter
    public void setTimetableID(int timetableID) {
        this.timetableID = timetableID;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setHall(Hall hall) {
        this.hall = hall;
    }

    public void setShowDate(ShowDate showDate) {
        this.showDate = showDate;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    // Getter
    public int getTimetableID() {
        return timetableID;
    }

    public Movie getMovie() {
        return movie;
    }

    public Hall getHall() {
        return hall;
    }

    public ShowDate getShowDate() {
        return showDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}