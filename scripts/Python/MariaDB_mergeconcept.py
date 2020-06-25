#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 2-Jun-2020
@author: lee peters 
"""
# Merge one concept into a second concept in the MariaDB opioid database 
#
# Input:  a file with merge records
#     each record contains the following fields (delimited by "|")
#         principle variant name of concept to merge
#         principle variant source of concept to merge
#         principle varint name of concept to be merged into
#         principle variant source of concept to be merged into
# 
#   example: (to merge NFLIS concept 5-MAPB into NFLIS concept 5-MAPDB)
#            5-MAPB|NFLIS|5-MAPDB|NFLIS
#
# Processing: 
#     A number of database queries are performed
#	1) get the source names from the NLMDrugAuthoritativeSource table (for checking input)
#       2) find concepts 1 and 2 from the NLMDrugTerm and NLMDrugConcept tables based on the input
#       3) update the NLMDrugTerm table term records for concept 1 to replace concept id with concept 2
#       4) delete the NLMDrugConcept concept record for concept 1
#	5) check the NLMDrugConcepttoConcept table for records containing concept 1
#       6) update the NLMDrugConcepttoConcept table for records containing concept 1

import sys

import mysql.connector as mariadb
from datetime import datetime

# flag indicating whether to actually perform database updates (1=no updates)
checkonly = 0

# process command line arguments
# the first argument is a file containing concepts to merge
# the second argument is optional; if specified, it will only check the data but not do any database updates
# 
arglen = len(sys.argv)
if arglen < 2:
    print("FORMAT:   python {} mergefile [check]".format(sys.argv[0]))
    print("   where: ")
    print("       mergefile - file of concepts to be merged")
    print("           format ('|' delimited): prefname1|src1|prefname2|src2")
    print("           example:  5-MAPB|NFLIS|5-MAPDB|NFLIS")
    print("       check - (optional) performs check, doesn't do the merge")
    sys.exit()
if arglen > 2:
    checkonly = 1

# get the date and time
dt = datetime.now()
formatted_date = dt.strftime('%Y-%m-%d %H:%M:%S')

mergefile = sys.argv[1]
# read in sources in database to check user input
cnx = mariadb.connect(user='root', password='Li.va.rot', database='opioid')
cursor = cnx.cursor()
sources = []
query = "select name from NLMDrugAuthoritativeSource"
cursor.execute(query)
sourceRecords = cursor.fetchall()
for row in sourceRecords:
    sources.append(row[0].lower())

# read in file
f = open(mergefile, 'r')
lines = f.readlines()
for line in lines:
    line2 = line.rstrip()
    fields = line2.split("|");
    prefname1 = fields[0]
    prefsource1 = fields[1]
    if prefsource1.lower() not in sources:
        print("NOT MERGED - invalid source1 - {}".format(line2))
        continue
    prefname2 = fields[2]
    prefsource2 = fields[3]
    if prefsource2.lower() not in sources:
        print("NOT MERGED - invalid source2 - {}".format(line2))
        continue

    # find the first concept - query returns the concept ID
    query1 = "select distinct t.DrugConceptID from NLMDrugTerm as t, NLMDrugAuthoritativeSource as s, NLMDrugConcept as c"
    query2 = " where t.DrugTermName = %s and s.Name = %s"
    query3 = " and t.DrugAuthoritativeSourceID=s.DrugAuthoritativeSourceID"
    query4 = " and c.DrugConceptId=t.DrugConceptId and c.PreferredTermId=t.DrugTermId"
    query = query1 + query2 + query3 + query4
    cursor.execute(query, (prefname1,prefsource1))
    records1 = cursor.fetchall()
    count1 = cursor.rowcount
    if count1 < 1:
        print("NOT MERGED: {} - concept 1 not found".format(line2))
        continue
    if count1 > 1:
        print("NOT MERGED: {} - more than 1 concept found for concept 1".format(line2))
        continue
    for row in records1:
        conceptID1 = row[0]

    # find the second concept - query returns the concept ID
    query1 = "select distinct t.DrugConceptID from NLMDrugTerm as t, NLMDrugAuthoritativeSource as s, NLMDrugConcept as c"
    query2 = " where t.DrugTermName = %s and s.Name = %s"
    query3 = " and t.DrugAuthoritativeSourceID=s.DrugAuthoritativeSourceID"
    query4 = " and c.DrugConceptId=t.DrugConceptId and c.PreferredTermId=t.DrugTermId"
    query = query1 + query2 + query3 + query4
    cursor.execute(query, (prefname2,prefsource2))
    records2 = cursor.fetchall()
    count2 = cursor.rowcount
    if count2 < 1:
        print("NOT MERGED: {} - concept 2 not found".format(line2))
        continue
    if count2 > 1:
        print("NOT MERGED: {} - more than 1 concept found for concept 2".format(line2))
        continue
    for row in records2:
        conceptID2 = row[0]

    if conceptID2 == conceptID1:
        print("NOT MERGED {} - concepts are the same".format(line2))
        continue
    if checkonly == 1:
        print("INFO: Concept 1 ({}):ID={}, Concept 2 ({}):ID={}".format(prefname1,conceptID1,prefname2,conceptID2))
    # check to see how many records will be merged
    query = "select DrugTermID, DrugTermName from NLMDrugTerm where DrugConceptID = %s"
    cursor.execute(query, (conceptID1,))
    termrecords = cursor.fetchall()
    termcount = cursor.rowcount
    if checkonly == 1:
        print("INFO: Term records to be merged:")
        for row in termrecords:
            print("        --> {} {}".format(row[0],row[1]))
        print("QUERY: UPDATE NLMDrugTerm SET DrugConceptID={},UpdatedDate='{}' WHERE DrugConceptID={}".format(conceptID2,formatted_date,conceptID1))
    else:
        query = "UPDATE NLMDrugTerm SET DrugConceptID=%s,UpdatedDate=%s WHERE DrugConceptID=%s"
        cursor.execute(query, (conceptID2,formatted_date,conceptID1))
        print("MERGED: Concept 1 ({}):ID={} -> Concept 2 ({}):ID={} ({} terms)".format(prefname1,conceptID1,prefname2,conceptID2,termcount))

    # delete the merged concept record
    if checkonly == 1:
        print("QUERY: DELETE FROM NLMDrugConcept where DrugConceptID = {}".format(conceptID1))
    else:
        query = "DELETE FROM NLMDrugConcept where DrugConceptID = %s"
        cursor.execute(query,(conceptID1,))
        print("REMOVED Concept Record for ID= {}".format(conceptID1))

    # check to see if any concept to concept records need to be changed
    query1 = "SELECT c2.DrugConceptConceptID,c2.Relation,c2.DrugConceptID2,t.DrugTermName"
    query2 = " FROM NLMDrugConcepttoConcept as c2, NLMDrugConcept as c, NLMDrugTerm as t"
    query3 = " WHERE DrugConceptID1=%s and c2.DrugConceptID2=c.DrugConceptID and c.PreferredTermID=t.DrugTermID"
    query = query1 + query2 + query3
    cursor.execute(query, (conceptID1,))
    c2crecords = cursor.fetchall()
    reccount = cursor.rowcount
    if reccount > 0:
        if checkonly == 1:
            print("INFO: Concept to concept records to be changed:")
            for row in c2crecords:
                print("        --> relation:{} concept2ID:{}  {}".format(row[1],row[2],row[3]))
            print("QUERY: UPDATE NLMDrugConcepttoConcept SET DrugConceptID1 = {},UpdatedDateTime='{}' WHERE DrugConceptID1={}".format(conceptID2,formatted_date,conceptID1))
        else:
            query = "UPDATE NLMDrugConcepttoConcept SET DrugConceptID1 = %s,UpdatedDateTime=%s WHERE DrugConceptID1=%s"
            cursor.execute(query, (conceptID2,formatted_date,conceptID1))
            print("UPDATED: Concept to Concept Table ({} records)".format(reccount))
    # modify query to check for concept in second position in concept to concept table
    query1 = "SELECT c2.DrugConceptConceptID,c2.Relation,c2.DrugConceptID1,t.DrugTermName"
    query2 = " FROM NLMDrugConcepttoConcept as c2, NLMDrugConcept as c, NLMDrugTerm as t"
    query3 = " WHERE DrugConceptID2=%s and c2.DrugConceptID1=c.DrugConceptID and c.PreferredTermID=t.DrugTermID"
    query = query1 + query2 + query3
    cursor.execute(query, (conceptID1,))
    c2crecords = cursor.fetchall()
    reccount2 = cursor.rowcount
    if reccount2 > 0:
        if checkonly == 1:
            print("INFO: Concept to concept records to be changed:")
            for row in c2crecords:
                print("        --> concept1ID:{}  {}, relation:{}".format(row[1],row[2],row[3]))
            print("QUERY: UPDATE NLMDrugConcepttoConcept SET DrugConceptID2 = {},UpdatedDateTime='{}' WHERE DrugConceptID1={}".format(conceptID2,formatted_date,conceptID1))
        else:
            query = "UPDATE NLMDrugConcepttoConcept SET DrugConceptID2 = %s,UpdatedDateTime=%s WHERE DrugConceptID2=%s"
            cursor.execute(query, (conceptID2,formatted_date,conceptID1))
            print("UPDATED: Concept to Concept Table ({} records)".format(reccount2))

    if reccount == 0 and reccount2 == 0:
        print("INFO: No Changes to the Concept to Concept Table")
cursor.close()
cnx.close()
