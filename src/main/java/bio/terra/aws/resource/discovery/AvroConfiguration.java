package bio.terra.aws.resource.discovery;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * The {@link AvroConfiguration} class parses Support Resource configuration records.
 *
 * <p>In order to perform discovery of Support Resources in a Terra AWS Environment, an
 * Avro-serialized configuration describing these resources is written to a well known location
 * (accessible S3 bucket or local filesystem directory).
 *
 * <p>Avro requires both the "writer schema" (the version of the schema that the Avro data was
 * written with) and the "reader schema" (the version in src/main/avro, which is the latest version
 * and used for Java code generation) in order to parse Avro-encoded data.
 *
 * <p>For any given configuration description (Environment or Landing Zone), we write a file (or S3
 * object) that JSON-encodes both the data and the schema used to generate that data. This is a
 * simple JSON record containing two fields:
 *
 * <ul>
 *   <li>Field "schema" contains a base64-encoded string describing the Avro schema in JSON
 *   <li>Field "payload" contains a base64-encoded string containing the Avro configuration data in
 *       JSON
 * </ul>
 *
 * <p>The responsibility of record AvroConfiguration is to deserialize this JSON record and perform
 * base64 decoding of both the schema and payload.
 *
 * @param encodedSchema base64-encoded Avro schema used to write configuration data
 * @param encodedPayload base64-encoded Avro data written with the associated schema
 */
record AvroConfiguration(
    @JsonProperty("schema") String encodedSchema, @JsonProperty("payload") String encodedPayload) {

  private static String decode(String base64Encoded) {
    byte[] bytes = Base64.getDecoder().decode(base64Encoded);
    return new String(bytes, StandardCharsets.UTF_8);
  }

  /**
   * @return base64 Avro schema JSON decoded to UTF-8 and usable with the Avro SDK
   */
  public String schema() {
    return decode(encodedSchema());
  }

  /**
   * @return base64 Avro configuration data JSON decoded to UTF-8 and usable with the Avro SDK
   */
  public String payload() {
    return decode(encodedPayload());
  }
}
