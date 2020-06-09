#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 15-Nov-2019
@author: lee peters 
"""
# Get the related term information from the MariaDB **** database 
#

import sys


import mysql.connector as mariadb

termName = ' '
rel = ''
nospell = 1
# process command line arguments
# if specified, argument is the term 
arglen = len(sys.argv)
if arglen > 1:
    termName = sys.argv[1]
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
    print("FORMAT:   python {} termName [-spell][-rel r]".format(sys.argv[0]))
    sys.exit()

cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
# use the name as subject (first term) in the relation table
query1 = "select r.drugtermid1, a.drugtermname, r.relation, r.drugtermid2, b.drugtermname, s.name"
query2 = " from NLMDrugTermTerm as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugAuthoritativeSource as s"
query3 = " where a.drugtermname = %s"
if nospell == 1:
    query3 = " where a.drugtermname = %s and s.name != 'Misspelling'"
if len(rel) > 0:
    part1 = query3
    part2 = " and r.relation = %s"
    query3 = part1 + part2
query4 = " and a.drugtermid=r.drugtermid1 and b.drugtermid=r.drugtermid2 and a.DrugAuthoritativeSourceID=s.DrugAuthoritativeSourceID"
query = query1 + query2 + query3 + query4 
if len(rel) > 0:
    cursor.execute(query, (termName,rel))
else:
    cursor.execute(query, (termName,))

record = cursor.fetchall()
# use a formatted fixed field length output
print("{0:^8} {1:^12} {2:^5} {3:^8} {4:^12} {5:^5}".format("ID1","Name","REL","ID2","Name","SRC"))
for row in record:
    drugtermid1 = row[0]
    drugname1 = row[1]
    relation = row[2]
    drugtermid2 = row[3]
    drugname2 = row[4]
    drugsource = row[5]
    print("{0:^8} {1:^12} {2:^5} {3:^8} {4:^12} {5:^5}".format(drugtermid1,drugname1,relation,drugtermid2,drugname2,drugsource))

# modify query to use the name as object (second term) in the relation table
query3 = " where b.drugtermname = %s"
if nospell == 1:
    query3 = " where b.drugtermname = %s and s.name != 'Misspelling'"
if len(rel) > 0:
    part1 = query3
    part2 = " and r.relation = %s"
    query3 = part1 + part2
query = query1 + query2 + query3 + query4
if len(rel) > 0:
    cursor.execute(query, (termName,rel))
else:
    cursor.execute(query, (termName,))

record = cursor.fetchall()

for row in record:
    drugtermid1 = row[0]
    drugname1 = row[1]
    relation = row[2]
    drugtermid2 = row[3]
    drugname2 = row[4]
    drugsource = row[5]
    print("{0:^8} {1:^12} {2:^5} {3:^8} {4:^12} {5:^5}".format(drugtermid1,drugname1,relation,drugtermid2,drugname2,drugsource))

cursor.close()
cnx.close()
