CREATE SCHEMA IF NOT EXISTS fb_notificator;

CREATE TABLE fb_notificator.notification_template
(
    id           SERIAL            NOT NULL,
    name         CHARACTER VARYING NOT NULL,
    type         CHARACTER VARYING NOT NULL,
    skeleton     TEXT              NOT NULL,
    query_text   CHARACTER VARYING NOT NULL,
    basic_params CHARACTER VARYING,
    created_at   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP WITHOUT TIME ZONE,

    CONSTRAINT notification_template_pkey PRIMARY KEY (id)
);

CREATE TYPE fb_notificator.notification_status AS ENUM ('CREATED', 'ACTIVE', 'ARCHIVE');

CREATE TABLE fb_notificator.notification
(
    id          BIGSERIAL                          NOT NULL,
    name        CHARACTER VARYING                  NOT NULL,
    subject     CHARACTER VARYING                  NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE        NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITHOUT TIME ZONE,
    period CHARACTER VARYING NOT NULL,
    frequency   CHARACTER VARYING                  NOT NULL,
    channel     CHARACTER VARYING                  NOT NULL,
    status      fb_notificator.notification_status NOT NULL,
    template_id INT                                NOT NULL,

    CONSTRAINT notification_pkey PRIMARY KEY (id),
    CONSTRAINT notification_notification_tmpl_fkey FOREIGN KEY (template_id) REFERENCES fb_notificator.notification_template (id)
);

CREATE TYPE fb_notificator.report_status AS ENUM ('created', 'send', 'failed', 'skipped');

CREATE TABLE fb_notificator.report
(
    id              BIGSERIAL                    NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE  NOT NULL,
    notification_id BIGINT                       NOT NULL,
    result          CHARACTER VARYING            NOT NULL,
    status          fb_notificator.report_status NOT NULL,
    CONSTRAINT report_pkey PRIMARY KEY (id)
);

CREATE TYPE fb_notificator.channel_type AS ENUM ('mail');

CREATE TABLE fb_notificator.channel
(
    NAME        CHARACTER VARYING           NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    type        fb_notificator.channel_type NOT NULL,
    destination CHARACTER VARYING           NOT NULL,
    CONSTRAINT channel_pkey PRIMARY KEY (NAME)
);

CREATE TABLE fb_notificator.shedlock
(
    name       VARCHAR(64),
    lock_until TIMESTAMP(3) NULL,
    locked_at  TIMESTAMP(3) NULL,
    locked_by  VARCHAR(255),
    PRIMARY KEY (name)
)

