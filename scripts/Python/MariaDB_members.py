#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 15-Nov-2019
@author: lee peters 
"""
# Get the members of a class from the MariaDB **** database 
#

##import numpy as np
##import pandas as pd
##import json
import sys
##sys.path.append('/nfsvol/mor-nchs/lee/python-code')


import mysql.connector as mariadb

isID = 1
classID = '26'
fmt = "python Mariadb_members.py id [name|srcID]\n"
# process command line arguments
# if specified, argument is the term 
arglen = len(sys.argv)
if arglen < 2:
    print("FORMAT: python {} id [-name|-srcid]".format(sys.argv[0]))
    print("      where:")
    print("            id     the concept identifier")
    print("            -name  the id field is a name")
    print("            -srcid the id field is a source identifier (e.g. B01AA)")
    sys.exit()
else:
    classID = sys.argv[1]
    # second argument indicates a name or source id
    if arglen > 2:
        argtype = sys.argv[2].lower()
        if argtype == "-name":
            isID = 0
        elif argtype == "-srcid":
            isID = 2
        else:
            print("ERROR: invalid argument {}, must be '-name' or '-srcid'\n".format(argtype))
            sys.exit()
cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
# get the concept information
if isID==1:
    query1 = "SELECT c.PreferredTermID,c.DrugSourceConceptID,t.DrugTermName,s.Name,z.Description"
    query2 = " from NLMDrugConcept as c, NLMDrugAuthoritativeSource as s, NLMDrugConceptType as z, NLMDrugTerm as t"
    query3 = " where c.DrugConceptID = %s"
    query4 = " and c.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
    query5 = " and c.DrugConceptTypeID = z.DrugConceptTypeID and t.DrugTermID=c.PreferredTermID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (classID,))
    for (PreferredTermID,DrugSourceConceptID,DrugTermName,Name,Description) in cursor:
        print("ConceptID:{}, ConceptType:{}, SourceID:{}, PreferredTerm:{} ({}), Source:{}".format(classID,Description,DrugSourceConceptID,DrugTermName,PreferredTermID,Name))

    query1 = "select a.DrugTermName,DrugConceptID1,a.DrugExternalID,relation,b.DrugTermName,DrugConceptID2"
    query2 = " from NLMDrugConcepttoConcept as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugConcept as c, NLMDrugConcept as d"
    query3 = " where r.DrugConceptID2 = %s and r.Relation='memberof'"
    query4 = " and DrugConceptID1=c.DrugConceptID and DrugConceptID2=d.DrugConceptID"
    query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID order by a.DrugTermName"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (classID,))
    records = cursor.fetchall()
    print("Members:")
    print("  {0:<15} {1:^6} {2:^6}".format("NAME","CONID","SRCID"))
    for row in records:
        print(" {0:<15} {1:^6} {2:^6}".format(row[0],row[1],row[2]))
# process concept name
elif isID==0:
    className = classID
    query1 = "SELECT DISTINCT c.DrugConceptID, s.Name, c.DrugSourceConceptID"
    query2 = " from NLMDrugConcept as c, NLMDrugAuthoritativeSource as s, NLMDrugTerm as t, NLMDrugConceptType as z"
    query3 = " where t.DrugTermName = %s and z.Description='class'"
    query4 = " and c.DrugConceptID=t.DrugConceptID and c.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
    query5 = " and c.DrugConceptTypeID = z.DrugConceptTypeID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (className,))
    records = cursor.fetchall()
    for row in records:
       	classID = row[0]
        classSrc = row[1]
        classSrcID = row[2]

#       get the members
        query1 = "select a.DrugTermName,DrugConceptID1,a.DrugExternalID,relation,b.DrugTermName,DrugConceptID2"
        query2 = " from NLMDrugConcepttoConcept as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugConcept as c, NLMDrugConcept as d"
        query3 = " where r.DrugConceptID2 = %s and r.Relation='memberof'"
        query4 = " and DrugConceptID1=c.DrugConceptID and DrugConceptID2=d.DrugConceptID"
        query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID order by a.DrugTermName"
        query = query1 + query2 + query3 + query4 + query5
        cursor.execute(query, (classID,))
        records = cursor.fetchall()
        count = cursor.rowcount
        if count > 0:
            print("{} Members in {} ({})  source: {}, id: {}".format(count, className,classID,classSrc,classSrcID))
            print("  {0:<15} {1:^6} {2:^6}".format("NAME","CONID","SRCID"))
            for row in records:
                print("  {0:<15} {1:^6} {2:^6}".format(row[0],row[1],row[2]))
        else:
            print("No Members in {} ({})  source: {}, id: {}\n".format(className,classID,classSrc,classSrcID))
# process concept source ID
else:
    classSrcID = classID
    query1 = "SELECT DISTINCT c.DrugConceptID, s.Name, t.DrugTermName"
    query2 = " from NLMDrugConcept as c, NLMDrugAuthoritativeSource as s, NLMDrugTerm as t, NLMDrugConceptType as z"
    query3 = " where c.DrugSourceConceptID = %s and z.Description='class'"
    query4 = " and c.DrugConceptID=t.DrugConceptID and c.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
    query5 = " and c.DrugConceptTypeID = z.DrugConceptTypeID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (classSrcID,))
    records = cursor.fetchall()
    count = cursor.rowcount
    if count < 1:
        print("\nERROR: Class source id {} not found\n".format(classSrcID))
        sys.exit()

    for row in records:
        classID = row[0]
        classSrc = row[1]
        className = row[2]

#       get the members
        query1 = "select a.DrugTermName,DrugConceptID1,a.DrugExternalID,relation,b.DrugTermName,DrugConceptID2"
        query2 = " from NLMDrugConcepttoConcept as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugConcept as c, NLMDrugConcept as d"
        query3 = " where r.DrugConceptID2 = %s and r.Relation='memberof'"
        query4 = " and DrugConceptID1=c.DrugConceptID and DrugConceptID2=d.DrugConceptID"
        query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID order by a.DrugTermName"
        query = query1 + query2 + query3 + query4 + query5
        cursor.execute(query, (classID,))
        records = cursor.fetchall()
        count = cursor.rowcount
        if count > 0:
            print("{} Members in {} ({})  source: {}, id: {}".format(count, className,classID,classSrc,classSrcID))
            print("  {0:<15} {1:^6} {2:^6}".format("NAME","CONID","SRCID"))
            for row in records:
                print("  {0:<15} {1:^6} {2:^6}".format(row[0],row[1],row[2]))
        else:
            print("No Members in {} ({})  source: {}, id: {}\n".format(className,classID,classSrc,classSrcID))
 
cursor.close()
cnx.close()
