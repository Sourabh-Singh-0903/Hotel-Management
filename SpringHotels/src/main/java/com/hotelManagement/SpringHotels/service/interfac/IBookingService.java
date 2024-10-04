package com.hotelManagement.SpringHotels.service.interfac;

import com.hotelManagement.SpringHotels.dto.Response;
import com.hotelManagement.SpringHotels.entity.Booking;

public interface IBookingService {
    Response saveBooking(Long roomId, Long userId, Booking bookingRequest);
    Response findBookingByConfirmationCode(String confirmationCode);
    Response getAllBookings();
    Response cancelBookings(Long bookingId);


}
