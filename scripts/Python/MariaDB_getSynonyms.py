#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 05-Nov-2019
@author: lee peters 
"""
# Dump the synonyms from the MariaDB **** database
#  
#

##import numpy as np
##import pandas as pd
##import json
import sys
##sys.path.append('/nfsvol/mor-nchs/lee/python-code')


import mysql.connector as mariadb

# process command line arguments
# if argument is "msp", user wants misspelling pairs
# otherwise, generate synonyms file (containing SY,BN,PIN)
spelling = 0
arglen = len(sys.argv)
if arglen > 1 :
    spelling = 1
cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
query1 = "select distinct a.DrugTermName,b.DrugTermName"
query2 = " from NLMDrugTerm as a, NLMDrugTerm as b, NLMDrugTermTerm as r"
query3 = " where r.DrugTermID1=a.DrugTermID and r.DrugTermID2=b.DrugTermID"
query4 = " and (relation='BN' OR (relation != 'UNII' and relation != 'MSP' and  substring(a.DrugTermName,1,length(b.DrugTermName)) != b.DrugtermName))"
query5 = " order by a.DrugTermName"
if spelling == 1:
    query4 = " and relation = 'MSP'"    
query = query1 + query2 + query3 + query4 + query5
try:
    cursor.execute(query, )
    records = cursor.fetchall()
    for row in records:
        synonym = row[0]
        synonym_for = row[1]
        print("{}|{}".format(synonym,synonym_for))

except mariadb.Error as error:
    print("Error: {}".format(error))
cursor.close()
cnx.close()
