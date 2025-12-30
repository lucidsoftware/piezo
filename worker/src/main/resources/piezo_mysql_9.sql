ALTER TABLE job_history
  DROP INDEX start_key,
  ADD INDEX start_key (start, trigger_group);
