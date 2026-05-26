-- USERS
CREATE TABLE IF NOT EXISTS users
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE
);


-- CATEGORIES
CREATE TABLE IF NOT EXISTS categories
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);


-- EVENTS
CREATE TABLE IF NOT EXISTS events
(
    id                 BIGSERIAL PRIMARY KEY,
    title              VARCHAR(120)  NOT NULL,
    annotation         VARCHAR(2000) NOT NULL,
    description        VARCHAR(7000) NOT NULL,
    event_date         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    category_id        BIGINT NOT NULL REFERENCES categories (id),
    initiator_id       BIGINT NOT NULL REFERENCES users (id),
    paid               BOOLEAN NOT NULL DEFAULT FALSE,
    participant_limit  INT     NOT NULL DEFAULT 0,
    request_moderation BOOLEAN NOT NULL DEFAULT TRUE,
    state              VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_on         TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    published_on       TIMESTAMP WITHOUT TIME ZONE,
    lat                DOUBLE PRECISION NOT NULL,
    lon                DOUBLE PRECISION NOT NULL,

    CONSTRAINT chk_participant_limit_non_negative CHECK (participant_limit >= 0),
    CONSTRAINT chk_event_state CHECK (state IN ('PENDING', 'PUBLISHED', 'CANCELED'))
);

-- COMPILATIONS
CREATE TABLE IF NOT EXISTS compilations
(
    id     BIGSERIAL PRIMARY KEY,
    title  VARCHAR(50) NOT NULL,
    pinned BOOLEAN     NOT NULL DEFAULT FALSE
);


-- COMPILATION <-> EVENTS (M:N)
CREATE TABLE IF NOT EXISTS compilation_events
(
    compilation_id BIGINT NOT NULL REFERENCES compilations (id) ON DELETE CASCADE,
    event_id       BIGINT NOT NULL REFERENCES events (id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, event_id)
);


-- PARTICIPATION REQUESTS
CREATE TABLE IF NOT EXISTS participation_requests
(
    id           BIGSERIAL PRIMARY KEY,
    created      TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW(),
    event_id     BIGINT NOT NULL REFERENCES events (id),
    requester_id BIGINT NOT NULL REFERENCES users (id),
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    CONSTRAINT uq_participation_request UNIQUE (event_id, requester_id),
    CONSTRAINT chk_request_status CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELED'))
);

-- LOCATIONS
CREATE TABLE IF NOT EXISTS locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    lat DOUBLE PRECISION NOT NULL,
    lon DOUBLE PRECISION NOT NULL,
    radius_meters INT NOT NULL CHECK (radius_meters > 0)
);
