package io.simplesource.kafka.serialization.avro.mappers;

import io.simplesource.api.CommandError;
import io.simplesource.data.Result;
import io.simplesource.data.Sequence;
import io.simplesource.kafka.api.AggregateSerdes;
import io.simplesource.kafka.model.*;
import io.simplesource.kafka.serialization.avro.mappers.domain.*;
import io.simplesource.kafka.serialization.json.JsonAggregateSerdes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static io.simplesource.kafka.serialization.json.JsonGenericMapper.jsonDomainMapper;
import static io.simplesource.kafka.serialization.json.JsonOptionalGenericMapper.jsonOptionalDomainMapper;

public class JsonAggregateSerdeTests {
    private static final String topic = "topic";
    private AggregateSerdes<UserAccountDomainKey, UserAccountDomainCommand, UserAccountDomainEvent, Optional<UserAccountDomain>> serdes;

    @BeforeEach
    void setup() {
        serdes = new JsonAggregateSerdes<>(
                jsonDomainMapper(),
                jsonDomainMapper(),
                jsonDomainMapper(),
                jsonOptionalDomainMapper());
    }

    @Test
    void aggregateKey() {
        UserAccountDomainKey aggKey = new UserAccountDomainKey("userId");
        byte[] serialised = serdes.aggregateKey().serializer().serialize(topic, aggKey);
        UserAccountDomainKey deserialised = serdes.aggregateKey().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualToComparingFieldByField(aggKey);
    }

    @Test
    void uuidResponseKey() {
        UUID responseKey = UUID.randomUUID();

        byte[] serialised = serdes.commandResponseKey().serializer().serialize(topic, responseKey);
        UUID deserialised = serdes.commandResponseKey().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualTo(responseKey);
    }

    @Test
    void aggregateUpdate() {
        AggregateUpdate<Optional<UserAccountDomain>> update = new AggregateUpdate<>(
                Optional.of(new UserAccountDomain("Name", Money.valueOf("100"))),
                Sequence.first());

        byte[] serialised = serdes.aggregateUpdate().serializer().serialize(topic, update);
        AggregateUpdate<Optional<UserAccountDomain>> deserialised = serdes.aggregateUpdate().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualToComparingFieldByField(update);
    }

    @Test
    void commandRequest() {
        UserAccountDomainKey aggKey = new UserAccountDomainKey("userId");

        CommandRequest<UserAccountDomainKey, UserAccountDomainCommand> commandRequest = new CommandRequest<>(
                aggKey,
                new UserAccountDomainCommand.UpdateUserName("name"),
                Sequence.first(),
                UUID.randomUUID());

        byte[] serialised = serdes.commandRequest().serializer().serialize(topic, commandRequest);
        CommandRequest<UserAccountDomainKey, UserAccountDomainCommand> deserialised = serdes.commandRequest().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualToComparingFieldByField(commandRequest);
    }

    @Test
    void updateResultSuccess() {
        AggregateUpdateResult<Optional<UserAccountDomain>> update = new AggregateUpdateResult<>(
                UUID.randomUUID(),
                Sequence.first(),
                Result.success(new AggregateUpdate<>(
                        Optional.of(new UserAccountDomain("Name", Money.valueOf("100"))),
                        Sequence.first())));

        byte[] serialised = serdes.updateResult().serializer().serialize(topic, update);
        AggregateUpdateResult<Optional<UserAccountDomain>> deserialised = serdes.updateResult().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualToComparingFieldByField(update);
    }

    @Test
    void updateResultFailure() {
        AggregateUpdateResult<Optional<UserAccountDomain>> update = new AggregateUpdateResult<>(
                UUID.randomUUID(),
                Sequence.first(),
                Result.failure(
                        CommandError.of(CommandError.Reason.InvalidCommand, "Invalid Command"),
                        CommandError.of(CommandError.Reason.InvalidReadSequence, "Invalid Sequence")));

        byte[] serialised = serdes.updateResult().serializer().serialize(topic, update);
        AggregateUpdateResult<Optional<UserAccountDomain>> deserialised = serdes.updateResult().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualToComparingFieldByField(update);
    }

    @Test
    void eventWithSequence() {
        ValueWithSequence<UserAccountDomainEvent> eventSeq = new ValueWithSequence<>(
                new UserAccountDomainEvent.AccountCreated("name", Money.valueOf("100")),
                Sequence.first()                );

        byte[] serialised = serdes.valueWithSequence().serializer().serialize(topic, eventSeq);
        ValueWithSequence<UserAccountDomainEvent> deserialised = serdes.valueWithSequence().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualToComparingFieldByField(eventSeq);
    }

    @Test
    void commandResponseSuccess() {
        CommandResponse commandResponse = new CommandResponse(
                UUID.randomUUID(),
                Sequence.first(),
                Result.success(Sequence.first()));

        byte[] serialised = serdes.commandResponse().serializer().serialize(topic, commandResponse);
        CommandResponse deserialised = serdes.commandResponse().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualToComparingFieldByField(commandResponse);
    }

    @Test
    void commandResponseFailure() {
        CommandResponse commandResponse = new CommandResponse(
                UUID.randomUUID(),
                Sequence.first(),
                Result.failure(CommandError.of(CommandError.Reason.InvalidReadSequence, "Invalid sequence")));

        byte[] serialised = serdes.commandResponse().serializer().serialize(topic, commandResponse);
        CommandResponse deserialised = serdes.commandResponse().deserializer().deserialize(topic, serialised);
        assertThat(deserialised).isEqualToComparingFieldByField(commandResponse);
    }
}