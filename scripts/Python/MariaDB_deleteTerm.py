#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 2-Jun-2020
@author: robert wynne
"""
# Removing a term from a concept
# Deactivate a term in the MariaDB opioid database
#
# Input: parameters from the command line
# 	1. The term to deactivate
#   2. Source of the term
#   3. Preferred term of the concept
#
# Processing:
#	1. Find the conceptID and termID for the first parameters
#   2. If found, deactivate the term
#	3. Search for active terms with the conceptID
#	4. If there are none, then deactivate the concept
#
import sys
import mysql.connector as mariadb
from datetime import datetime

def get_drug_term(termName, conceptSource, preferredName):
	getTermQuery1 = "SELECT c.DrugConceptID, t.DrugTermID"
	getTermQuery2 = " FROM NLMDrugConcept as c, NLMDrugTerm as p, NLMDrugTerm as t, NLMDrugAuthoritativeSource as s"
	getTermQuery3 = " WHERE t.DrugTermName ='" + termName + "' and t.DrugAuthoritativeSourceID = s.DrugAuthoritativeSourceID"
	getTermQuery4 = " and s.Name ='" + conceptSource + "' and t.DrugConceptID = c.DrugConceptID and c.PreferredTermId=p.DrugTermID"
	getTermQuery5 = " and p.DrugTermName='" + preferredName + "'"
	getTermQuery = getTermQuery1 + getTermQuery2 + getTermQuery3 + getTermQuery4 + getTermQuery5
	records = run_full_query(getTermQuery, "false")
	return records
	
def run_full_query(query, commit):
	print(query)
	records = ""
	try:
		conx = mariadb.connect(user='root', password='**********', database='opioid')
		cursor = conx.cursor()
		cursor.execute(query)
		if "SELECT" in query:
			records = cursor.fetchall()
		if commit == "true":
			conx.commit();
	except Error as error:
		print(error)
	finally:
		cursor.close()
		conx.close()
	return records
	
def deactivate_term(termId, conceptId):
	#R. Dharmkar, https://www.tutorialspoint.com/How-to-insert-date-object-in-MySQL-using-Python
	now = datetime.now()
	formattedDate = now.strftime('%Y-%m-%d %H:%M:%S')
	updateQuery = "UPDATE NLMDrugTerm SET IsActive=0,UpdatedDate='" + str(formattedDate) + "' WHERE DrugTermID='" + str(termId) + "'"
	run_full_query(updateQuery, "true")
	getTermsQuery = "SELECT DrugTermID from NLMDrugTerm WHERE DrugConceptID='" + str(conceptId) + "' and IsActive=1"
	records = run_full_query(getTermsQuery, "false")
#       If there are not active terms in the concept, deactivate the concept
	if len(records) == 0:
		updateConcept = "UPDATE NLMDrugConcept SET IsActive=0, UpdatedDate='" + str(formattedDate) + "' WHERE DrugConceptID='" + str(conceptId) + "'"
		run_full_query(updateConcept, "true")
	
arglen = len(sys.argv)	
if arglen == 4:
	termName = sys.argv[1]
	conceptSource = sys.argv[2]
	preferredName = sys.argv[3]
	drugTermRecords = get_drug_term(termName, conceptSource, preferredName)
	if len(drugTermRecords) > 0:
		for d in drugTermRecords:
			print("drugTermRecord {}".format(d))
			conceptId = d[0]
			termId = d[1]
			deactivate_term(termId, conceptId)
		print("DELETED: Term ({}) has been deleted (marked as Inactive).".format(termName))
	else:
		print("NOT DELETED: No records found for term ({}) with preferred name ({}) of source ({}).".format(termName, preferredName, conceptSource))
else:
	print("FORMAT:   python {} [term] [source] [preferred term]".format(sys.argv[0]))
	sys.exit()
