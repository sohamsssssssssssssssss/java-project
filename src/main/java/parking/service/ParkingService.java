package parking.service;

import parking.config.DatabaseConnection;
import java.sql.*;

public class ParkingService {

    public void parkVehicle(String vehicleNumber, String vehicleType) {

        vehicleNumber = vehicleNumber.toUpperCase().trim();
        vehicleType = vehicleType.toUpperCase().trim();

        if (!vehicleType.equals("CAR") && !vehicleType.equals("BIKE")) {
            System.out.println("Invalid vehicle type. Use CAR or BIKE.");
            return;
        }

        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            String checkSql =
                    "SELECT pr.record_id FROM parking_records pr " +
                    "JOIN vehicles v ON pr.vehicle_id = v.vehicle_id " +
                    "WHERE v.vehicle_number = ? AND pr.exit_time IS NULL";

            try (PreparedStatement statement = connection.prepareStatement(checkSql)) {
                statement.setString(1, vehicleNumber);

                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Vehicle is already parked.");
                        connection.rollback();
                        return;
                    }
                }
            }

            int slotId;
            String slotNumber;

            String slotSql =
                    "SELECT slot_id, slot_number FROM parking_slots " +
                    "WHERE vehicle_type = ? AND is_occupied = FALSE " +
                    "ORDER BY slot_id LIMIT 1 FOR UPDATE";

            try (PreparedStatement statement = connection.prepareStatement(slotSql)) {
                statement.setString(1, vehicleType);

                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("No available " + vehicleType + " slots.");
                        connection.rollback();
                        return;
                    }

                    slotId = rs.getInt("slot_id");
                    slotNumber = rs.getString("slot_number");
                }
            }

            String vehicleSql =
                    "INSERT INTO vehicles (vehicle_number, vehicle_type) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE vehicle_type = VALUES(vehicle_type)";

            try (PreparedStatement statement = connection.prepareStatement(vehicleSql)) {
                statement.setString(1, vehicleNumber);
                statement.setString(2, vehicleType);
                statement.executeUpdate();
            }

            int vehicleId;

            String idSql =
                    "SELECT vehicle_id FROM vehicles WHERE vehicle_number = ?";

            try (PreparedStatement statement = connection.prepareStatement(idSql)) {
                statement.setString(1, vehicleNumber);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();
                    vehicleId = rs.getInt("vehicle_id");
                }
            }

            String occupySql =
                    "UPDATE parking_slots SET is_occupied = TRUE WHERE slot_id = ?";

            try (PreparedStatement statement = connection.prepareStatement(occupySql)) {
                statement.setInt(1, slotId);
                statement.executeUpdate();
            }

            String recordSql =
                    "INSERT INTO parking_records (vehicle_id, slot_id, entry_time) " +
                    "VALUES (?, ?, NOW())";

            try (PreparedStatement statement = connection.prepareStatement(recordSql)) {
                statement.setInt(1, vehicleId);
                statement.setInt(2, slotId);
                statement.executeUpdate();
            }

            connection.commit();

            System.out.println();
            System.out.println("Vehicle parked successfully!");
            System.out.println("Vehicle: " + vehicleNumber);
            System.out.println("Type: " + vehicleType);
            System.out.println("Assigned Slot: " + slotNumber);

        } catch (SQLException e) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }

            System.out.println("Parking failed!");
            System.out.println("Reason: " + e.getMessage());

        } finally {

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    public void exitVehicle(String vehicleNumber) {

        vehicleNumber = vehicleNumber.toUpperCase().trim();
        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            int recordId;
            int slotId;
            String slotNumber;
            String vehicleType;
            Timestamp entryTime;

            String findSql =
                    "SELECT pr.record_id, pr.slot_id, pr.entry_time, " +
                    "ps.slot_number, v.vehicle_type " +
                    "FROM parking_records pr " +
                    "JOIN vehicles v ON pr.vehicle_id = v.vehicle_id " +
                    "JOIN parking_slots ps ON pr.slot_id = ps.slot_id " +
                    "WHERE v.vehicle_number = ? AND pr.exit_time IS NULL " +
                    "LIMIT 1 FOR UPDATE";

            try (PreparedStatement statement = connection.prepareStatement(findSql)) {

                statement.setString(1, vehicleNumber);

                try (ResultSet rs = statement.executeQuery()) {

                    if (!rs.next()) {
                        System.out.println("Vehicle is not currently parked.");
                        connection.rollback();
                        return;
                    }

                    recordId = rs.getInt("record_id");
                    slotId = rs.getInt("slot_id");
                    slotNumber = rs.getString("slot_number");
                    vehicleType = rs.getString("vehicle_type");
                    entryTime = rs.getTimestamp("entry_time");
                }
            }

            long duration =
                    System.currentTimeMillis() - entryTime.getTime();

            long hours = (long) Math.ceil(
                    duration / (1000.0 * 60 * 60)
            );

            if (hours < 1) {
                hours = 1;
            }

            double rate;

            if (vehicleType.equals("CAR")) {
                rate = 50;
            } else {
                rate = 20;
            }

            double fee = hours * rate;

            String recordSql =
                    "UPDATE parking_records " +
                    "SET exit_time = NOW(), fee = ? " +
                    "WHERE record_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(recordSql)) {

                statement.setDouble(1, fee);
                statement.setInt(2, recordId);
                statement.executeUpdate();
            }

            String slotSql =
                    "UPDATE parking_slots " +
                    "SET is_occupied = FALSE " +
                    "WHERE slot_id = ?";

            try (PreparedStatement statement =
                         connection.prepareStatement(slotSql)) {

                statement.setInt(1, slotId);
                statement.executeUpdate();
            }

            connection.commit();

            System.out.println();
            System.out.println("Vehicle exited successfully!");
            System.out.println("Vehicle: " + vehicleNumber);
            System.out.println("Slot: " + slotNumber);
            System.out.println("Duration: " + hours + " hour(s)");
            System.out.println("Parking Fee: Rs. " + String.format("%.2f", fee));

        } catch (SQLException e) {

            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ignored) {
                }
            }

            System.out.println("Vehicle exit failed!");
            System.out.println("Reason: " + e.getMessage());

        } finally {

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }


    public void viewParkingSlots() {

        String sql =
                "SELECT slot_number, vehicle_type, is_occupied " +
                "FROM parking_slots ORDER BY slot_id";

        try (
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet rs = statement.executeQuery()
        ) {

            System.out.println();
            System.out.println("========================================");
            System.out.println("           PARKING SLOTS");
            System.out.println("========================================");
            System.out.printf("%-10s %-10s %-15s%n",
                    "SLOT", "TYPE", "STATUS");
            System.out.println("----------------------------------------");

            while (rs.next()) {

                String slotNumber = rs.getString("slot_number");
                String vehicleType = rs.getString("vehicle_type");
                boolean occupied = rs.getBoolean("is_occupied");

                String status;

                if (occupied) {
                    status = "OCCUPIED";
                } else {
                    status = "AVAILABLE";
                }

                System.out.printf(
                        "%-10s %-10s %-15s%n",
                        slotNumber,
                        vehicleType,
                        status
                );
            }

            System.out.println("========================================");

        } catch (SQLException e) {

            System.out.println("Could not load parking slots.");
            System.out.println("Reason: " + e.getMessage());
        }
    }

}
