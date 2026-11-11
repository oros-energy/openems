package io.openems.backend.uiwebsocket.impl;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.handshake.ClientHandshake;
import org.slf4j.Logger;

import io.openems.common.websocket.AbstractWebsocketServer;

public class WebsocketServer extends AbstractWebsocketServer<WsData> {

	protected final UiWebsocketImpl parent;
	private final OnOpen onOpen;
	private final OnRequest onRequest;
	private final OnNotification onNotification;
	private final OnError onError;
	private final OnClose onClose;
	private final int requestLimit;

	public WebsocketServer(UiWebsocketImpl parent, String name, int port, int poolSize, int requestLimit) {
		super(name, port, poolSize);
		this.parent = parent;
		this.onRequest = new OnRequest(parent);
		this.onOpen = new OnOpen(parent, this.onRequest);
		this.onNotification = new OnNotification(parent);
		this.onError = new OnError(parent);
		this.onClose = new OnClose(parent);
		this.requestLimit = requestLimit;
	}

	@Override
	protected WsData createWsData(WebSocket ws) {
		return new WsData(ws, this.requestLimit);
	}

	@Override
	protected WsData onHandshake(WebSocket ws, Draft draft, ClientHandshake request) throws InvalidDataException {
		final var wsData = super.onHandshake(ws, draft, request);
		if (OnOpen.getBearerToken(request.getFieldValue(OnOpen.AUTHORIZATION_HEADER)) != null) {
			// Marked during the handshake, so a request that arrives before OnOpen runs
			// waits for the authentication.
			wsData.beginConnectAuthentication();
		}
		return wsData;
	}

	@Override
	protected OnOpen getOnOpen() {
		return this.onOpen;
	}

	@Override
	protected OnRequest getOnRequest() {
		return this.onRequest;
	}

	@Override
	public OnNotification getOnNotification() {
		return this.onNotification;
	}

	@Override
	protected OnError getOnError() {
		return this.onError;
	}

	@Override
	protected OnClose getOnClose() {
		return this.onClose;
	}

	@Override
	protected void logInfo(Logger log, String message) {
		this.parent.logInfo(log, message);
	}

	@Override
	protected void logWarn(Logger log, String message) {
		this.parent.logWarn(log, message);
	}

	@Override
	protected void logError(Logger log, String message) {
		this.parent.logError(log, message);
	}
}
