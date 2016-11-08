#
# One time setup for the environment.
# Must be run by a DB user with the necessary permissions.
#

DROP DATABASE IF EXISTS jobs;
CREATE DATABASE jobs;
GRANT ALL PRIVILEGES ON jobs.* TO dev@'localhost' IDENTIFIED BY 'dev';
GRANT ALL PRIVILEGES ON jobs.* TO dev@'%' IDENTIFIED BY 'dev';
