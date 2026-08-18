CREATE TABLE anonymous_users (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE moods (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES anonymous_users(id),
    energy VARCHAR(32) NOT NULL,
    note VARCHAR(2000),
    recorded_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_moods_owner_date ON moods(owner_id, recorded_date);

CREATE TABLE mood_emotions (
    mood_id UUID NOT NULL REFERENCES moods(id) ON DELETE CASCADE,
    emotion VARCHAR(32) NOT NULL,
    PRIMARY KEY (mood_id, emotion)
);

CREATE TABLE action_results (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES anonymous_users(id),
    session_id UUID NOT NULL,
    mood_id UUID NOT NULL REFERENCES moods(id),
    action_id VARCHAR(64) NOT NULL,
    change VARCHAR(32),
    detail_note VARCHAR(2000),
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_action_result_owner_session UNIQUE(owner_id, session_id)
);

CREATE TABLE rewards (
    id UUID PRIMARY KEY,
    result_id UUID NOT NULL UNIQUE REFERENCES action_results(id),
    experience INTEGER NOT NULL,
    fragments INTEGER NOT NULL,
    applied_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE pet_progress (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES anonymous_users(id),
    pet_key VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    level INTEGER NOT NULL,
    experience INTEGER NOT NULL,
    fragments INTEGER NOT NULL,
    required_fragments INTEGER NOT NULL,
    unlocked BOOLEAN NOT NULL,
    primary_pet BOOLEAN NOT NULL,
    CONSTRAINT uq_pet_owner_key UNIQUE(owner_id, pet_key),
    CONSTRAINT ck_pet_values CHECK(level >= 0 AND experience >= 0 AND fragments >= 0 AND required_fragments > 0)
);
