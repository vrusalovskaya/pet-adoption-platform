package com.wise.petadoption.user;

import java.util.Optional;

public interface UserService {
    User create(CreateUserCommand command);

    Optional<User> findByEmail(String email);
}
