#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 05-Feb-2020
@author: lee peters 
"""
# Dump all the substances from the MariaDB **** database 
#

import sys

import mysql.connector as mariadb

# no command line arguments
cnx = mariadb.connect(user='****', password='****', database='****')
cursor = cnx.cursor()
query1 = "select distinct t.DrugTermName,p.DrugTermName"
query2 = " from NLMDrugTerm as t, NLMDrugTerm as p, NLMDrugConcept as c, NLMDrugConceptType as ctty"
query3 = " where t.DrugConceptID=c.DrugConceptID and c.PreferredTermID=p.DrugTermId"
query4 = " and c.DrugConceptTypeID=ctty.DrugConceptTypeID and ctty.Description='substance'"
query5 = " order by p.DrugTermName, t.DrugTermName"
query = query1 + query2 + query3 + query4 + query5
try:
    cursor.execute(query, )
    records = cursor.fetchall()
    for row in records:
        variant = row[0]
        principal = row[1]
        print("{}|{}".format(variant,principal))

except mariadb.Error as error:
    print("Error: {}".format(error))
cursor.close()
cnx.close()
