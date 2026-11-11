package io.openems.backend.uiwebsocket.impl;

import org.java_websocket.WebSocket;
import org.java_websocket.framing.CloseFrame;
import org.java_websocket.handshake.Handshakedata;
import org.slf4j.Logger;

import io.openems.backend.common.component.AbstractOpenemsBackendComponent;
import io.openems.common.exceptions.OpenemsError;

/**
 * Authenticates a connection whose handshake carries an "Authorization: Bearer"
 * header. Connections without that header are left as they are.
 */
public class OnOpen implements io.openems.common.websocket.OnOpen {

	/* package */ static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final Logger log;
	private final OnRequest onRequest;

	public OnOpen(UiWebsocketImpl parent, OnRequest onRequest) {
		this.log = AbstractOpenemsBackendComponent.getComponentLogger(this.getClass(), parent);
		this.onRequest = onRequest;
	}

	@Override
	public OpenemsError apply(WebSocket ws, Handshakedata handshakedata) {
		final var token = getBearerToken(handshakedata.getFieldValue(AUTHORIZATION_HEADER));
		if (token == null) {
			return null;
		}
		final WsData wsData = ws.getAttachment();
		this.onRequest.authenticateOnConnect(wsData, token).whenComplete((ignore, throwable) -> {
			if (throwable != null) {
				this.log.info("Refused the token of connection [{}] on connect: {}", wsData.getId(),
						throwable.getMessage());
				ws.close(CloseFrame.POLICY_VALIDATION, "Authentication failed");
			}
			wsData.completeConnectAuthentication();
		});
		return null;
	}

	/**
	 * Gets the token of an "Authorization" header with the "Bearer" scheme.
	 *
	 * @param header the value of the header, may be null
	 * @return the token, or null if the header carries no bearer token
	 */
	/* package */ static String getBearerToken(String header) {
		if (header == null || !header.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			return null;
		}
		final var token = header.substring(BEARER_PREFIX.length()).trim();
		return token.isEmpty() ? null : token;
	}

}
