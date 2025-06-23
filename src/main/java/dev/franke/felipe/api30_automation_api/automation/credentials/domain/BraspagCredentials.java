package dev.franke.felipe.api30_automation_api.automation.credentials.domain;

import dev.franke.felipe.api30_automation_api.automation.credentials.exception.InvalidPasswordException;
import dev.franke.felipe.api30_automation_api.automation.credentials.exception.InvalidUsernameException;

public record BraspagCredentials(String username, String password) implements CredentialsContract {

    public BraspagCredentials(String username, String password) {
        this.username = username;
        this.password = password;
        validate();
    }

    @Override
    public void validate() {
        checkUsername();
        checkPassword();
    }

    private boolean usernameIsNullOrBlank() {
        return username == null || username.isBlank();
    }

    private boolean passwordIsNullOrBlank() {
        return password == null || password.isBlank();
    }

    private void checkUsername() {
        if (usernameIsNullOrBlank()) {
            throw new InvalidUsernameException("Username is blank or null");
        }
    }

    private void checkPassword() {
        if (passwordIsNullOrBlank()) {
            throw new InvalidPasswordException("Password is blank or null");
        }
    }
}
