package com.wise.petadoption.user.controller;

import com.wise.petadoption.security.SecurityUser;
import com.wise.petadoption.user.domain.ChangePasswordCommand;
import com.wise.petadoption.user.domain.UpdateProfileCommand;
import com.wise.petadoption.user.mapper.UserResponseMapper;
import com.wise.petadoption.user.service.UserService;
import com.wise.petadoption.user.api.ChangePasswordRequest;
import com.wise.petadoption.user.api.UpdateProfileRequest;
import com.wise.petadoption.user.api.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserResponseMapper responseMapper;

    @PatchMapping("/me")
    public UserResponse updateProfile(@AuthenticationPrincipal SecurityUser user,
                                      @Valid @RequestBody UpdateProfileRequest request
    ) {
        return responseMapper.toResponse(
                userService.updateProfile(
                        user.getUserId(),
                        toCommand(request)
                )
        );
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal SecurityUser user,
                                               @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(user.getUserId(), toCommand(request));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal SecurityUser user) {
        userService.delete(user.getUserId());
        return ResponseEntity.noContent().build();
    }

    private UpdateProfileCommand toCommand(UpdateProfileRequest request) {
        return new UpdateProfileCommand(request.email(), request.firstName(), request.lastName(), request.phone());
    }

    private ChangePasswordCommand toCommand(ChangePasswordRequest request) {
        return new ChangePasswordCommand(request.oldPassword(), request.newPassword());
    }
}
