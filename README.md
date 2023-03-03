# terra-aws-resource-discovery

Terra AWS Support Resource Discovery Client Library

## Avro Schema Evolution
* Avro always requires two versions of schema when deserializing:
    * The Writer's Schema (the current schema version used by the writer at the time the data was written)
    * The Reader's Schema (the current schema version used by the reader of the data)
* As long as we follow the [Avro schema resolution rules](https://avro.apache.org/docs/1.10.2/spec.html#Schema+Resolution),
  we should always be able to read any schema where the Reader's schema is the same version or later than the
  Writer's schema (i.e. backward compatibility).
