CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE transfers (
    id                              uuid                DEFAULT uuid_generate_v4()  NOT NULL,
    sender_id                       uuid                                            NOT NULL,
    sender_account_id               uuid                                            NOT NULL,
    recipient_id                    uuid                                            NOT NULL,
    recipient_account_id            uuid                                            NOT NULL,
    amount                          numeric(1000, 2)                                NOT NULL,
    bacen_notified                  boolean             DEFAULT false                       ,
    created_at                      timestamptz                                     NOT NULL
);

ALTER TABLE ONLY transfers
    ADD CONSTRAINT transfer_pkey PRIMARY KEY (id);