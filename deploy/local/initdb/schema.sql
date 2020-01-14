-- default schema taken from
-- https://github.com/akka/akka-persistence-jdbc/blob/v3.5.2/src/test/resources/postgres-application-with-hard-delete.conf
DROP TABLE IF EXISTS public.journal;

CREATE TABLE IF NOT EXISTS public.journal (
                                              ordering BIGSERIAL,
                                              persistence_id VARCHAR(255) NOT NULL,
                                              sequence_number BIGINT NOT NULL,
                                              deleted BOOLEAN DEFAULT FALSE,
                                              tags VARCHAR(255) DEFAULT NULL,
                                              message BYTEA NOT NULL,
                                              PRIMARY KEY(persistence_id, sequence_number)
);

CREATE UNIQUE INDEX journal_ordering_idx ON public.journal(ordering);

DROP TABLE IF EXISTS public.snapshot;

CREATE TABLE IF NOT EXISTS public.snapshot (
                                               persistence_id VARCHAR(255) NOT NULL,
                                               sequence_number BIGINT NOT NULL,
                                               created BIGINT NOT NULL,
                                               snapshot BYTEA NOT NULL,
                                               PRIMARY KEY(persistence_id, sequence_number)
);


