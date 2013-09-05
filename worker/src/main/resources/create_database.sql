#
# One time setup for the environment
#

DROP DATABASE IF EXISTS jobs;
CREATE DATABASE jobs;
GRANT ALL PRIVILEGES ON jobs.* TO dev@'localhost' IDENTIFIED BY 'dev';
GRANT ALL PRIVILEGES ON jobs.* TO dev@'%' IDENTIFIED BY 'dev';
