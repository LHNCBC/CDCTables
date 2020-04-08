#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 19-Nov-2019
@author: lee peters 
"""
# Get the housekeeping information from the MariaDB **** database 
#

import sys

import mysql.connector as mariadb

# process command line arguments
# if specified, argument is the term 
#arglen = len(sys.argv)
#if arglen > 1:
#    term = sys.argv[1]

cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()

cursor.execute( "SELECT Name from NLMDrugAuthoritativeSource",)
records = cursor.fetchall()
print ("Sources")
for row in records:
    name = row[0]
    print ("  ", name)

cursor.execute( "SELECT Abbreviation,Description from NLMDrugTermType",)
print ("\nTerm Types:")
for (Abbreviation,Description) in cursor:
    print("  {0:<5} {1:<20}".format(Abbreviation,Description))

cursor.execute("SELECT distinct relation from NLMDrugTermTerm",)
records = cursor.fetchall()

print ("\nRelations:")
for row in records:
    relation = row[0]
    print("  ", relation)

cursor.close()
cnx.close()
