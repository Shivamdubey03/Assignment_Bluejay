import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EmployeeAnalyzer {

    public static void main(String[] args) {

        // Define the path to the CSV file you want to analyze.
        String filePath = "D:\\Bluejay_Project\\Assignment_Timecard.csv";

        // Initialize a counter for certain conditions.
        int cnt = 0;

        // Create a HashSet to store unique employee names.
        HashSet<String> hp = new HashSet<>();
        try {

            // Read the CSV file and store its data in a list of EmployeeRecord objects.
            List<EmployeeRecord> employeeRecords = readCSVFile(filePath);

            // Make a copy of the employee records list.
            List<EmployeeRecord> copyOfEmployeeRecords = new ArrayList<>(employeeRecords);

            // Loop through each employee record in the copied list.
            for (EmployeeRecord record : copyOfEmployeeRecords) {

                // Check if the employee has worked for 7 consecutive days.
                if (hasWorkedFor7ConsecutiveDays(record, employeeRecords)) {
                    System.out.println("Employee Name: " + record.getEmployeeName());
                    hp.add(record.getEmployeeName());
                    System.out.println("Position: " + record.getPosition());
                    System.out.println("Condition: Worked for 7 consecutive days\n");
                }

                // Check if the employee has less than 10 hours between shifts.
                if (hasLessThan10HoursBetweenShifts(record, employeeRecords)) {
                    System.out.println("Employee Name: " + record.getEmployeeName());
                    System.out.println("Position: " + record.getPosition());
                    System.out.println("Condition: Less than 10 hours between shifts but more than 1 hour\n");
                    System.out.println(cnt++);
                    hp.add(record.getEmployeeName());
                }

                // Check if the employee has worked more than 14 hours in a single shift.
                if (hasWorkedMoreThan14Hours(record)) {
                    System.out.println("Employee Name: " + record.getEmployeeName());
                    System.out.println("Position: " + record.getPosition());
                    System.out.println("Condition: Worked more than 14 hours in a single shift\n");
                }
            }

            // Print the total number of unique employee names that met certain conditions.
            System.out.println(hp.size());
            for (String str : hp) {
                System.out.println(str);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Function to read data from the CSV file and convert it into EmployeeRecord
    // objects.
    private static List<EmployeeRecord> readCSVFile(String filePath) throws IOException {
        List<EmployeeRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0];
                    String position = parts[1];
                    String dateString = parts[2];

                    // Handle different date formats.
                    SimpleDateFormat[] dateFormats = {
                            new SimpleDateFormat("MM/dd/yyyy h:mm a"),
                            new SimpleDateFormat("yyyy-MM-dd")
                    };

                    Date date = null;
                    for (SimpleDateFormat dateFormat : dateFormats) {
                        try {
                            date = dateFormat.parse(dateString);
                            break;
                        } catch (ParseException e) {
                            // Continue to the next format if parsing fails.
                        }
                    }

                    // Add valid records to the list.
                    if (date != null) {
                        records.add(new EmployeeRecord(name, position, date));
                    } else {
                        System.err.println("Unable to parse date: " + dateString);
                    }
                }
            }
        }

        return records;
    }

    // Check if an employee has worked for 7 consecutive days.
    private static boolean hasWorkedFor7ConsecutiveDays(EmployeeRecord record, List<EmployeeRecord> recordList) {
        // Sort the records by date.
        recordList.sort((r1, r2) -> r1.getDate().compareTo(r2.getDate()));

        // Find the index of the current record.
        int index = recordList.indexOf(record);

        // If the record is not found, return false.
        if (index == -1) {
            return false;
        }

        // Initialize a counter for consecutive work days.
        int consecutiveWorkDays = 1;

        // Iterate through the records to check for consecutive work days.
        for (int i = index + 1; i < recordList.size(); i++) {
            Date currentDate = recordList.get(i - 1).getDate();
            Date nextDate = recordList.get(i).getDate();

            // Calculate the time difference in days.
            long diffInMillis = nextDate.getTime() - currentDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            // If the difference is 1 day, increment the counter.
            if (diffInDays == 1) {
                consecutiveWorkDays++;
                // If the employee worked for 7 consecutive days, return true.
                if (consecutiveWorkDays == 7) {
                    return true;
                }
            } else {
                // Reset the counter if there's a gap in work days.
                consecutiveWorkDays = 1;
            }
        }

        // Return false if the condition is not met.
        return false;
    }

    // Check if an employee has less than 10 hours between shifts.
    private static boolean hasLessThan10HoursBetweenShifts(EmployeeRecord record, List<EmployeeRecord> recordList) {
        // Sort the records by date.
        recordList.sort((r1, r2) -> r1.getDate().compareTo(r2.getDate()));

        // Find the index of the current record.
        int index = recordList.indexOf(record);

        // If the record is not found, return false.
        if (index == -1) {
            return false;
        }

        // Iterate through the records to check for time between shifts.
        for (int i = index + 1; i < recordList.size(); i++) {
            Date currentShiftStart = recordList.get(i - 1).getDate();
            Date nextShiftStart = recordList.get(i).getDate();

            // Calculate the time difference in hours.
            long diffInMillis = nextShiftStart.getTime() - currentShiftStart.getTime();
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

            // Check if the time difference is less than 10 hours but more than 1 hour.
            if (diffInHours < 10 && diffInHours > 1) {
                return true;
            } else if (diffInHours >= 10) {
                break; // No need to check further if the gap is more than 10 hours.
            }
        }

        // Return false if the condition is not met.
        return false;
    }

    // Check if an employee has worked more than 14 hours in a single shift.
    private static boolean hasWorkedMoreThan14Hours(EmployeeRecord record) {
        if (record != null && record.getStartTime() != null && record.getEndTime() != null) {
            long startTimeMillis = record.getStartTime().getTime();
            long endTimeMillis = record.getEndTime().getTime();
            long hoursWorked = (endTimeMillis - startTimeMillis) / (60 * 60 * 1000);

            // Check if the employee worked more than 14 hours.
            return hoursWorked > 14;
        }
        // Return false if the condition is not met.
        return false;
    }
}

// Define a class to represent employee records.
class EmployeeRecord {

    private String employeeName;
    private String position;
    private Date date;
    private Date shiftEnd;
    private Date startTime; // Add startTime field

    // Constructor to initialize employee record.
    public EmployeeRecord(String employeeName, String position, Date date) {
        this.employeeName = employeeName;
        this.position = position;
        this.date = date;
    }

    // Setter for shift end time.
    public void setShiftEnd(Date shiftEnd) {
        this.shiftEnd = shiftEnd;
    }

    // Getter for employee name.
    public String getEmployeeName() {
        return employeeName;
    }

    // Getter for position.
    public String getPosition() {
        return position;
    }

    // Getter for date.
    public Date getDate() {
        return date;
    }

    // Getter for shift end time.
    public Date getShiftEnd() {
        return shiftEnd;
    }

    // Getter for start time.
    public Date getStartTime() {
        return startTime;
    }

    // Setter for start time.
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    // Getter for end time (assuming shiftEnd represents the end time).
    public Date getEndTime() {
        return shiftEnd;
    }
}
