package com.hotelManagement.SpringHotels.service.interfac;

import com.hotelManagement.SpringHotels.dto.LoginRequest;
import com.hotelManagement.SpringHotels.dto.Response;
import com.hotelManagement.SpringHotels.entity.User;

public interface IUserService {
    Response register( User loginRequest);
    Response login( LoginRequest loginRequest);
    Response getAllUsers();
    Response getUserBookingHistory(String userId);
    Response deleteUser( String userId);
    Response getUserById( String userId);
    Response getMyInfo( String email);


}
