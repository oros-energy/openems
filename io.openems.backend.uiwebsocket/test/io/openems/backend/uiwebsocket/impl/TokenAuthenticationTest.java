package io.openems.backend.uiwebsocket.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;

import io.openems.backend.authentication.api.AuthUserPasswordAuthenticationService;
import io.openems.backend.authentication.api.model.PasswordAuthenticationResult;
import io.openems.backend.common.metadata.User;
import io.openems.backend.common.test.DummyMetadata;
import io.openems.common.exceptions.OpenemsError;
import io.openems.common.jsonrpc.request.AuthenticateWithTokenRequest;
import io.openems.common.session.Language;
import io.openems.common.session.Role;

public class TokenAuthenticationTest {

	private static final Instant EXPIRES_AT = Instant.parse("2026-11-11T12:05:00Z");

	/** Maps a token to the subject and expiry of the user it belongs to. */
	private record TestToken(String subject, Instant expiresAt) {
	}

	private static final Map<String, TestToken> TOKENS = Map.of(//
			"token-a", new TestToken("sub-a", EXPIRES_AT), //
			"token-a-fresh", new TestToken("sub-a", EXPIRES_AT.plusSeconds(300)), //
			"token-b", new TestToken("sub-b", EXPIRES_AT), //
			"token-without-expiry", new TestToken("sub-a", null));

	private static class TestAuthService implements AuthUserPasswordAuthenticationService {

		@Override
		public CompletableFuture<PasswordAuthenticationResult> authenticateWithPassword(String username,
				String password) {
			return CompletableFuture.failedFuture(OpenemsError.COMMON_AUTHENTICATION_FAILED.exception());
		}

		@Override
		public CompletableFuture<PasswordAuthenticationResult> authenticateWithToken(String token) {
			final var testToken = TOKENS.get(token);
			if (testToken == null) {
				return CompletableFuture.failedFuture(OpenemsError.COMMON_AUTHENTICATION_FAILED.exception());
			}
			return CompletableFuture.completedFuture(new PasswordAuthenticationResult(testToken.subject(),
					testToken.subject() + "@example.org", token, null, testToken.expiresAt()));
		}

		@Override
		public CompletableFuture<Void> logout(String token) {
			return CompletableFuture.completedFuture(null);
		}
	}

	private static class TestMetadata extends DummyMetadata {

		@Override
		public CompletableFuture<User> getUserByExternalId(String subject) {
			if (!subject.startsWith("sub-")) {
				return CompletableFuture.failedFuture(OpenemsError.COMMON_AUTHENTICATION_FAILED.exception());
			}
			return CompletableFuture.completedFuture(new User(subject, subject + "@example.org", "Name " + subject, "",
					Language.EN, Role.GUEST, false, new JsonObject()));
		}
	}

	private static OnRequest createOnRequest() {
		final var parent = new UiWebsocketImpl();
		parent.metadata = new TestMetadata();
		parent.userAuthPasswordService = new TestAuthService();
		return new OnRequest(parent);
	}

	@Test
	public void testAuthenticateOnConnect() throws Exception {
		final var sut = createOnRequest();
		final var wsData = new WsData(null, 10);

		sut.authenticateOnConnect(wsData, "token-a").get();

		assertEquals(Optional.of("sub-a"), wsData.getUserId());
		assertEquals(Optional.of("token-a"), wsData.getToken());
		assertEquals(Optional.of(EXPIRES_AT), wsData.getUser().getTokenExpiresAt());
	}

	@Test
	public void testAuthenticateOnConnectWithRefusedToken() {
		final var sut = createOnRequest();
		final var wsData = new WsData(null, 10);

		assertThrows(ExecutionException.class, () -> sut.authenticateOnConnect(wsData, "forged").get());
		assertNull(wsData.getUser());
	}

	@Test
	public void testAuthenticateOnConnectWithUnknownUser() {
		final var parent = new UiWebsocketImpl();
		parent.metadata = new TestMetadata() {
			@Override
			public CompletableFuture<User> getUserByExternalId(String subject) {
				return CompletableFuture.failedFuture(OpenemsError.COMMON_AUTHENTICATION_FAILED.exception());
			}
		};
		parent.userAuthPasswordService = new TestAuthService();
		final var sut = new OnRequest(parent);
		final var wsData = new WsData(null, 10);

		assertThrows(ExecutionException.class, () -> sut.authenticateOnConnect(wsData, "token-a").get());
		assertNull(wsData.getUser());
	}

	@Test
	public void testAuthenticateOnConnectWithoutExpiry() {
		final var sut = createOnRequest();
		final var wsData = new WsData(null, 10);

		assertThrows(ExecutionException.class, () -> sut.authenticateOnConnect(wsData, "token-without-expiry").get());
		assertNull(wsData.getUser());
	}

	@Test
	public void testFreshTokenOfSameUserKeepsSubscriptions() throws Exception {
		final var sut = createOnRequest();
		final var wsData = new WsData(null, 10);
		sut.authenticateOnConnect(wsData, "token-a").get();
		wsData.addSubscribedEdges(Set.of("edge0", "edge1"));

		sut.handleAuthenticateWithTokenRequest(wsData, new AuthenticateWithTokenRequest("token-a-fresh")).get();

		assertEquals(Optional.of("sub-a"), wsData.getUserId());
		assertEquals(Optional.of("token-a-fresh"), wsData.getToken());
		assertEquals(Optional.of(EXPIRES_AT.plusSeconds(300)), wsData.getUser().getTokenExpiresAt());
		assertEquals(Set.of("edge0", "edge1"), wsData.getSubscribedEdges());
	}

	@Test
	public void testTokenOfAnotherUserIsRefused() throws Exception {
		final var sut = createOnRequest();
		final var wsData = new WsData(null, 10);
		sut.authenticateOnConnect(wsData, "token-a").get();
		wsData.addSubscribedEdges(Set.of("edge0"));

		assertThrows(ExecutionException.class, () -> sut
				.handleAuthenticateWithTokenRequest(wsData, new AuthenticateWithTokenRequest("token-b")).get());

		assertEquals(Optional.of("sub-a"), wsData.getUserId());
		assertEquals(Optional.of("token-a"), wsData.getToken());
		assertEquals(Optional.of(EXPIRES_AT), wsData.getUser().getTokenExpiresAt());
		assertEquals(Set.of("edge0"), wsData.getSubscribedEdges());
	}

	@Test
	public void testConnectionWithoutConnectTokenIsUnchanged() throws Exception {
		final var sut = createOnRequest();
		final var wsData = new WsData(null, 10);

		sut.handleAuthenticateWithTokenRequest(wsData, new AuthenticateWithTokenRequest("token-a")).get();
		sut.handleAuthenticateWithTokenRequest(wsData, new AuthenticateWithTokenRequest("token-b")).get();

		// Connections that did not authenticate on connect behave as before.
		assertEquals(Optional.of("sub-b"), wsData.getUserId());
		assertEquals(Optional.empty(), wsData.getUser().getTokenExpiresAt());
	}

	@Test
	public void testBearerToken() {
		assertEquals("abc.def.ghi", OnOpen.getBearerToken("Bearer abc.def.ghi"));
		assertEquals("abc.def.ghi", OnOpen.getBearerToken("bearer abc.def.ghi"));
		assertNull(OnOpen.getBearerToken(null));
		assertNull(OnOpen.getBearerToken(""));
		assertNull(OnOpen.getBearerToken("Bearer "));
		assertNull(OnOpen.getBearerToken("Basic dXNlcjpwYXNz"));
	}

}
