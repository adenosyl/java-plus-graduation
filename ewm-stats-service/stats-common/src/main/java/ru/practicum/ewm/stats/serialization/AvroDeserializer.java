package ru.practicum.ewm.stats.serialization;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Deserializer;

public class AvroDeserializer<T extends SpecificRecord>
        implements Deserializer<T> {

    private final Class<T> clazz;

    public AvroDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T deserialize(String topic, byte[] data) {

        if (data == null) {
            return null;
        }

        try {

            T instance =
                    clazz.getDeclaredConstructor()
                            .newInstance();

            SpecificDatumReader<T> reader =
                    new SpecificDatumReader<>(
                            instance.getSchema()
                    );

            BinaryDecoder decoder =
                    DecoderFactory.get()
                            .binaryDecoder(data, null);

            return reader.read(
                    null,
                    decoder
            );

        } catch (Exception e) {

            throw new RuntimeException(e);
        }
    }
}
