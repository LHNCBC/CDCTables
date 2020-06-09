#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 22-Nov-2019
@author: lee peters 
"""
# Get the classes that a drug isa member of from the MariaDB **** database 
#  input is either a drug concept ID or drug name
#

import sys
import mysql.connector as mariadb

substId="68114"
name = 0
# process command line arguments
# if specified, first arg is the concept id
# if second arg is specified, a term name is 
arglen = len(sys.argv)
if arglen > 1:
    substId = sys.argv[1]
    if arglen == 3 and sys.argv[2].lower()=='-name':
        name = 1
else:
    print("FORMAT:  python {} id [-name]".format(sys.argv[0]))
    print("           where:")
    print("                   id      the concept identifier")
    print("                   -name   id is a name")
    sys.exit()
 
cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
# process name
if name == 1:
    # get the concept Id from the name
    substName = substId
    query1 = "SELECT DISTINCT c.DrugConceptID"
    query2 = " from NLMDrugConcept as c, NLMDrugTerm as t"
    query3 = " where t.DrugTermName = %s"
    query4 = " "
    query5 = " and t.DrugConceptID = c.DrugConceptID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (substId,))
    records = cursor.fetchall()
    count = cursor.rowcount
    if count < 1:
        print("\nERROR: name not found {}\n".format(substName))
        sys.exit()
    # for each concept Id, get the classes that it is a member of
    for row in records:
        substId = row[0]
        query1 = "select a.DrugTermName,DrugConceptID1,relation,b.DrugTermName,DrugConceptID2,y.Name,d.DrugSourceConceptID"
        query2 = " from NLMDrugConcepttoConcept as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugConcept as c, NLMDrugConcept as d, NLMDrugAuthoritativeSource as y"
        query3 = " where r.DrugConceptID1 = %s and r.Relation='memberof'"
        query4 = " and DrugConceptID1=c.DrugConceptID and DrugConceptID2=d.DrugConceptID"
        query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID and b.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
        query = query1 + query2 + query3 + query4 + query5
        cursor.execute(query, (substId,))
        records = cursor.fetchall()
        
        print("Concept {}({}) is a member of these classes:".format(substName,substId))
        for row in records:
            print(" {0:^6} {1:^6} {3:^6} {2:<15}".format(row[5],row[4],row[3],row[6]))

# process Id 
else:
    # get the concept information
    query1 = "SELECT c.PreferredTermID,c.DrugSourceConceptID,t.DrugTermName,s.Name,z.Description"
    query2 = " from NLMDrugConcept as c, NLMDrugAuthoritativeSource as s, NLMDrugConceptType as z, NLMDrugTerm as t"
    query3 = " where c.DrugConceptID = %s"
    query4 = " and c.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
    query5 = " and c.DrugConceptTypeID = z.DrugConceptTypeID and t.DrugTermID=c.PreferredTermID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (substId,))
    count = 0
    for (PreferredTermID,DrugSourceConceptID,DrugTermName,Name,Description) in cursor:
        count += 1
        print("ConceptID:{}, ConceptType:{}, SourceID:{}, PreferredTerm:{} ({}), Source:{}".format(substId,Description,DrugSourceConceptID,DrugTermName,PreferredTermID,Name))

    if count < 1:
        print("\nERROR: concept id {} not found\n".format(substId))
        sys.exit()
# 
    query1 = "select a.DrugTermName,DrugConceptID1,relation,b.DrugTermName,DrugConceptID2,y.Name,d.DrugSourceConceptID"
    query2 = " from NLMDrugConcepttoConcept as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugConcept as c, NLMDrugConcept as d, NLMDrugAuthoritativeSource as y"
    query3 = " where r.DrugConceptID1 = %s and r.Relation='memberof'"
    query4 = " and DrugConceptID1=c.DrugConceptID and DrugConceptID2=d.DrugConceptID"
    query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID and b.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (substId,))
    records = cursor.fetchall()
    count = cursor.rowcount
    if count < 1:
        print("\nNo class membership found\n")
    else:
        print("Member of these classes:")
        for row in records:
            print(" {0:^6} {1:^6} {3:^6} {2:<15}".format(row[5],row[4],row[3],row[6]))

cursor.close()
cnx.close()
