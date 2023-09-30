import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EmployeeAnalyzer {

    public static void main(String[] args) {
        // Step 1: Take the file as input
        String filePath = "D:\\Bluejay_Project\\Assignment_Timecard.csv";
        int cnt = 0;

        HashSet<String> hp = new HashSet<>();
        try {

            // Step 2: Read the CSV file
List<EmployeeRecord> employeeRecords = readCSVFile(filePath);

// Make a copy of the employeeRecords list before iterating over it
List<EmployeeRecord> copyOfEmployeeRecords = new ArrayList<>(employeeRecords);

// Step 3: Iterate over records and apply conditions
for (EmployeeRecord record : copyOfEmployeeRecords) {
  if (hasWorkedFor7ConsecutiveDays(record, employeeRecords)) {
    System.out.println("Employee Name: " + record.getEmployeeName());
    hp.add(record.getEmployeeName());
    System.out.println("Position: " + record.getPosition());
    System.out.println("Condition: Worked for 7 consecutive days\n");
  }

  if (hasLessThan10HoursBetweenShifts(record, employeeRecords)) {
    System.out.println("Employee Name: " + record.getEmployeeName());
    System.out.println("Position: " + record.getPosition());
    System.out.println("Condition: Less than 10 hours between shifts but more than 1 hour\n");
    System.out.println(cnt++);
     hp.add(record.getEmployeeName());
  }
   
  if (hasWorkedMoreThan14Hours(record)) {
    System.out.println("Employee Name: " + record.getEmployeeName());
    System.out.println("Position: " + record.getPosition());
    System.out.println("Condition: Worked more than 14 hours in a single shift\n");
  
  }
   
}

System.out.println(hp.size());
for(String str : hp)
{
     System.out.println(str);
}
      
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper method to read the CSV file and parse records
    private static List<EmployeeRecord> readCSVFile(String filePath) throws IOException {
        List<EmployeeRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) { // Ensure at least 3 columns (name, position, date)
                    String name = parts[0];
                    String position = parts[1];
                    String dateString = parts[2];
                    
                    SimpleDateFormat[] dateFormats = {
                        new SimpleDateFormat("MM/dd/yyyy h:mm a"),
                        new SimpleDateFormat("yyyy-MM-dd")
                    };
        
                    Date date = null;
                    for (SimpleDateFormat dateFormat : dateFormats) {
                        try {
                            date = dateFormat.parse(dateString);
                            break; // If parsing succeeds, exit the loop
                        } catch (ParseException e) {
                            // Parsing failed with this format, try the next one
                        }
                    }
        
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

    // Helper methods to check conditions
    private static boolean hasWorkedFor7ConsecutiveDays(EmployeeRecord record, List<EmployeeRecord> recordList) {
        // Sort the records by date (assuming they are not already sorted)
        recordList.sort((r1, r2) -> r1.getDate().compareTo(r2.getDate()));

        // Find the index of the provided record in the sorted list
        int index = recordList.indexOf(record);

        if (index == -1) {
            // The provided record is not in the list, so return false
            return false;
        }

        // Iterate forward and count consecutive workdays
        int consecutiveWorkDays = 1; // Start with 1 as the first day counts

        for (int i = index + 1; i < recordList.size(); i++) {
            Date currentDate = recordList.get(i - 1).getDate();
            Date nextDate = recordList.get(i).getDate();

            long diffInMillis = nextDate.getTime() - currentDate.getTime();
            long diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis);

            if (diffInDays == 1) {
                consecutiveWorkDays++;
                if (consecutiveWorkDays == 7) {
                    return true; // Employee has worked for 7 consecutive days
                }
            } else {
                consecutiveWorkDays = 1; // Reset consecutive work days count
            }
        }

        return false;
    }

    private static boolean hasLessThan10HoursBetweenShifts(EmployeeRecord record, List<EmployeeRecord> recordList) {
        // Sort the records by date (assuming they are not already sorted)
        recordList.sort((r1, r2) -> r1.getDate().compareTo(r2.getDate()));

        // Find the index of the provided record in the sorted list
        int index = recordList.indexOf(record);

        if (index == -1) {
            // The provided record is not in the list, so return false
            return false;
        }

        // Iterate forward and check the time difference between shifts
        for (int i = index + 1; i < recordList.size(); i++) {
            Date currentShiftStart = recordList.get(i - 1).getDate();
            Date nextShiftStart = recordList.get(i).getDate();

            long diffInMillis = nextShiftStart.getTime() - currentShiftStart.getTime();
            long diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis);

            if (diffInHours < 10 && diffInHours > 1) {
                return true; // Employee has less than 10 hours between shifts but more than 1 hour
            } else if (diffInHours >= 10) {
                break; // No need to check further as the shifts are more than 10 hours apart
            }
        }

        return false;
    }

    private static boolean hasWorkedMoreThan14Hours(EmployeeRecord record) {
        if (record != null && record.getStartTime() != null && record.getEndTime() != null) {
            long startTimeMillis = record.getStartTime().getTime();
            long endTimeMillis = record.getEndTime().getTime();
            long hoursWorked = (endTimeMillis - startTimeMillis) / (60 * 60 * 1000); // Calculate hours worked

            return hoursWorked > 14;
        }
        return false; // Handle the case when any of the Date objects are null
    }
}

 class EmployeeRecord {

    private String employeeName;
    private String position;
    private Date date;
    private Date shiftEnd;
    private Date startTime; // Add startTime field

    public EmployeeRecord(String employeeName, String position, Date date) {
        this.employeeName = employeeName;
        this.position = position;
        this.date = date;
    }

    public void setShiftEnd(Date shiftEnd) {
        this.shiftEnd = shiftEnd;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public String getPosition() {
        return position;
    }

    public Date getDate() {
        return date;
    }

    public Date getShiftEnd() {
        return shiftEnd;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return shiftEnd; // Assuming shiftEnd represents the end time
    }
}
