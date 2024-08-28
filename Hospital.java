package HOSPITAL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;

class Hospital {
    private static final String url = "jdbc:mysql://localhost:3306/hospital";
    private static final String username = "root";
    private static final String password = "root";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Scanner scanner = new Scanner(System.in);
        try {
            Connection connection = DriverManager.getConnection(url, username, password);
            Patients patient = new Patients(connection, scanner);
            Doctors doctor = new Doctors(connection, scanner);
            while (true) {
                System.out.println("1. Add Patient");
                System.out.println("2. View Patients");
                System.out.println("3. View Doctors");
                System.out.println("4. Book Appointment");
                System.out.println("5. Exit");
                System.out.println("Enter your choice:");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        patient.addPatient();
                        break;
                    case 2:
                        patient.viewPatient();
                        break;
                    case 3:
                        doctor.viewDoctors();
                        break;
                    case 4:
                        BookAppointment(connection, scanner);
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        return; // Exit the loop and terminate the program
                    default:
                        System.out.println("Please enter a valid choice.");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    public static void BookAppointment(Connection connection, Scanner scanner) {
        System.out.println("Enter ID:");
        int id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Patient ID: ");
        int patient_id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter Doctor ID:");
        int doctor_id = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter appointment date (YYYY-MM-DD):");
        String appointment_date = scanner.nextLine();

        Patients patient = new Patients(connection, scanner);
        Doctors doctor = new Doctors(connection, scanner);

        if (patient.getPatientById(patient_id) && doctor.getDoctorById(doctor_id)) {
            if (checkDoctorsAvailability(doctor_id, appointment_date, connection)) {
                String appointmentQuery = "insert into appointments(patient_id, doctor_id, appointment_date) values(?,?,?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(appointmentQuery)) {
                    preparedStatement.setInt(1, patient_id);
                    preparedStatement.setInt(2, doctor_id);
                    preparedStatement.setString(3, appointment_date);

                    int rowAffected = preparedStatement.executeUpdate();
                    if (rowAffected > 0) {
                        System.out.println("Appointment booked successfully.");
                    } else {
                        System.out.println("Failed to book appointment.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Doctor not available on this date!");
            }
        } else {
            System.out.println("Either doctor or patient doesn't exist!");
        }
    }

    public static boolean checkDoctorsAvailability(int doctorId, String appointmentDate, Connection connection) {
        String query = "select count(*) from appointments where doctor_id=? and appointment_date=?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setInt(1, doctorId);
            preparedStatement.setString(2, appointmentDate);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count == 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}