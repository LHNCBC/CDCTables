#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 15-Nov-2019
@author: lee peters 
"""
# Get the term information from the MariaDB **** database 
#

##import numpy as np
##import pandas as pd
##import json
import sys
##sys.path.append('/nfsvol/mor-nchs/lee/python-code')


import mysql.connector as mariadb

termID = '10436'
termName = ' '
termTTY = ' '
termSrc = ' '
nospell = 1
rel = ''
# process command line arguments
# if specified, argument is the term 
arglen = len(sys.argv)
if arglen > 1:
    termID = sys.argv[1]
    count = 2
    while count < arglen:
        val = sys.argv[count].lower()
        if val == '-spell':
            nospell = 0
        elif val == '-rel':
            count += 1
            if count == arglen:
                print("ERROR: no rel value specified on command line")
                sys.exit()
            rel = sys.argv[count].lower()
        else:
            print("ERROR: invalid option - %s".format(val))
            sys.exit()

        count += 1

else:
    print("FORMAT:  python {} id [-spell][-rel r]".format(sys.argv[0]))
    sys.exit()

cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
# query to get term name, tty, src
queryTerm1 = "SELECT DrugTermName,Abbreviation,Name from NLMDrugTerm as t,NLMDrugTermType as y,NLMDrugAuthoritativeSource as s"
queryTerm2 = " where DrugTermID = %s and t.DrugTTYID=y.DrugTTYID and t.DrugAuthoritativeSourceID=s.DrugAuthoritativeSourceID"
queryTerm = queryTerm1 + queryTerm2
cursor.execute(queryTerm, (termID,))
records = cursor.fetchall()
# NOTE: should only be one record
for row in records:
    termName = row[0]
    termTTY = row[1]
    termSrc = row[2]
# use the id as subject (first term) in the relation table
query1 = "SELECT t.DrugTermName,t.DrugTermID,r.Relation,y.Abbreviation"
query2 = " from NLMDrugConcept as c, NLMDrugTerm as t, NLMDrugTermType as y, NLMDrugAuthoritativeSource as s, NLMDrugTermTerm as r"
query3 = " where r.DrugTermID1 = %s"
if nospell == 1:
    query3 = " where r.DrugTermID1 = %s and s.name != 'Misspelling'"
if len(rel) > 0:
    part1 = query3
    part2 = " and r.relation = %s"
    query3 = part1 + part2

query4 = " and t.DrugTTYID = y.DrugTTYID and t.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
query5 = " and t.DrugConceptID = c.DrugConceptID and r.DrugTermID2=t.DrugTermID"
##query = ("SELECT DrugTermTermID,DrugTermID1,Relation,DrugTermID2 from NLMDrugTermTerm where DrugTermID1= %s")
query = query1 + query2 + query3 + query4 + query5
if len(rel) > 0:
    cursor.execute(query, (termID,rel))
else:
    cursor.execute(query, (termID,))
# use a formatted fixed field length output
print("{0:^12} {1:^8} {2:^5} {3:^5} {4:^12} {5:^8} {6:^5} {7:^5}".format("Term","TermID1","TTY","REL","Term","TermID2","TTY","SRC"))
for (DrugTermName,DrugTermID,Relation,Abbreviation) in cursor:
    dtName = DrugTermName
    dtId = DrugTermID
    rel = Relation
    abbr = Abbreviation    
    print("{0:^12} {1:^8} {2:^5} {3:^5} {4:^12} {5:^8} {6:^5} {7:^5}".format(termName,termID,termTTY,rel,dtName,dtId,abbr,termSrc))

# modify query to use the id as object (second term) in the relation table
query3 = " where r.DrugTermID2 = %s"
if nospell == 1:
    query3 = " where r.DrugTermID2 = %s and s.name != 'Misspelling'"
if len(rel) > 0:
    part1 = query3
    part2 = " and r.relation = %s"
    query3 = part1 + part2

query5 = " and t.DrugConceptID = c.DrugConceptID and r.DrugTermID1=t.DrugTermID"
query = query1 + query2 + query3 + query4 + query5
if len(rel) > 0:
    cursor.execute(query, (termID,rel))
else:
    cursor.execute(query, (termID,))

for (DrugTermName,DrugTermID,Abbreviation,Relation) in cursor:
    dtName = DrugTermName
    dtId = DrugTermID
    rel = Relation
    abbr = Abbreviation    
    print("{0:^12} {1:^8} {2:^5} {3:^5} {4:^12} {5:^8} {6:^5} {7:^5}".format(dtName,dtId,abbr,rel,termName,termID,termTTY,termSrc))

cursor.close()
cnx.close()
