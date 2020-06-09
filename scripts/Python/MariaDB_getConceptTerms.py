#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 15-Nov-2019
@author: lee peters 
"""
# Get the term information from the MariaDB **** database 
#

import sys
import mysql.connector as mariadb

nospell = 1
conceptID = "10437"
# process command line arguments
# if specified, argument is the term 
arglen = len(sys.argv)
if arglen > 1:
    conceptID = sys.argv[1]
    if arglen == 3 and sys.argv[2] == '-spell':
        nospell = 0
else:
    print("FORMAT:   python {} conceptID [-spell]".format(sys.argv[0]))
    print("Example:  python {} 69691 -spell".format(sys.argv[0]))
    sys.exit()
cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
# get the concept information
query1 = "SELECT c.PreferredTermID,c.DrugSourceConceptID,t.DrugTermName,s.Name,z.Description"
query2 = " from NLMDrugConcept as c, NLMDrugAuthoritativeSource as s, NLMDrugConceptType as z, NLMDrugTerm as t"
query3 = " where c.DrugConceptID = %s"
query4 = " and c.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
query5 = " and c.DrugConceptTypeID = z.DrugConceptTypeID and t.DrugTermID=c.PreferredTermID"
query = query1 + query2 + query3 + query4 + query5
cursor.execute(query, (conceptID,))
for (PreferredTermID,DrugSourceConceptID,DrugTermName,Name,Description) in cursor:
    print("ConceptID:{}, ConceptType:{}, SourceID:{}, PreferredTerm:{} ({}), Source:{}".format(conceptID,Description,DrugSourceConceptID,DrugTermName,PreferredTermID,Name))
 
# get the terms
query1 = "SELECT t.DrugTermName,t.DrugTermID,t.DrugExternalID,s.Name"
query2 = " from NLMDrugTerm as t, NLMDrugAuthoritativeSource as s"
query3 = " where t.DrugConceptID = %s"
# change query if spelling terms are to be ignored
if nospell == 1:
    query3 = " where t.DrugConceptID = %s and s.Name != 'Misspelling'"
query4 = " and t.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
query5 = " order by s.Name, t.DrugTermName"
query = query1 + query2 + query3 + query4 + query5
cursor.execute(query, (conceptID,))
# use a formatted fixed field length output
print("Terms: ")
print("{0:^7} {1:^6} {2:^10} {3:<12}".format("ID","SRC","SRCID","Term"))
for (DrugTermName,DrugTermID,DrugExternalID,Name) in cursor:
    print("{0:>7} {1:^6} {2:^10} {3:<12}".format(DrugTermID,Name,DrugExternalID,DrugTermName))

cursor.close()
cnx.close()
