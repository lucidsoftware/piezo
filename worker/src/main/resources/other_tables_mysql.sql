CREATE TABLE job_history(
    fire_instance_id VARCHAR(120),
    job_name VARCHAR(200) NOT NULL,
    job_group VARCHAR(200) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    success BOOLEAN NOT NULL,
    start DATETIME NOT NULL,
    finish DATETIME,
    PRIMARY KEY(fire_instance_id, start),
    KEY job_event(job_name(50), job_group(50), success),
    KEY finish_key(finish)
);

CREATE TABLE trigger_history(
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    scheduled_start DATETIME NOT NULL,
    actual_start DATETIME,
    finish DATETIME NOT NULL,
    misfire BOOLEAN NOT NULL,
    KEY finished_success(finish, misfire)
);