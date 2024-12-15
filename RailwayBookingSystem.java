import java.io.*;
import java.sql.*;
import java.util.*;

//RailWay Booking System
class RailwayBookingSystem {
    List<TrainSchedule> trainSchedules = new ArrayList<>();
    Map<Integer, TrainSchedule> trainScheduleMap = new HashMap<>();
    Set<PassengerBooking> passengerBookings = new HashSet<>();
    Queue<PassengerBooking> bookingQueue = new LinkedList<>();
    TreeMap<String, TrainSchedule> trainScheduleTreeMap = new TreeMap<>();//sort train
    CustomLinkedList passengerList = new CustomLinkedList();//to display passenger details

    // File paths
     static final String TRAIN_FILE = "trains.txt";
     static final String PASSENGER_FILE = "passengers.txt";

    // Database connection details
     static final String DB_URL = "jdbc:mysql://localhost:3306/railway";
     static final String DB_USER = "root";
     static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        RailwayBookingSystem system = new RailwayBookingSystem();
        //Load Train Schedule from DB 
        system.loadTrainSchedules(); 
        //Load passenger Schedule from DB
        system.loadPassengerBookingsFromDatabase(); 
        //Display the menu
        system.displayMenu();  
    }

    // Load Train Schedule using procedure
    void loadTrainSchedules() {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            // Call the stored procedure
            String query = "{CALL LoadTrainSchedules()}";
            CallableStatement callableStatement = connection.prepareCall(query);
            
            // Execute the procedure
            ResultSet resultSet = callableStatement.executeQuery();
    
            // Clear the previous data in the collections
            trainSchedules.clear();
            trainScheduleMap.clear();
            trainScheduleTreeMap.clear();
    
            // Iterate through the results
            while (resultSet.next()) {
                TrainSchedule schedule = new TrainSchedule();
                schedule.setTrainId(resultSet.getInt("train_id"));
                schedule.setTrainName(resultSet.getString("train_name"));
                schedule.setSource(resultSet.getString("source"));
                schedule.setDestination(resultSet.getString("destination"));
                schedule.setDepartureTime(resultSet.getString("departure_time"));
                schedule.setArrivalTime(resultSet.getString("arrival_time"));
                schedule.setTotalSeats(resultSet.getInt("total_seats"));
                schedule.setAvailableSeats(resultSet.getInt("available_seats"));
                schedule.setTravelDate(resultSet.getString("travel_date"));  // Set travel date
                
                // Add to the lists and maps
                trainSchedules.add(schedule);
                trainScheduleMap.put(schedule.getTrainId(), schedule);
    
                // Sort by departure time and travel date
                String key = schedule.getTravelDate() + " " + schedule.getDepartureTime();
                trainScheduleTreeMap.put(key, schedule);
            }
    
            // Optionally save the train schedules to file
            saveTrainSchedules();
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // Load passenger schedule function
    void loadPassengerBookingsFromDatabase() {
        String bookingQuery = "SELECT * FROM passenger_bookings";
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet bookingRs = statement.executeQuery(bookingQuery)) {

            while (bookingRs.next()) {
                PassengerBooking booking = new PassengerBooking(
                    bookingRs.getString("name"),
                    bookingRs.getInt("age"),
                    bookingRs.getString("gender"),bookingRs.getString("source"),bookingRs.getString("destination"),bookingRs.getString("travel_date"),bookingRs.getInt("travel_class"),bookingRs.getString("mobile_number"),bookingRs.getInt("train_id"));
              passengerBookings.add(booking);
              bookingQueue.add(booking);
            }
            //load passenger schedule to file
            savePassengerBookings(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to save Train Schedule in trains.txt file
     void saveTrainSchedules() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TRAIN_FILE))) {
            for (TrainSchedule schedule : trainSchedules) {
                writer.write(schedule.getTrainId() + "," +
                        schedule.getTrainName() + "," +
                        schedule.getSource() + "," +
                        schedule.getDestination() + "," +
                        schedule.getDepartureTime() + "," +
                        schedule.getArrivalTime() + "," +
                        schedule.getTotalSeats() + "," +
                        schedule.getAvailableSeats() + "," +
                        schedule.getTravelDate());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function to save passenger Schedule to passenger.txt file
      void savePassengerBookings() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PASSENGER_FILE))) {
            for (PassengerBooking booking : passengerBookings) {
                writer.write(booking.getName() + "," +
                        booking.getAge() + "," +
                        booking.getGender() + "," +
                        booking.getSource() + "," +
                        booking.getDestination() + "," +
                        booking.getTravelDate() + "," +
                        booking.getTravelClass() + "," +
                        booking.getMobileNumber() + "," +
                        booking.getTrainId());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Display menu function
     void displayMenu() {
        Scanner scanner = new Scanner(System.in);
        int choice;
        System.out.println("**************Welcome to Railway Booking System****************");
        try{
        while (true) {
            System.out.println("            - Press 1. Book Ticket");
            System.out.println("            - Press 2. Check Seat Availability");
            System.out.println("            - Press 3. Cancel Booking");
            System.out.println("            - Press 4. Manage Schedules");
            System.out.println("            - Press 5. Display Passenger Details");
            System.out.println("            - Press 6. Fetch and Display Passenger Booking with Train Name");
            System.out.println("            - Press 7. Generate Reports");
            System.out.println("            - Press 8. Exit");
            System.out.println("            - Enter your choice.");
            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    bookTicket();
                    break;
                case 2:
                    checkSeatAvailability();
                    break;
                case 3:
                    cancelBooking();
                    break;
                case 4:
                    manageSchedules();
                    break;
                case 5:
                    displayPassengerList();
                    break;
                case 6:
                    fetchAndDisplayPassengersWithTrainDetails();
                    break;
                case 7:
                    generateReports();
                    break;
                case 8:
                System.out.println("************Exiting from RailWay Booking System************");
                    System.exit(0);
                    break;
                default:
                    System.out.println("        Invalid choice. Please try again.");
            }
        }
    }catch(Exception e)
    {
        System.out.println("                   InputMismatchException");
        displayMenu();
    }
 }
    
    // Function for Ticket Booking
     void bookTicket() {
        String mobileNumber;
        Scanner scanner = new Scanner(System.in);
        System.out.print("              Enter Passenger Name: ");
        String name = scanner.nextLine();
        System.out.print("              Enter Age: ");
        int age = scanner.nextInt();
        scanner.nextLine();  // consume newline
        System.out.print("              Enter Gender: ");
        String gender = scanner.nextLine();
        System.out.print("              Enter Source: ");
        String source = scanner.nextLine();
        System.out.print("              Enter Destination: ");
        String destination = scanner.nextLine();
        System.out.print("              Enter Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        System.out.print("              Enter Class (1: First, 2: Second, 3: Third): ");
        int travelClass = scanner.nextInt();
        scanner.nextLine();  // consume newline
        do{
        System.out.print("              Enter Mobile Number: ");
        mobileNumber = scanner.nextLine();
        }while(mobileNumber.length()!=10);        
        // Check availability
        List<TrainSchedule> availableTrains = checkTrainAvailability(source, destination, date);
        if (availableTrains.isEmpty()) {
            System.out.println("        No trains available for the selected route and date.");
            return;
        }
    
        System.out.println("              Available Trains:");
        for (TrainSchedule train : availableTrains) {
            System.out.println("              Train ID: " + train.getTrainId() + ", Train Name: " + train.getTrainName());
        }
    
        System.out.print("              Enter Train ID to book: ");
        int trainId = scanner.nextInt();
    
        TrainSchedule selectedTrain = trainScheduleMap.get(trainId);
        if (selectedTrain == null || selectedTrain.getAvailableSeats() <= 0) {
            System.out.println("        Selected train is not available or fully booked.");
            return;
        }
    
        // Calculate fare
        double fare = calculateFare(travelClass);
        System.out.println("              Total Fare: " + fare);
    
        // Process payment
        if (processPayment(fare)) {
            // Store booking information
            PassengerBooking booking = new PassengerBooking(name, age, gender, source, destination, date, travelClass, mobileNumber, trainId);
            passengerBookings.add(booking);
            bookingQueue.add(booking); // Add to booking queue
            passengerList.insert(booking); // Add to custom linked list
            selectedTrain.setAvailableSeats(selectedTrain.getAvailableSeats() - 1);
    
            // Insert passenger details into database
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String passengerQuery = "INSERT INTO passenger_bookings (name, age, gender, source, destination, travel_date, travel_class, mobile_number, train_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement passengerStatement = connection.prepareStatement(passengerQuery);
                passengerStatement.setString(1, name);
                passengerStatement.setInt(2, age);
                passengerStatement.setString(3, gender);
                passengerStatement.setString(4, source);
                passengerStatement.setString(5, destination);
                passengerStatement.setString(6, date);
                passengerStatement.setInt(7, travelClass);
                passengerStatement.setString(8, mobileNumber);
                passengerStatement.setInt(9, trainId);
                passengerStatement.executeUpdate();
                System.out.println("       Passenger details saved successfully.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("             Ticket booked successfully. Booking details: " + booking);
            //save passenger booking to passenger.txt file
            savePassengerBookings();
        } else {
            System.out.println("           Payment failed. Booking not completed.");
        }
    }


    //Display passenger list Using Linked list
     void displayPassengerList() {
        System.out.println("               Passenger Details :");
        passengerList.display();
    }
    
    //check Train Availability
     List<TrainSchedule> checkTrainAvailability(String source, String destination, String date) {
        List<TrainSchedule> availableTrains = new ArrayList<>();
        for (TrainSchedule train : trainSchedules) {
            if (train.getSource().equalsIgnoreCase(source) && train.getDestination().equalsIgnoreCase(destination) && train.getAvailableSeats() > 0) {
                availableTrains.add(train);
            }
        }
        return availableTrains;
    }
    
    // Calculation of fare on the base of travel class
     double calculateFare(int travelClass) {
        double baseFare = 100.0;
        switch (travelClass) {
            case 1:
                return baseFare * 2;  // First class
            case 2:
                return baseFare * 1.5;  // Second class
            case 3:
                return baseFare;  // Third class
            default:
                throw new IllegalArgumentException("               Invalid travel class");
        }
    }
    
    // Simulate payment processing
     boolean processPayment(double amount) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("              Enter payment amount: ");
        double payment = scanner.nextDouble();
        return payment >= amount;
    }
    
    // Check seat availability
     void checkSeatAvailability() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("             Enter Source: ");
        String source = scanner.nextLine();
        System.out.print("             Enter Destination: ");
        String destination = scanner.nextLine();
        System.out.print("             Enter Date (YYYY-MM-DD): ");
        String date = scanner.nextLine();
        
        List<TrainSchedule> availableTrains = checkTrainAvailability(source, destination, date);
        if (availableTrains.isEmpty()) {
            System.out.println("       No trains available for the selected route and date.");
        } else {
            System.out.println("Available Trains:");
            for (TrainSchedule train : availableTrains) {
                System.out.println("Train ID: " + train.getTrainId() + ", Train Name: " + train.getTrainName() + ", Available Seats: " + train.getAvailableSeats());
            }
        }
    }

    // Function for booking cancellation
     void cancelBooking() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("          Enter Passenger Name: ");
        String name = scanner.nextLine();
        System.out.print("          Enter Mobile Number: ");
        String mobileNumber = scanner.nextLine();

        PassengerBooking bookingToCancel = null;
        for (PassengerBooking booking : passengerBookings) {
            if (booking.getName().equalsIgnoreCase(name) && booking.getMobileNumber().equals(mobileNumber)) {
                bookingToCancel = booking;
                break;
            }
        }

        if (bookingToCancel == null) {
            System.out.println("    Booking not found.");
            return;
        }

        // Remove booking from the set and update available seats
        passengerBookings.remove(bookingToCancel);
        // Remove from booking queue
        bookingQueue.remove(bookingToCancel); 
        // Deleting a passenger
        passengerList.deleteByNameAndPhone(name, mobileNumber);


        TrainSchedule train = trainScheduleMap.get(bookingToCancel.getTrainId());
        if (train != null) {
            train.setAvailableSeats(train.getAvailableSeats() + 1);

            // Delete booking from database
            try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                String deleteQuery = "DELETE FROM passenger_bookings WHERE name = ? AND mobile_number = ?";
                PreparedStatement deleteStatement = connection.prepareStatement(deleteQuery);
                deleteStatement.setString(1, name);
                deleteStatement.setString(2, mobileNumber);
                deleteStatement.executeUpdate();
                System.out.println("           Booking cancelled and database updated.");
                savePassengerBookings();
                displayPassengerList();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Manage Train Schedule 
     void manageSchedules() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("---------------Manage Train Schedules-----------------");
        System.out.println("               Press 1. Add Schedule");
        System.out.println("               Press 2. Update Schedule");
        System.out.println("               Press 3. Delete Schedule");
        System.out.println("               Press 4. View trains");
        System.out.println("               Press 5 To Exit");
        try{
        int choice = scanner.nextInt();
        // consume newline
        scanner.nextLine(); 

        switch (choice) {
            case 1:
                addSchedule();
                break;
            case 2:
                updateSchedule();
                break;
            case 3:
                deleteSchedule();
                break;
            case 4:
                viewSchedules();
                break;
            case 5: 
                System.out.println("-----------EXIT------------");
            default:
                System.out.println("           Invalid choice.");
        }
    }catch(Exception e)
    {
        System.out.println("                   InputMismatchException");
        System.out.println("                   Invalid input. Please Enter a number.");
        manageSchedules(); 
    }
}

    // Add Train Schedule
    void addSchedule() {
        Scanner scanner = new Scanner(System.in);
        TrainSchedule schedule = new TrainSchedule();
        System.out.print("                Enter Train ID: ");
        schedule.setTrainId(scanner.nextInt());
        scanner.nextLine();  // consume newline
        System.out.print("                Enter Train Name: ");
        schedule.setTrainName(scanner.nextLine());
        System.out.print("                Enter Source: ");
        schedule.setSource(scanner.nextLine());
        System.out.print("                Enter Destination: ");
        schedule.setDestination(scanner.nextLine());
        System.out.print("                Enter Departure Time (HH:MM): ");
        schedule.setDepartureTime(scanner.nextLine());
        System.out.print("                Enter Arrival Time (HH:MM): ");
        schedule.setArrivalTime(scanner.nextLine());
        System.out.print("                Enter Total Seats: ");
        schedule.setTotalSeats(scanner.nextInt());
        System.out.print("                Enter Available Seats: ");
        schedule.setAvailableSeats(scanner.nextInt());
        scanner.nextLine();  // consume newline
        System.out.print("                Enter Travel Date (YYYY-MM-DD): ");
        schedule.setTravelDate(scanner.nextLine());
    
        trainSchedules.add(schedule);
        trainScheduleMap.put(schedule.getTrainId(), schedule);
        String key = schedule.getTravelDate() + " " + schedule.getDepartureTime();
        trainScheduleTreeMap.put(key, schedule);
    
        // Insert schedule into database
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "INSERT INTO train_schedules (train_id, train_name, source, destination, departure_time, arrival_time, total_seats, available_seats, travel_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, schedule.getTrainId());
            statement.setString(2, schedule.getTrainName());
            statement.setString(3, schedule.getSource());
            statement.setString(4, schedule.getDestination());
            statement.setString(5, schedule.getDepartureTime());
            statement.setString(6, schedule.getArrivalTime());
            statement.setInt(7, schedule.getTotalSeats());
            statement.setInt(8, schedule.getAvailableSeats());
            statement.setString(9, schedule.getTravelDate());
            statement.executeUpdate();
            System.out.println("            Schedule added and database updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Update Train Schedule
     void updateSchedule() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("                Enter Train ID to update: ");
        int trainId = scanner.nextInt();
        scanner.nextLine();  // consume newline
    
        TrainSchedule schedule = trainScheduleMap.get(trainId);
        if (schedule == null) {
            System.out.println("          Train ID not found!!!!!!!");
            return;
        }
    
        System.out.print("                Enter new Train Name (leave blank to keep unchanged): ");
        String trainName = scanner.nextLine();
        if (!trainName.isEmpty()) schedule.setTrainName(trainName);
    
        System.out.print("                Enter new Source (leave blank to keep unchanged): ");
        String source = scanner.nextLine();
        if (!source.isEmpty()) schedule.setSource(source);
    
        System.out.print("                Enter new Destination (leave blank to keep unchanged): ");
        String destination = scanner.nextLine();
        if (!destination.isEmpty()) schedule.setDestination(destination);
    
        System.out.print("                Enter new Departure Time (HH:MM) (leave blank to keep unchanged): ");
        String departureTime = scanner.nextLine();
        if (!departureTime.isEmpty()) schedule.setDepartureTime(departureTime);
    
        System.out.print("                Enter new Arrival Time (HH:MM) (leave blank to keep unchanged): ");
        String arrivalTime = scanner.nextLine();
        if (!arrivalTime.isEmpty()) schedule.setArrivalTime(arrivalTime);
    
        System.out.print("                Enter new Total Seats (leave blank to keep unchanged): ");
        String totalSeats = scanner.nextLine();
        if (!totalSeats.isEmpty()) schedule.setTotalSeats(Integer.parseInt(totalSeats));
    
        System.out.print("                Enter new Available Seats (leave blank to keep unchanged): ");
        String availableSeats = scanner.nextLine();
        if (!availableSeats.isEmpty()) schedule.setAvailableSeats(Integer.parseInt(availableSeats));
    
        System.out.print("               Enter new Travel Date (YYYY-MM-DD) (leave blank to keep unchanged): ");
        String travelDate = scanner.nextLine();
        if (!travelDate.isEmpty()) schedule.setTravelDate(travelDate);
    
        trainScheduleMap.put(trainId, schedule);
        String key = schedule.getTravelDate() + " " + schedule.getDepartureTime();
        trainScheduleTreeMap.put(key, schedule);
    
        // Update schedule in database
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "UPDATE train_schedules SET train_name = ?, source = ?, destination = ?, departure_time = ?, arrival_time = ?, total_seats = ?, available_seats = ?, travel_date = ? WHERE train_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, schedule.getTrainName());
            statement.setString(2, schedule.getSource());
            statement.setString(3, schedule.getDestination());
            statement.setString(4, schedule.getDepartureTime());
            statement.setString(5, schedule.getArrivalTime());
            statement.setInt(6, schedule.getTotalSeats());
            statement.setInt(7, schedule.getAvailableSeats());
            statement.setString(8, schedule.getTravelDate());
            statement.setInt(9, trainId);
            statement.executeUpdate();
            System.out.println("           Schedule updated and database updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete Train Schedule
     void deleteSchedule() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("                Enter Train ID to delete: ");
        int trainId = scanner.nextInt();
        scanner.nextLine();  // consume newline
    
        TrainSchedule schedule = trainScheduleMap.remove(trainId);
        if (schedule == null) {
            System.out.println("          Train ID not found.");
            return;
        }
    
        trainSchedules.remove(schedule);
        String key = schedule.getTravelDate() + " " + schedule.getDepartureTime();
        trainScheduleTreeMap.remove(key);
    
        // Delete schedule from database
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "DELETE FROM train_schedules WHERE train_id = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, trainId);
            statement.executeUpdate();
            System.out.println("          Schedule deleted and database updated.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // View Train schedule
     void viewSchedules() {
        if (trainSchedules.isEmpty()) {
            System.out.println("          No train schedules available!!!!");
        } else {
            System.out.println("          Train Schedules:");
            for (TrainSchedule schedule : trainSchedules) {
                System.out.println("Train ID: " + schedule.getTrainId() + ", Train Name: " + schedule.getTrainName() + ", Source: " + schedule.getSource() + ", Destination: " + schedule.getDestination() + ", Departure Time: " + schedule.getDepartureTime() + ", Arrival Time: " + schedule.getArrivalTime() + ", Total Seats: " + schedule.getTotalSeats() + ", Available Seats: " + schedule.getAvailableSeats() + ", Travel Date: " + schedule.getTravelDate());
            }
        }
    }

    // Generate Report in report.txt File
    void generateReports() {
        // Report generation logic
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("report.txt"))) {
                writer.write("Train Schedule Report\n");
                writer.write("====================\n");
                for (TrainSchedule schedule : trainSchedules) {
                    writer.write("Train ID: " + schedule.getTrainId() + ", Train Name: " + schedule.getTrainName() + ", Source: " + schedule.getSource() + ", Destination: " + schedule.getDestination() +", Travel date: "+schedule.getTravelDate()+ ", Departure Time: " + schedule.getDepartureTime() + ", Arrival Time: " + schedule.getArrivalTime() + ", Available Seats: " + schedule.getAvailableSeats() +  "\n");
                }
                writer.write("\nPassenger Bookings Report\n");
                writer.write("========================\n");
                for (PassengerBooking booking : passengerBookings) {
                    writer.write("Passenger Name: " + booking.getName() + ", Age: " + booking.getAge() + ", Gender: " + booking.getGender() + ", Source: " + booking.getSource() + ", Destination: " + booking.getDestination() +", Travel date: "+booking.getTravelDate()+ ", Travel Class: " + booking.getTravelClass() + ", Mobile Number: " + booking.getMobileNumber() + "\n");
                }
                System.out.println("                 Report generated successfully.");
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("                     Passenger Booking Report with Fare:");
            System.out.println("                     ----------------------------------");
            
            if (passengerBookings.isEmpty()) {
                System.out.println("                 No bookings found.");
            } else {
                for (PassengerBooking booking : passengerBookings) {
                    System.out.println("               Name: " + booking.getName());
                    System.out.println("               Age: " + booking.getAge());
                    System.out.println("               Gender: " + booking.getGender());
                    System.out.println("               Source: " + booking.getSource());
                    System.out.println("               Destination: " + booking.getDestination());
                    System.out.println("               Date: " + booking.getTravelDate());
                    System.out.println("               Class: " + booking.getTravelClass());
                    System.out.println("               Mobile Number: " + booking.getMobileNumber());
                    System.out.println("               Train ID: " + booking.getTrainId());
                    // Fetch and display fare
                    System.out.println("               Fare Paid: " + calculateFare(booking.getTravelClass()));
                    System.out.println("             --------------------------------------");
                }
            }
        System.out.println("-------------------------Generate Reports------------------------:");
        System.out.println("                         Press 1. All Trains");
        System.out.println("                         Press 2. All Bookings");
        System.out.println("                         Press 3. Exit no report to generate");
        Scanner scanner = new Scanner(System.in);
        try{
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                displayAllTrains();
                break;
            case 2:
                displayAllBookings();
                break;
            case 3:
                System.out.println("-----------------No report generated!!!!!");
                break;
            default:
                System.out.println("                 Invalid choice.");
        }
    }catch(Exception e)
    {
             System.out.println("                   InputMismatchException");
             generateReports(); 
    }
    }

    // To display the Trains 
     void displayAllTrains() {
        System.out.println("                        Train Schedules:");
        for (TrainSchedule train : trainSchedules) {
            System.out.println(train);
        }
    }

    // To display all bookings
     void displayAllBookings() {
        System.out.println("                        Passenger Bookings:");
        for (PassengerBooking booking : passengerBookings) {
            System.out.println(booking);
        }
    }

    // Fetch and display passenger booking details with train name
     void fetchAndDisplayPassengersWithTrainDetails() {
        String query = "SELECT p.id, p.name, p.age, p.gender, p.source, p.destination, p.travel_date, p.travel_class, p.mobile_number, t.train_name " +
                       "FROM passenger_bookings p " +
                       "INNER JOIN train_schedules t ON p.train_id = t.train_id";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id=rs.getInt("id");
                String name = rs.getString("name");
                int age=rs.getInt("age");
                String gender=rs.getString("gender");
                String source=rs.getString("source");
                String destination=rs.getString("destination");
                String travelDate=rs.getString("travel_date");
                String travelClass=rs.getString("travel_class");
                String phoneNumber = rs.getString("mobile_number");
                String trainName = rs.getString("train_name");

                System.out.println("----------Passenger Booking Details-----------");
                System.out.println("            Id: "+id);
                System.out.println("            Name: "+name);
                System.out.println("            Age: "+age);
                System.out.println("            Gender: "+gender);
                System.out.println("            Source: "+source);
                System.out.println("            Destination: "+destination);
                System.out.println("            Travel Date: "+travelDate);
                System.out.println("            Travel Class: "+travelClass);
                System.out.println("            Mobile Number: "+phoneNumber);
                System.out.println("            Train Name: "+trainName);
                System.out.println("**********************************************");
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


class TrainSchedule {
    private int trainId;
    private String trainName;
    private String source;
    private String destination;
    private String departureTime;
    private String arrivalTime;
    private int totalSeats;
    private int availableSeats;
    private String travelDate;

    // Getters and setters
     TrainSchedule() {}

     TrainSchedule(int trainId, String trainName, String source, String destination, String departureTime, String arrivalTime, int totalSeats, int availableSeats, String travelDate) {
        this.trainId = trainId;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.travelDate = travelDate;
    }

     int getTrainId() {
        return trainId;
    }

     void setTrainId(int trainId) {
        this.trainId = trainId;
    }

     String getTrainName() {
        return trainName;
    }

    void setTrainName(String trainName) {
        this.trainName = trainName;
    }

     String getSource() {
        return source;
    }

     void setSource(String source) {
        this.source = source;
    }

    String getDestination() {
        return destination;
    }

    void setDestination(String destination) {
        this.destination = destination;
    }

     String getDepartureTime() {
        return departureTime;
    }

     void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

     String getArrivalTime() {
        return arrivalTime;
    }

     void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

     int getTotalSeats() {
        return totalSeats;
    }

     void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

     int getAvailableSeats() {
        return availableSeats;
    }

     void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

     String getTravelDate() {
        return travelDate;
    }

     void setTravelDate(String travelDate2) {
        this.travelDate = travelDate2;
    }

    @Override
    public String toString() {
        return "Train ID: " + trainId + ", Name: " + trainName + ", Source: " + source + ", Destination: " + destination +
                ", Departure: " + departureTime + ", Arrival: " + arrivalTime + ", Total Seats: " + totalSeats +
                ", Available Seats: " + availableSeats + ", Date: " + travelDate;
    }
}

class PassengerBooking {
    private String name;
    private int age;
    private String gender;
    private String source;
    private String destination;
    private String travelDate;
    private int travelClass;
    private String mobileNumber;
    private int trainId;
    // Getters and setters
     PassengerBooking(String name, int age, String gender, String source, String destination, String travelDate, int travelClass, String mobileNumber, int trainId) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.source = source;
        this.destination = destination;
        this.travelDate = travelDate;
        this.travelClass = travelClass;
        this.mobileNumber = mobileNumber;
        this.trainId = trainId;
    }

    String getName() {
        return name;
    }

     int getAge() {
        return age;
    }

     String getGender() {
        return gender;
    }

   String getSource() {
        return source;
    }

     String getDestination() {
        return destination;
    }

     String getTravelDate() {
        return travelDate;
    }

     int getTravelClass() {
        return travelClass;
    }

     String getMobileNumber() {
        return mobileNumber;
    }

     int getTrainId() {
        return trainId;
    }
    @Override
public String toString() {
    return "Name: " + name + ", Age: " + age + ", Gender: " + gender + 
           ", Source: " + source + ", Destination: " + destination + 
           ", Travel Date: " + travelDate + ", Class: " + travelClass + 
           ", Mobile Number: " + mobileNumber + ", Train ID: " + trainId;
}
}

// Node class to represent each element in the linked list
class Node {
    PassengerBooking data;
    Node next;

    Node(PassengerBooking data) {
        this.data = data;
        this.next = null;
    }
}

// Custom LinkedList class with methods to manage the list
class CustomLinkedList {
     Node head;

    // Constructor to initialize an empty list
    CustomLinkedList() {
        head = null;
    }

    // Method to insert a passenger into the list
     void insert(PassengerBooking passenger) {
        Node newNode = new Node(passenger);
        if (head == null) {
            head = newNode;
        } else {
            Node current = head;
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
    }

    // Method to delete a passenger by name and phone number
     void deleteByNameAndPhone(String name, String phoneNumber) {
        if (head == null) {
            System.out.println("-----------------The list is empty.-------------------");
            return;
        }

        // Handle deletion of the head node if it matches
        if (head.data.getName().equals(name) && head.data.getMobileNumber().equals(phoneNumber)) {
            head = head.next;
            System.out.println("-----------------Passenger deleted successfully.----------------");
            return;
        }

        Node current = head;
        while (current.next != null) {
            if (current.next.data.getName().equals(name) && current.next.data.getMobileNumber().equals(phoneNumber)) {
                current.next = current.next.next;
                System.out.println("-------------Passenger deleted successfully.-----------------");
                return;
            }
            current = current.next;
        }

        System.out.println("---------------------Passenger not found.----------------------------");
    }

    // Method to display all passengers in the list
     void display() {
        if (head == null) {
            System.out.println("-----------------The list is empty.------------------------------");
            return;
        }

        Node current = head;
        while (current != null) {
            System.out.println(current.data);
            current = current.next;
        }
    }
}

