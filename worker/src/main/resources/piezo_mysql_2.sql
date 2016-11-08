
ALTER TABLE `job_history`
  MODIFY COLUMN trigger_name VARCHAR(100) NOT NULL,
  MODIFY COLUMN trigger_group VARCHAR(100) NOT NULL,
  MODIFY COLUMN job_name VARCHAR(100) NOT NULL,
  MODIFY COLUMN job_group VARCHAR(100) NOT NULL,
  DROP KEY job_key,
  ADD KEY job_key (job_group, job_name, start);

ALTER TABLE `trigger_history`
  MODIFY COLUMN trigger_name VARCHAR(100) NOT NULL,
  MODIFY COLUMN trigger_group VARCHAR(100) NOT NULL;
