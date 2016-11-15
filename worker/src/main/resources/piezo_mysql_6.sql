ALTER TABLE job_history ADD INDEX trigger_success_key (trigger_group, trigger_name, success, start);
