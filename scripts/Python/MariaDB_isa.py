#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 22-Nov-2019
@author: lee peters 
"""
# Get the class isa relations from the MariaDB **** database 
#  input is either a class concept ID or class name
#

import sys
import mysql.connector as mariadb

fmt = "python MariaDB_isa.py id [-name|-srcid]"
clssId="68114"
name = 0
# process command line arguments
# if specified, first arg is the class id
# if second arg is specified, a class name is specified
arglen = len(sys.argv)
if arglen > 1:
    clssId = sys.argv[1]
    if arglen > 2:
        val = sys.argv[2].lower()
        if val=='-name':
            name = 1
        elif val=='-srcid':
            name = 2
        else:
            print("Invalid command line parameter {}\n  Format is: {}".format(val,fmt))
            sys.exit()
else:
     print("Missing command line arguments, format:\n  {}".format(fmt))
     sys.exit()            

cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
if name == 1:
    clssName = clssId
    query1 = "SELECT DISTINCT c.DrugConceptID, c.DrugSourceConceptID, y.Name"
    query2 = " from NLMDrugConcept as c, NLMDrugTerm as t, NLMDrugAuthoritativeSource as y"
    query3 = " where t.DrugTermName = %s"
    query4 = " and c.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
    query5 = " and t.DrugConceptID = c.DrugConceptID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (clssName,))
    records = cursor.fetchall()
    for row in records:
        clssId = row[0]
        clssSrcId = row[1]
        clssSrc = row[2]
        query1 = "select a.DrugTermName,DrugConceptID1,relation,b.DrugTermName,DrugConceptID2,y.Name,d.DrugSourceConceptID"
        query2 = " from NLMDrugConcepttoConcept as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugConcept as c, NLMDrugConcept as d, NLMDrugAuthoritativeSource as y"
        query3 = " where r.DrugConceptID1 = %s and r.Relation='isa'"
        query4 = " and DrugConceptID1=c.DrugConceptID and DrugConceptID2=d.DrugConceptID"
        query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID and b.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
        query = query1 + query2 + query3 + query4 + query5
        cursor.execute(query, (clssId,))
        records = cursor.fetchall()
        count = cursor.rowcount
        if count > 0:
            print("\n{} concept {} ({}) conceptId:{} has ancestors:".format(clssSrc, clssName, clssSrcId, clssId))
        else:
            print("\n{} concept {} ({}) conceptId:{} has no ancestors\n".format(clssSrc, clssName,clssSrcId, clssId))
        for row in records:
            print(" {0:^6} {1:<15} {2:^5} conceptId:{3:<6}".format(row[5],row[3],row[6],row[4]))

#       Now get the descendants
        query1 = "select a.DrugTermName,DrugConceptID1,relation,b.DrugTermName,DrugConceptID2,y.Name,c.DrugSourceConceptID"
        query3 = " where r.DrugConceptID2 = %s and r.Relation='isa'"
        query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID and a.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
        query = query1 + query2 + query3 + query4 + query5
        cursor.execute(query, (clssId,))
        records = cursor.fetchall()
        count = cursor.rowcount
        if count > 0:
            print("\n{} concept {} ({}) conceptId:{} has children:".format(clssSrc, clssName, clssSrcId, clssId))
        else:
            print("\n{} concept {} ({}) conceptId:{} has no children\n".format(clssSrc, clssName,clssSrcId, clssId))
        for row in records:
            childName = row[0]
            childConcId = row[1]
            childSrc = row[5]
            childSrcId = row[6]
            print(" {0:^6} {1:<15} {2:^5} conceptId:{3:<6}".format(childSrc,childName,childSrcId,childConcId))
 
elif name==2:
    # find the concepts by the concept source id 
    clssSrcID = clssId
    query1 = "SELECT c.PreferredTermID,c.DrugConceptID,t.DrugTermName,s.Name,z.Description"
    query2 = " from NLMDrugConcept as c, NLMDrugAuthoritativeSource as s, NLMDrugConceptType as z, NLMDrugTerm as t"
    query3 = " where c.DrugSourceConceptID = %s"
    query4 = " and c.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
    query5 = " and c.DrugConceptTypeID = z.DrugConceptTypeID and t.DrugTermID=c.PreferredTermID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (clssId,))
    records = cursor.fetchall()
    count = cursor.rowcount

    if count < 1:
        print("\nERROR: no concept found for concept source id={}".format(clssId))
        sys.exit()
    for row in records:
        preferredTermID = row[0]
        clssId = row[1]
        clssName = row[2]
        clssSrc = row[3]
#        print("\nConceptID:{}, ConceptType:{}, SourceID:{}, PreferredTerm:{} ({}), Source:{}".format(clssId,row[4],clssSrcID,clssName,preferredTermID,clssSrc))

        query1 = "select a.DrugTermName,DrugConceptID1,relation,b.DrugTermName,DrugConceptID2,y.Name,d.DrugSourceConceptID"
        query2 = " from NLMDrugConcepttoConcept as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugConcept as c, NLMDrugConcept as d, NLMDrugAuthoritativeSource as y"
        query3 = " where r.DrugConceptID1 = %s and r.Relation='isa'"
        query4 = " and DrugConceptID1=c.DrugConceptID and DrugConceptID2=d.DrugConceptID"
        query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID and b.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
        query = query1 + query2 + query3 + query4 + query5
        cursor.execute(query, (clssId,))
        records = cursor.fetchall()
        count = cursor.rowcount
        if count > 0:
            print("\n{} concept {} ({}) conceptId:{} has ancestors:".format(clssSrc, clssName, clssSrcID, clssId))
        else:
            print("\n{} concept {} ({}) conceptId:{} has no ancestors\n".format(clssSrc, clssName,clssSrcID, clssId))
        for row in records:
            print(" {0:^6} {1:<15} {2:^5} conceptId:{3:<6}".format(row[5],row[3],row[6],row[4]))

#       Now get the descendants
        query1 = "select a.DrugTermName,DrugConceptID1,relation,b.DrugTermName,DrugConceptID2,y.Name,c.DrugSourceConceptID"
        query3 = " where r.DrugConceptID2 = %s and r.Relation='isa'"
        query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID and a.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
        query = query1 + query2 + query3 + query4 + query5
        cursor.execute(query, (clssId,))
        records = cursor.fetchall()
        count = cursor.rowcount
        if count > 0:
            print("\n{} concept {} ({}) conceptId:{} has children:".format(clssSrc, clssName, clssSrcID, clssId))
        else:
            print("\n{} concept {} ({}) conceptId:{} has no children\n".format(clssSrc, clssName,clssSrcID, clssId))
        for row in records:
            childName = row[0]
            childConcId = row[1]
            childSrc = row[5]
            childSrcId = row[6]
            print(" {0:^6} {1:<15} {2:^5} conceptId:{3:<6}".format(childSrc,childName,childSrcId,childConcId))
    
else:
    # get the concept information
    query1 = "SELECT c.PreferredTermID,c.DrugSourceConceptID,t.DrugTermName,s.Name,z.Description"
    query2 = " from NLMDrugConcept as c, NLMDrugAuthoritativeSource as s, NLMDrugConceptType as z, NLMDrugTerm as t"
    query3 = " where c.DrugConceptID = %s"
    query4 = " and c.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
    query5 = " and c.DrugConceptTypeID = z.DrugConceptTypeID and t.DrugTermID=c.PreferredTermID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (clssId,))
    records = cursor.fetchall()
    count = cursor.rowcount

    if count < 1:
        print("\nERROR: no concept found for concept id={}".format(clssId))
        sys.exit()
    for row in records:
        preferredTermID = row[0]
        clssSrcID = row[1]
        clssName = row[2]
        clssSrc = row[3]
        print("\nConceptID:{}, ConceptType:{}, SourceID:{}, PreferredTerm:{} ({}), Source:{}".format(clssId,row[4],clssSrcID,clssName,preferredTermID,clssSrc))

#    for (PreferredTermID,DrugSourceConceptID,DrugTermName,Name,Description) in cursor:
#        clssName = DrugTermName
#        clssSrcId = DrugSourceConceptID
#        clssSrc = Name
#        print("\nConceptID:{}, ConceptType:{}, SourceID:{}, PreferredTerm:{} ({}), Source:{}".format(clssId,Description,DrugSourceConceptID,DrugTermName,PreferredTermID,Name))

    query1 = "select a.DrugTermName,DrugConceptID1,relation,b.DrugTermName,DrugConceptID2,y.Name,d.DrugSourceConceptID"
    query2 = " from NLMDrugConcepttoConcept as r, NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugConcept as c, NLMDrugConcept as d, NLMDrugAuthoritativeSource as y"
    query3 = " where r.DrugConceptID1 = %s and r.Relation='isa'"
    query4 = " and DrugConceptID1=c.DrugConceptID and DrugConceptID2=d.DrugConceptID"
    query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID and b.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (clssId,))
    records = cursor.fetchall()
    count = cursor.rowcount
    if count > 0:
        print("Ancestor(s):")
    else:
        print("No ancestors found!\n")

    for row in records:
        ancestorName = row[3]
        ancestorConcId = row[4]
        ancestorSrc = row[5]
        ancestorSrcId = row[6]
        print(" {0:^6} {1:<15} {2:^5} conceptId:{3:<6}".format(ancestorSrc,ancestorName, ancestorSrcId, ancestorConcId))

#   get the children classes
    query1 = "select a.DrugTermName,DrugConceptID1,relation,b.DrugTermName,DrugConceptID2,y.Name,c.DrugSourceConceptID"
    query3 = " where r.DrugConceptID2 = %s and r.Relation='isa'"
    query5 = " and c.PreferredTermID=a.DrugTermID and d.PreferredTermID=b.DrugTermID and a.DrugAuthoritativeSourceID=y.DrugAuthoritativeSourceID"
    query = query1 + query2 + query3 + query4 + query5
    cursor.execute(query, (clssId,))
    records = cursor.fetchall()
    count = cursor.rowcount
    if count > 0:
        print("\nChildren:")
    else:
        print("\nNo children classes\n")
    for row in records:
        childName = row[0]
        childConcId = row[1]
        childSrc = row[5]
        childSrcId = row[6]
        print(" {0:^6} {1:<15} {2:^5} conceptId:{3:<6}".format(childSrc,childName,childSrcId,childConcId))

cursor.close()
cnx.close()
