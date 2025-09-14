package com.assigment.userservice.services;

import com.assigment.userservice.dto.request.UserRequest;
import com.assigment.userservice.dto.response.PaginatedUserResponse;
import com.assigment.userservice.dto.response.UserStandardResponse;
import org.springframework.security.core.Authentication;

public interface UserService {
    UserStandardResponse createUser(UserRequest request);

    UserStandardResponse findAllUsersBasedOnRole(Authentication authentication, int page, int size);

    UserStandardResponse existsByID(String userID);

    UserStandardResponse findById(String userID);

    UserStandardResponse updateUser(String userID, UserRequest request);

    UserStandardResponse deleteCustomer(String userID);

    UserStandardResponse updateUserStatus(String userID, boolean enable);

    PaginatedUserResponse findAllUsersPaginated(int page, int size, String baseUrl);

}
