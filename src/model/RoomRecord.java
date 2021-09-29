package model;

import com.sun.tools.javac.util.Pair;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

public class RoomRecord {
    private String recordID;
    private LocalDate date;
    private int roomNumber; //or string?
    private ArrayList<Pair<LocalDate, LocalDate>> availableTimes;
    private String bookedBy; //studentID if it’s booked by student, null otherwise


    public RoomRecord(int roomNumber, LocalDate date, ArrayList<Pair<LocalDate, LocalDate>> availableTimes) {
        this.recordID = UUID.randomUUID().toString(); //has to be RR + 5 unique digits
        this.roomNumber = roomNumber;
        this.date = date;
        this.availableTimes = availableTimes;
    }
}
