package io.openems.backend.authentication.api.model;

import java.time.Instant;

/**
 * Result of a successful user authentication.
 *
 * <p>
 * The refreshToken is null when the authentication provider does not issue one,
 * for example for a plain session token. The expiresAt is null when the token
 * carries no expiry that the provider knows of.
 */
public record PasswordAuthenticationResult(String userId, String login, String token, String refreshToken,
		Instant expiresAt) {

	public PasswordAuthenticationResult(String userId, String login, String token, String refreshToken) {
		this(userId, login, token, refreshToken, null);
	}

	public PasswordAuthenticationResult(String userId, String login, String token) {
		this(userId, login, token, null, null);
	}

}
