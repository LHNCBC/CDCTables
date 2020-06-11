#!/usr/bin/env python3
#
# Adding a new concept
import sys
import mysql.connector as mariadb

def concept_exists(name, source, conceptType):
	conceptQuery1 = "SELECT t.DrugConceptID from NLMDrugTerm as t, NLMDrugConcept as c, NLMDrugConceptType as y "
	conceptQuery2 = "WHERE t.DrugTermName= %s and t.DrugTermID=c.PreferredTermID and t.IsActive=1 "
	conceptQuery3 = "and c.DrugConceptTypeID=y.DrugConceptTypeID and y.Description= %s"
	conceptQuery = conceptQuery1 + conceptQuery2 + conceptQuery3
	args = (name, conceptType)
	results = run_select_query(conceptQuery,args)
	if len(results) > 0:
		for r in results:
			print("Concept already exists for {} (ConceptID {})".format(name,r[0]))
		return "true"
	else:
		# print("Concept doesn't exist.")
		return "false"

def run_select_query(query, args):
	records = ""
	try:
		# print("In runQuery")
		print("Query: " + query)
		print("Args: {}".format(args))
		conx = mariadb.connect(user='root', password='************', database='opioid')
		cursor = conx.cursor()
		cursor.execute(query, args)
		records = cursor.fetchall()
	except Error as error:
		print(error)
	finally:
		cursor.close()
		conx.close()
	return records
	
def run_insert_query(query, args):
	try:
#		print("In runQuery")
#		print("Query: " + query)
#		print("Args: {}".format(args))
		conx = mariadb.connect(user='root', password='************', database='opioid')
		cursor = conx.cursor()
		cursor.execute(query, args)
		conx.commit();
	except Error as error:
		print(error)
	finally:
		cursor.close()
		conx.close()
	
def run_full_query(query, commit):
	records = ""
	try:
		conx = mariadb.connect(user='root', password='************', database='opioid')
		cursor = conx.cursor()
		cursor.execute(query)
		records = cursor.fetchall()
		if commit == "true":
			conx.commit();
	except Error as error:
		print(error)
	finally:
		cursor.close()
		conx.close()
	return records

def create_term(termName, sourceId, pvTypeId):
	index = get_max_index()
	insertTermQuery = "INSERT INTO NLMDrugTerm(DrugTermID, DrugTermName, DrugTTYID, DrugExternalID, DrugAuthoritativeSourceID, IsActive, DrugConceptID) VALUES(%s,%s,%s,%s,%s,%s,%s)"
	args = (index + 1, termName, pvTypeId, "", sourceId, 1, index + 2)
	run_insert_query(insertTermQuery, args)
	return index + 1
	
def create_concept(termId, sourceId, conceptTypeId):
	index = get_max_index()
	insertConceptQuery = "INSERT INTO NLMDrugConcept(DrugConceptID, PreferredTermID, DrugAuthoritativeSourceID, DrugConceptTypeID, DrugSourceConceptID, IsActive) VALUES(%s,%s,%s,%s,%s,%s)"
	args = (index + 1, termId, sourceId, conceptTypeId, "", 1)
	run_insert_query(insertConceptQuery, args)
	
def get_max_index():
	maxIndex = 0
	cnx = mariadb.connect(user='root', password='************', database='opioid')
	cursor = cnx.cursor()

	max1query = "select max(DrugAuthoritativeSourceID) from NLMDrugAuthoritativeSource"
	max2query = "select max(DrugConceptTypeID) from NLMDrugConceptType"
	max3query = "select max(DrugTTYID) from NLMDrugTermType"
	max4query = "select max(DrugTermID) from NLMDrugTerm"
	max5query = "select max(DrugConceptID) from NLMDrugConcept"
	max6query = "select max(DrugTermTermID) from NLMDrugTermTerm"
	max7query = "select max(DrugConceptConceptID) from NLMDrugConcepttoConcept"
	maxarr = [max1query,max2query,max3query,max4query,max5query,max6query,max7query]
  
	for q in maxarr:
		cursor.execute(q)
		records = cursor.fetchall()
		for r in records:
			tmpMax = r[0]
			# print("Max test: {}".format(tmpMax))
			if tmpMax > maxIndex:
				maxIndex = tmpMax
#	print("Max is {}".format(maxIndex))
	return maxIndex
# arguments to this function
#  concept name to add (this is also the principle variant)
#  concept source (should be an existing one)
#  concept type (SUBSTANCE or CLASS)
arglen = len(sys.argv)	
if arglen == 4:
	conceptName = sys.argv[1]
	conceptSource = sys.argv[2]
	conceptType = sys.argv[3]
	conceptExist = concept_exists(conceptName, conceptSource, conceptType)
	if conceptExist == "false":
	
		sourceQuery = "SELECT DrugAuthoritativeSourceID from NLMDrugAuthoritativeSource where Name='" + conceptSource + "'"
		conceptTypeQuery = "SELECT DrugConceptTypeID from NLMDrugConceptType where Description='" + conceptType + "'"
		termTypePVQuery = "SELECT DrugTTYID from NLMDrugTermType where Abbreviation='PV'"
		
		sourceRecords = run_full_query(sourceQuery, "false")
		conceptTypeRecords = run_full_query(conceptTypeQuery, "false")
		termTypeRecords = run_full_query(termTypePVQuery, "false")
		
		sourceId = None
		conceptTypeId = None
		pvTypeId = None
		
		for s in sourceRecords:
			sourceId = s[0]
#			print("sourceId={}".format(sourceId))
		for c in conceptTypeRecords:
			conceptTypeId = c[0]
#			print("cTypeId={}".format(conceptTypeId))
		for t in termTypeRecords:
			pvTypeId = t[0]
		if sourceId != None and conceptTypeId != None and pvTypeId != None:
			termId = create_term(conceptName, sourceId, pvTypeId)
			create_concept(termId, sourceId, conceptTypeId)
			print("Concept created for {} (Concept ID= {})".format(conceptName, termId + 1))
		else:
			if sourceId == None:
				print("Source doesn't exist {} - add not executed".format(conceptSource))
			if conceptTypeId == None:
				print("Concept type doesn't exist {} - add not executed".format(conceptType))
			if pvTypeId == None:
				print("Term type dosen't exist - add not executed")
	# else:
		# print("Concept already exists. Exiting.")
else:
    print("FORMAT:   python {} [term] [source] [conceptType]".format(sys.argv[0]))
    sys.exit()

