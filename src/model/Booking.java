package model;

import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class Booking {
    private String recordID;
    private String bookedBy; //studentID if it’s booked by student, null otherwise
    private String bookingID;
    private Map.Entry<Long, Long> timeslot;

    public Booking(String recordID, String bookedBy, Map.Entry<Long, Long> timeslot) {
        this.recordID = recordID;
        this.bookedBy = bookedBy;
        this.timeslot = timeslot;
        if (bookedBy != null) {
            this.bookingID = UUID.randomUUID().toString();
        }
    }

    public String getRecordID() {
        return recordID;
    }

    public void setRecordID(String recordID) {
        this.recordID = recordID;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(recordID, booking.recordID) &&
                Objects.equals(bookedBy, booking.bookedBy) &&
                Objects.equals(bookingID, booking.bookingID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordID, bookedBy, bookingID);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "recordID='" + recordID + '\'' +
                ", bookedBy='" + bookedBy + '\'' +
                ", bookingID='" + bookingID + '\'' +
                '}';
    }

    public Map.Entry<Long, Long> getTimeslot() {
        return timeslot;
    }

    public void setTimeslot(Map.Entry<Long, Long> timeslot) {
        this.timeslot = timeslot;
    }
}
