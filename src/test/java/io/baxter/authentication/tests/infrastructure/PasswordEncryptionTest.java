package io.baxter.authentication.tests.infrastructure;

import io.baxter.authentication.infrastructure.auth.PasswordEncryption;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PasswordEncryptionTest {
    private PasswordEncryption passwordEncryption;

    private final String password = "testPassword123";

    @BeforeEach
    void setUp() {
        passwordEncryption = new PasswordEncryption();
    }

    @Test
    @DisplayName("encrypt() should return a non-null, non-empty hash different from the original password")
    void encryptShouldReturnHashedPassword() {
        // Act
        String encrypted = passwordEncryption.encrypt(password);

        // Assert
        assertThat(encrypted).isNotNull();
        assertThat(encrypted).isNotEmpty();
        assertThat(encrypted).isNotEqualTo(password);
        assertThat(encrypted.startsWith("$2a$") || encrypted.startsWith("$2b$")).isTrue(); // BCrypt prefix check
    }

    @Test
    @DisplayName("verify() should return true for a correct password match")
    void verifyShouldReturnTrueForMatchingPasswords() {
        // Arrange
        String encrypted = passwordEncryption.encrypt(password);

        // Act
        boolean matches = passwordEncryption.verify(password, encrypted);

        // Assert
        assertThat(matches).isTrue();
    }

    @Test
    @DisplayName("verify() should return false for a mismatched password")
    void verifyShouldReturnFalseForMismatchedPasswords() {
        // Arrange
        String wrongPassword = "differentPassword";
        String encrypted = passwordEncryption.encrypt(password);

        // Act
        boolean matches = passwordEncryption.verify(wrongPassword, encrypted);

        // Assert
        assertThat(matches).isFalse();
    }

    @Test
    @DisplayName("encrypt() should generate different hashes for the same password due to salt")
    void encryptShouldReturnDifferentHashesForSamePassword() {
        // Act
        String encrypted1 = passwordEncryption.encrypt(password);
        String encrypted2 = passwordEncryption.encrypt(password);

        // Assert
        assertThat(encrypted1).isNotEqualTo(encrypted2);
        assertThat(passwordEncryption.verify(password, encrypted1)).isTrue();
        assertThat(passwordEncryption.verify(password, encrypted2)).isTrue();
    }
}
