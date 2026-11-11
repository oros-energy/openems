package io.openems.backend.common.metadata;

import java.time.Instant;
import java.util.Optional;

import com.google.gson.JsonObject;

import io.openems.common.session.AbstractUser;
import io.openems.common.session.Language;
import io.openems.common.session.Role;

/**
 * A {@link User} used by OpenEMS Backend.
 */
public class User extends AbstractUser {

	/**
	 * Keeps the login token.
	 */
	private final String token;

	/**
	 * True, if the current User can see multiple edges.
	 */
	private final boolean hasMultipleEdges;

	/**
	 * The expiry of the token, or null if the session does not end with the token.
	 */
	private final Instant tokenExpiresAt;

	public User(String id, String name, String token, Language language, Role globalRole, boolean hasMultipleEdges,
			JsonObject settings) {
		super(id, name, language, globalRole, settings);
		this.hasMultipleEdges = hasMultipleEdges;
		this.token = token;
		this.tokenExpiresAt = null;
	}

	public User(String userId, String email, String name, String token, Language language, Role globalRole,
			boolean hasMultipleEdges, JsonObject settings) {
		this(userId, email, name, token, null, language, globalRole, hasMultipleEdges, settings);
	}

	private User(String userId, String email, String name, String token, Instant tokenExpiresAt, Language language,
			Role globalRole, boolean hasMultipleEdges, JsonObject settings) {
		super(userId, email, name, language, globalRole, settings);
		this.hasMultipleEdges = hasMultipleEdges;
		this.token = token;
		this.tokenExpiresAt = tokenExpiresAt;
	}

	/**
	 * Gets the login token.
	 *
	 * @return the token
	 */
	public String getToken() {
		return this.token;
	}

	/**
	 * Creates a new User with the given token.
	 *
	 * @param token the token
	 * @return a new User
	 */
	public User withToken(String token) {
		return this.withToken(token, null);
	}

	/**
	 * Creates a new User with the given token whose session ends when the token
	 * expires.
	 *
	 * @param token          the token
	 * @param tokenExpiresAt the expiry of the token, or null if the session does
	 *                       not end with the token
	 * @return a new User
	 */
	public User withToken(String token, Instant tokenExpiresAt) {
		return new User(this.getUserId(), this.getEmail(), this.getName(), token, tokenExpiresAt, this.getLanguage(),
				this.getGlobalRole(), this.hasMultipleEdges(), this.getSettings());
	}

	/**
	 * Gets the expiry of the token.
	 *
	 * @return the expiry, or Optional.Empty if the session does not end with the
	 *         token
	 */
	public Optional<Instant> getTokenExpiresAt() {
		return Optional.ofNullable(this.tokenExpiresAt);
	}

	@Override
	public boolean hasMultipleEdges() {
		return this.hasMultipleEdges;
	}

}
