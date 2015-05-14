CREATE TABLE job_history(
    fire_instance_id VARCHAR(120),
    job_name VARCHAR(200) NOT NULL,
    job_group VARCHAR(200) NOT NULL,
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    success BOOLEAN NOT NULL,
    start DATETIME NOT NULL,
    finish DATETIME,
    PRIMARY KEY(fire_instance_id),
    KEY job_key(job_group, job_name),
    KEY start_key(start)
);

CREATE TABLE trigger_history(
    trigger_name VARCHAR(200) NOT NULL,
    trigger_group VARCHAR(200) NOT NULL,
    scheduled_start DATETIME NOT NULL,
    actual_start DATETIME,
    finish DATETIME NOT NULL,
    misfire BOOLEAN NOT NULL,
    fire_instance_id VARCHAR(120) NULL,
    PRIMARY KEY(trigger_group, trigger_name, scheduled_start, fire_instance_id),
    KEY sched_start_key(scheduled_start)
);