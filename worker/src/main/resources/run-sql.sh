#!/bin/bash

echo Running run_me_first.sql
mysql < run_me_first.sql

for script in $(ls quartz*.sql | sort -V); do
  echo Running $script
  mysql jobs < $script
done

for script in $(ls piezo*.sql | sort -V); do
  echo Running $script
  mysql jobs < $script
done
