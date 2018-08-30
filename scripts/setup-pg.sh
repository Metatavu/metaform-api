#!/bin/sh

sudo -u postgres createuser -R -S mftest
sudo -u postgres createdb -Omftest -Ttemplate0 mftest
sudo -u postgres psql -c "alter user mftest with password 'mftest'"
sudo -u postgres psql -c "alter role mftest with login"