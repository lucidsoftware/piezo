#
# One time setup for the environment
#

DROP DATABASE IF EXISTS jobs;
CREATE DATABASE jobs;
GRANT ALL PRIVILEGES ON jobs.* TO quartz@'localhost' IDENTIFIED BY 'quartz';
GRANT ALL PRIVILEGES ON jobs.* TO quartz@'%' IDENTIFIED BY 'quartz';
