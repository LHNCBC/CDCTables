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
# process command line arguments
# if specified, argument is the term 
arglen = len(sys.argv)
if arglen > 1:
    termID = sys.argv[1]
cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
query1 = "SELECT t.DrugTermName,c.DrugConceptID,t.DrugTermID,s.Name,y.Abbreviation,c.PreferredTermID"
query2 = " from NLMDrugConcept as c, NLMDrugTerm as t, NLMDrugTermType as y, NLMDrugAuthoritativeSource as s"
query3 = " where t.DrugTermID = %s"
query4 = " and t.DrugTTYID = y.DrugTTYID and t.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
query5 = " and t.DrugConceptID = c.DrugConceptID"
##query = ("SELECT DrugTermTermID,DrugTermID1,Relation,DrugTermID2 from NLMDrugTermTerm where DrugTermID1= %s")
query = query1 + query2 + query3 + query4 + query5
cursor.execute(query, (termID,))
# use a formatted fixed field length output
print("{0:^12} {1:^8} {2:^8} {3:^8} {4:^5} {5:^8}".format("Term","Concept","TermID","TTY","SRC","PrefID"))
for (DrugTermName,DrugConceptID,DrugTermID,Name,Abbreviation,PreferredTermID) in cursor:
    print("{0:^12} {1:^8} {2:^8} {3:^8} {4:^5} {5:^8}".format(DrugTermName,DrugConceptID,DrugTermID,Name,Abbreviation,PreferredTermID))

cursor.close()
cnx.close()
