package io.openems.backend.uiwebsocket.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.openems.backend.common.test.DummyUser;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;

public class WsDataTest {

	private static final String TOKEN = "token";
	private static final String REFRESH_TOKEN = "refreshToken";

	@Test
	public void test() throws OpenemsNamedException {
		var sut = new WsData(null, 10);
		assertEquals(Optional.empty(), sut.getUser(null));
		assertEquals(Optional.empty(), sut.getRefreshToken());
		assertThrows(OpenemsNamedException.class, () -> sut.assertToken());
		assertEquals("UiWebsocket.WsData [userId=UNKNOWN]", sut.toLogString());

		sut.setToken(TOKEN);
		sut.setRefreshToken(REFRESH_TOKEN);
		sut.setUser(DummyUser.DUMMY_GUEST);

		assertEquals(Optional.of("guest"), sut.getUserId());
		assertEquals(Optional.of(TOKEN), sut.getToken());
		assertEquals(Optional.of(REFRESH_TOKEN), sut.getRefreshToken());
		assertEquals(TOKEN, sut.assertToken());
		assertEquals("UiWebsocket.WsData [userId=guest]", sut.toLogString());

		sut.logout();

		assertEquals(Optional.empty(), sut.getToken());
		assertEquals(Optional.empty(), sut.getRefreshToken());
		assertNull(sut.getUser());
	}

	@Test
	public void testTokenExpiry() {
		final var now = Instant.parse("2026-11-11T12:00:00Z");
		var sut = new WsData(null, 10);
		assertFalse(sut.isTokenExpired(now));

		sut.setUser(DummyUser.DUMMY_GUEST.withToken(TOKEN));
		assertFalse(sut.isTokenExpired(now.plusSeconds(3600)));

		sut.setUser(DummyUser.DUMMY_GUEST.withToken(TOKEN, now.plusSeconds(1)));
		assertFalse(sut.isTokenExpired(now));
		assertTrue(sut.isTokenExpired(now.plusSeconds(1)));

		sut.logout();
		assertFalse(sut.isTokenExpired(now.plusSeconds(1)));
	}

	@Test
	public void testConnectAuthentication() {
		var sut = new WsData(null, 10);
		assertTrue(sut.getConnectAuthentication().isDone());

		sut.beginConnectAuthentication();
		assertFalse(sut.getConnectAuthentication().isDone());

		sut.completeConnectAuthentication();
		assertTrue(sut.getConnectAuthentication().isDone());
	}

}
