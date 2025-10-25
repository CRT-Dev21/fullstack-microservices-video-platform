package dev.crt.user.service.service;

import dev.crt.user.service.domain.User;
import dev.crt.user.service.dto.UserMapper;
import dev.crt.user.service.dto.response.CreatorDetailsResponse;
import dev.crt.user.service.dto.request.LoginRequest;
import dev.crt.user.service.dto.request.RegisterRequest;
import dev.crt.user.service.dto.response.UserResponse;
import dev.crt.user.service.exception.EmailAlreadyExistsException;
import dev.crt.user.service.exception.InvalidCredentialsException;
import dev.crt.user.service.exception.UserNotFoundException;
import dev.crt.user.service.repository.UserRepository;
import dev.crt.user.service.util.JwtUtil;
import dev.crt.user.service.util.PasswordUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public void registerUser(RegisterRequest request){

        if(userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists.");
        }

        User user = new User(
                request.username(),
                request.email(),
                PasswordUtil.hashPassword(request.password()));

        userRepository.save(user);
    }

    public String loginUser(LoginRequest request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if(PasswordUtil.matches(request.password(), user.getPassword())){
            return jwtUtil.generateToken(user);
        } else {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }

    public UserResponse getUserInfo(UUID userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        return new UserResponse(user.getUsername(), user.getAvatarUrl());
    }

    public List<CreatorDetailsResponse> getCreatorsDetails(List<UUID> ids){
        List<User> users = userRepository.findAllById(ids);

        return users.stream()
                .map(UserMapper::toCreatorDetails)
                .collect(Collectors.toList());
    }

    public void updateAvatar(UUID userId, String avatarUrl){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setAvatarUrl(avatarUrl);

        userRepository.save(user);
    }
}
