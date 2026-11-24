package cl.duoc.huertohogar.service;

import cl.duoc.huertohogar.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.List;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final NimbusJwtDecoder refreshJwtDecoder;
    private final String issuer;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtTokenService(JwtEncoder jwtEncoder,
                           SecretKey secretKey,
                           @Value("${app.jwt.issuer:huertohogar-api}") String issuer,
                           @Value("${app.jwt.access-expiration-seconds:900}") long accessExpirationSeconds,
                           @Value("${app.jwt.refresh-expiration-seconds:86400}") long refreshExpirationSeconds) {
        this.jwtEncoder = jwtEncoder;
        this.issuer = issuer;
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;

        this.refreshJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        this.refreshJwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(issuer));
    }

    public String generateAccessToken(Usuario usuario) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(usuario.getCorreo())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessExpirationSeconds))
                .claim("uid", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("roles", List.of(usuario.getRol().name()))
                .claim("token_type", "access")
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String generateRefreshToken(Usuario usuario) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(usuario.getCorreo())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshExpirationSeconds))
                .claim("uid", usuario.getId())
                .claim("token_type", "refresh")
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public Jwt decodeRefreshToken(String refreshToken) {
        try {
            Jwt jwt = refreshJwtDecoder.decode(refreshToken);
            if (!"refresh".equals(jwt.getClaimAsString("token_type"))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "El token enviado no es un refreshToken valido");
            }
            return jwt;
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (JwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token invalido o expirado");
        }
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }
}
