#!/bin/sh
# contex.sh - Extracts the Name,Phone,Email from the Android Contact Database
#==============================================================================
# Last Change:     2011-12-29
# Name:            Android DB Contacts Exporter
#
# Description:  Extracts contacts from the Android contacts database 
#               to a CSV file for import into other contact list tools.
#
# Usage:    ./contex.sh <output_filename> &
# 
# How to use:  
#
# 1. Copy your Android Contacts Database to your computer:
#    adb pull /data/data/com.android.providers.contacts/databases/contacts2.db ./
# 2. Run this file from the same directory as contacts2.db
#     ./contex.sh output_filename.csv
#
# System Requirements:  sh, sqlite3, sed
# 
# Additional Notes:
#
#    1. This script is very slow, please be patient!
#
#    2. The sed sequence to replace the end on line (\n) with a comma (,) 
#       from a list, but avoiding \n on the last line is:
# 
#       sed ':a;N;$!ba;s/\n/,/g'
#       --- OR ---  
#       sed '{:q;N;s/\n//g;t q}'
# 
#    3. Only numbers that are of the types: HOME, MOBILE, WORK and OTHER 
#       get extracted in this script. These types are specified by the 
#    "data2" field in the "data" table.
# 
#==============================================================================
echo; echo "Android contacts backup script started with PID: $$ "; echo;
DATE=`date +%G%m%d`

#----------------------------------------------------------
# Input and Output files:
#----------------------------------------------------------
CONTACTSDB="./contacts2.db"
if [ ! -f "$CONTACTSDB" ]
then
    echo "Contacts Database file: $CONTACTSDB not found. Exiting."; echo
    exit 1
fi

OUTFILE=$1
if [ -z "$OUTFILE" ]
then
    OUTFILE="contex_backup_${DATE}.csv"
fi
echo "Contacts backup file: $OUTFILE "; echo

if [ -f "$OUTFILE" ]
then
    echo "Contacts Backup file: $OUTFILE already exist. Exiting."; echo
    exit 1
fi

#----------------------------------------------------------
# Write the Table Header:
#----------------------------------------------------------
echo "First Name,Last Name,E-mail,Mobile Phone,Home Phone,Work Phone,Other Phone" >> $OUTFILE

#----------------------------------------------------------
# Setting up SQLite command and mode:
#----------------------------------------------------------
#CMD="sqlite3 -bail -noheader -csv" 
CMD="sqlite3";

#----------------------------------------------------------
# Get the raw IDs of all contacts:
#----------------------------------------------------------
RAWID_LIST=`$CMD $CONTACTSDB "SELECT _id from raw_contacts WHERE account_type='vnd.sec.contact.phone'"`
#echo "$RAWID_LIST"

#----------------------------------------------------------
# Loop through the IDs and extract all necessary data
#----------------------------------------------------------
for RAWID in $RAWID_LIST; do

    # Get the First_Name and Last_Name in one query:
    # NOTE: Need to remove occasional CTRL-M's with sed.
    NAME=`$CMD -csv $CONTACTSDB "SELECT data2, data3 FROM data WHERE raw_contact_id=$RAWID AND mimetype_id=6;" | sed ':a;N;$!ba;s/\n//g'`

    # Get the Email address:
    # NOTE: This will simply append multiple email adresses to one long string, with ";" separator!
    EMAIL=`$CMD $CONTACTSDB "SELECT data1 FROM data WHERE raw_contact_id=$RAWID AND mimetype_id=1;" | sed ':a;N;$!ba;s/\n/;/g'`

    # Here we get the "Home", "Mobile", Work" and "Other" (labeled) phone numbers.
    # NOTE: Each of these may return multiple responses, so we need to convert new-lines (\n) to ",".
    HPHONE=`$CMD $CONTACTSDB "SELECT data1 FROM data WHERE raw_contact_id=$RAWID AND mimetype_id=5 AND data2=1;" | sed ':a;N;$!ba;s/\n/,/g'`;
    MPHONE=`$CMD $CONTACTSDB "SELECT data1 FROM data WHERE raw_contact_id=$RAWID AND mimetype_id=5 AND data2=2;" | sed ':a;N;$!ba;s/\n/,/g'`;
    WPHONE=`$CMD $CONTACTSDB "SELECT data1 FROM data WHERE raw_contact_id=$RAWID AND mimetype_id=5 AND data2=3;" | sed ':a;N;$!ba;s/\n/,/g'`;
    OPHONE=`$CMD $CONTACTSDB "SELECT data1 FROM data WHERE raw_contact_id=$RAWID AND mimetype_id=5 AND data2=7;" | sed ':a;N;$!ba;s/\n/,/g'`;
    PHONES="$MPHONE,$HPHONE,$WPHONE,$OPHONE";

    # If at least one main field is found then write the CSV line
    if [ -n "$NAME" ] || [ -n "$EMAIL" ] || [ -n "$PHONES" ]; then
        #echo "ID: $RAWID"
        echo -n "$NAME,$EMAIL,$PHONES" >> $OUTFILE
        echo >> $OUTFILE
    fi

done
echo 'Done.'