CREATE TABLE trigger_monitoring_priority(
    trigger_name VARCHAR(190) NOT NULL,
    trigger_group VARCHAR(190) NOT NULL,
    priority TINYINT DEFAULT NULL,
    created datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(trigger_group, trigger_name)
);
