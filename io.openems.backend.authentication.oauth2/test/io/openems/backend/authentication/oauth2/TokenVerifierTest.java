package io.openems.backend.authentication.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.RSAKeyProvider;

public class TokenVerifierTest {

	private static final String ISSUER = "https://auth.example.org/realms/test";
	private static final String AUDIENCE = "backend";
	private static final Instant NOW = Instant.parse("2026-11-11T12:00:00Z");
	private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

	private static KeyPair keyA;
	private static KeyPair keyB;

	@BeforeAll
	public static void createKeys() throws Exception {
		final var generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(2048);
		keyA = generator.generateKeyPair();
		keyB = generator.generateKeyPair();
	}

	private static RSAKeyProvider keyProvider(Map<String, KeyPair> keys) {
		return new RSAKeyProvider() {
			@Override
			public RSAPublicKey getPublicKeyById(String keyId) {
				final var keyPair = keys.get(keyId);
				return keyPair == null ? null : (RSAPublicKey) keyPair.getPublic();
			}

			@Override
			public RSAPrivateKey getPrivateKey() {
				return null;
			}

			@Override
			public String getPrivateKeyId() {
				return null;
			}
		};
	}

	private static JWTVerifier verifier(String audience) {
		return OAuthUserAuthenticationServiceImpl.createVerifier(keyProvider(Map.of("a", keyA)), ISSUER, audience,
				CLOCK);
	}

	private static JWTCreator.Builder token() {
		return JWT.create() //
				.withKeyId("a") //
				.withIssuer(ISSUER) //
				.withAudience("odoo", AUDIENCE) //
				.withSubject("a1b2c3") //
				.withNotBefore(NOW.minusSeconds(60)) //
				.withExpiresAt(NOW.plusSeconds(300));
	}

	private static String sign(JWTCreator.Builder builder, KeyPair keyPair) {
		return builder.sign(Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), //
				(RSAPrivateKey) keyPair.getPrivate()));
	}

	@Test
	public void testValidToken() {
		final var decoded = verifier(AUDIENCE).verify(sign(token(), keyA));

		assertEquals("a1b2c3", decoded.getSubject());
		assertEquals(NOW.plusSeconds(300), decoded.getExpiresAtAsInstant());
	}

	@Test
	public void testExpiredToken() {
		final var token = sign(token().withExpiresAt(NOW.minusSeconds(1)), keyA);

		assertThrows(JWTVerificationException.class, () -> verifier(AUDIENCE).verify(token));
	}

	@Test
	public void testNotYetValidToken() {
		final var token = sign(token().withNotBefore(NOW.plusSeconds(1)), keyA);

		assertThrows(JWTVerificationException.class, () -> verifier(AUDIENCE).verify(token));
	}

	@Test
	public void testWrongAudience() {
		final var token = sign(token().withAudience("odoo"), keyA);

		assertThrows(JWTVerificationException.class, () -> verifier(AUDIENCE).verify(token));
	}

	@Test
	public void testAnyAudienceWithoutConfiguredAudience() {
		final var token = sign(token().withAudience("odoo"), keyA);

		verifier("").verify(token);
	}

	@Test
	public void testWrongIssuer() {
		final var token = sign(token().withIssuer("https://auth.example.org/realms/other"), keyA);

		assertThrows(JWTVerificationException.class, () -> verifier(AUDIENCE).verify(token));
	}

	@Test
	public void testBadSignature() {
		// The key id "a" is known, but the token is signed with key B.
		final var token = sign(token(), keyB);

		assertThrows(JWTVerificationException.class, () -> verifier(AUDIENCE).verify(token));
	}

	@Test
	public void testUnknownKeyId() {
		final var token = sign(token().withKeyId("b"), keyB);

		assertThrows(JWTVerificationException.class, () -> verifier(AUDIENCE).verify(token));
	}

	@Test
	public void testOtherAlgorithm() {
		final var token = token().sign(Algorithm.HMAC256("secret"));

		assertThrows(JWTVerificationException.class, () -> verifier(AUDIENCE).verify(token));
	}

}
