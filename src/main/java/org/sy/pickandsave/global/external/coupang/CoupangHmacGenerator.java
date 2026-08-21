package org.sy.pickandsave.global.external.coupang;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class CoupangHmacGenerator {

  private static final String ALGORITHM = "HmacSHA256";
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

  @Value("${coupang.partners.access-key}")
  private String accessKey;

  @Value("${coupang.partners.secret-key}")
  private String secretKey;

  public String generateAuthHeader(String method, String path, String query) {
    String cleanAccessKey = accessKey != null ? accessKey.trim() : "";
    String cleanSecretKey = secretKey != null ? secretKey.trim() : "";

    String datetime = DATE_FORMATTER.format(Instant.now());

    // 쿠팡 HMAC 서명 원문: datetime + method + path + query ("?" 미포함)
    String message = datetime + method + path + query;

    String signature = hmacSha256(cleanSecretKey, message);

    return String.format(
        "CEA algorithm=HmacSHA256, access-key=%s, signed-date=%s, signature=%s",
        cleanAccessKey, datetime, signature
    );
  }

  private String hmacSha256(String secretKey, String message) {
    try {
      SecretKeySpec signingKey = new SecretKeySpec(
          secretKey.getBytes(StandardCharsets.UTF_8),
          ALGORITHM
      );
      Mac mac = Mac.getInstance(ALGORITHM);
      mac.init(signingKey);

      byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
      return Hex.encodeHexString(rawHmac);

    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("쿠팡 HMAC 서명 생성 실패", e);
    }
  }
}