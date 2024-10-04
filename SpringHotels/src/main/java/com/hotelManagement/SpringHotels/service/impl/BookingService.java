package com.hotelManagement.SpringHotels.service.impl;

import com.hotelManagement.SpringHotels.dto.BookingDTO;
import com.hotelManagement.SpringHotels.dto.Response;
import com.hotelManagement.SpringHotels.entity.Booking;
import com.hotelManagement.SpringHotels.entity.Room;
import com.hotelManagement.SpringHotels.entity.User;
import com.hotelManagement.SpringHotels.exception.OurException;
import com.hotelManagement.SpringHotels.repo.BookingRepository;
import com.hotelManagement.SpringHotels.repo.RoomRepository;
import com.hotelManagement.SpringHotels.repo.UserRepository;
import com.hotelManagement.SpringHotels.service.interfac.IBookingService;
import com.hotelManagement.SpringHotels.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService implements IBookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository  userRepository;

    @Override
    public Response saveBooking(Long roomId, Long userId, Booking bookingRequest) {
        Response response = new Response();
        try{
            if(bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())){
                throw new IllegalArgumentException("Check out date must come after check in date");
            }
            Room room =  roomRepository.findById(roomId).orElseThrow(()->new OurException("Room Not Found"));
            User user = userRepository.findById(userId).orElseThrow(()->new OurException("User Not Found"));

            List<Booking> existingBookings = room.getBookings();
            if(!roomIsAvailable(bookingRequest,existingBookings)){
                throw new OurException("Room not Available for Selected date range");
            }

            bookingRequest.setRoom(room);
            bookingRequest.setUser(user);
            String bookingConfirmationCode = Utils.generateRandomConfirmationCode(10);
            bookingRequest.setBookingConfirmationCode(bookingConfirmationCode);
            bookingRepository.save(bookingRequest);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBookingConfirmationCode(bookingConfirmationCode);
        }catch (OurException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error Saving a Booking "+e.getMessage());
        }
        return response;
    }



    @Override
    public Response findBookingByConfirmationCode(String confirmationCode) {
        Response response = new Response();
        try{
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode).orElseThrow(()-> new OurException("Booking Not Found"));
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRooms(booking,true);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBooking(bookingDTO);
        }catch (OurException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error finding a Booking by this confirmation code "+e.getMessage());
        }
        return response;
    }

    @Override
    public Response getAllBookings() {
        Response response = new Response();
        try{
            List<Booking> bookingList = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<BookingDTO> bookingDTOList = Utils.mapBookingListEntityToBookingListDTO(bookingList);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBookingList(bookingDTOList);
        }catch (OurException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error getting all bookings "+e.getMessage());
        }
        return response;
    }

    @Override
    public Response cancelBookings(Long bookingId) {
        Response response = new Response();
        try{
            bookingRepository.findById(bookingId).orElseThrow(()-> new OurException("Booking does not exist"));
            bookingRepository.deleteById(bookingId);
            response.setStatusCode(200);
            response.setMessage("successful");
        }catch (OurException e){
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error Cancelling a Booking "+e.getMessage());
        }
        return response;
    }

    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        // Check if the requested booking overlaps with an existing booking
                        (bookingRequest.getCheckInDate().isBefore(existingBooking.getCheckOutDate())
                                && bookingRequest.getCheckOutDate().isAfter(existingBooking.getCheckInDate())));
    }

}
