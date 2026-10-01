package parking;

import parking.service.ParkingService;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        ParkingService parkingService = new ParkingService();

        int choice;

        do {

            System.out.println();
            System.out.println("==============================");
            System.out.println("   SMART PARKING SYSTEM");
            System.out.println("==============================");
            System.out.println("1. Park Vehicle");
            System.out.println("2. Exit Vehicle");
            System.out.println("3. View Parking Slots");
            System.out.println("4. Exit Program");
            System.out.println("==============================");
            System.out.print("Enter choice: ");

            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {

                case 1:
                    System.out.print("Enter vehicle number: ");
                    String vehicleNumber = scanner.nextLine();

                    System.out.print("Enter vehicle type (CAR/BIKE): ");
                    String vehicleType = scanner.nextLine();

                    parkingService.parkVehicle(
                            vehicleNumber,
                            vehicleType
                    );
                    break;

                case 2:
                    System.out.print("Enter vehicle number: ");
                    String exitVehicleNumber = scanner.nextLine();

                    parkingService.exitVehicle(
                            exitVehicleNumber
                    );
                    break;

                case 3:
                    parkingService.viewParkingSlots();
                    break;

                case 4:
                    System.out.println();
                    System.out.println(
                            "Thank you for using Smart Parking."
                    );
                    break;

                default:
                    System.out.println();
                    System.out.println("Invalid choice.");
            }

        } while (choice != 4);

        scanner.close();
    }
}
