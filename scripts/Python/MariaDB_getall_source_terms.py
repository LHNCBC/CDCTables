#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 05-Nov-2019
@author: lee peters 
"""
# Dump the search terms for each source from the MariaDB **** database 
#

##import numpy as np
import pandas as pd
##import json
import sys
##sys.path.append('/nfsvol/mor-nchs/lee/python-code')


import mysql.connector as mariadb

sourceName = 'rxnorm'
# process command line arguments
# if specified, argument is the source file type 
arglen = len(sys.argv)
if arglen > 1:
    sourceName = sys.argv[1]

#### common word filtering for BrandName
frequentWord = pd.read_csv('Data/frequentWord.tsv', sep='\t')

cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
query = ("Select t.DrugTermName,y.Abbreviation from NLMDrugTerm as t, NLMDrugTermType as y, NLMDrugAuthoritativeSource as a where a.Name= %s and t.DrugTTYID=y.DrugTTYID and t.DrugAuthoritativeSourceID=a.DrugAuthoritativeSourceID");
try:
    cursor.execute(query, (sourceName,))
    for (DrugTermName,Abbreviation) in cursor:
        if Abbreviation != 'BN':
            print("{}".format(DrugTermName))
        else:
#           check if BN is a frequent word, if so eliminate
            if DrugTermName.lower() not in list(frequentWord.Word):
                print("{}".format(DrugTermName))

except mariadb.Error as error:
    print("Error: {}".format(error))
cursor.close()
cnx.close()
