INSERT INTO trigger_monitoring_priority (trigger_name, trigger_group, priority) 
SELECT TRIGGER_NAME, TRIGGER_GROUP, 3 from QRTZ_TRIGGERS;

