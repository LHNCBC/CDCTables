#!/usr/bin/env python3
#
"""
1)Adding a term (whitelist operation) to existing concept

minimum fields needed:
a) term name
1) rel
b) name of source of added term
c) preferred term name of concept to be added
d) type - SUBSTANCE or CLASS

other (optional) fields:
source id
comment
others?

algorithm:
open file with terms to be added
for each line:
   split fields from line
   search for concept record using the preferred term name and type (start with collecting the indexes from each field following the first)
   if found:
       create new NLMDrugTerm record
   else:
       user ERROR - preferred term name not found
"""

import sys
import mysql.connector as mariadb

"""
 Field                     Type             Null Key Default Extra
 ------------------------- ---------------- ---- --- ------- -----
 DrugTermID                int(10) unsigned NO   PRI NULL
 DrugTermName              varchar(500)     NO   MUL NULL
 DrugTTYID                 smallint(6)      YES      NULL
 DrugExternalID            varchar(32)      YES      NULL
 DrugAuthoritativeSourceID smallint(6)      YES      NULL
 CreationUserID            char(4)          YES      NULL
 CreationDate              datetime         YES      NULL
 UpdatedUserID             char(5)          YES      NULL
 UpdatedDate               datetime         YES      NULL
 IsActive                  tinyint(1)       YES      NULL
 DrugConceptID             bigint(20)       YES      NULL
"""	

def insert_term(DrugTermID, DrugTermName, DrugTTYID, DrugExternalID, DrugAuthoritativeSourceID, IsActive, DrugConceptID):
  query = "INSERT INTO NLMDrugTerm(DrugTermID, DrugTermName, DrugTTYID, DrugExternalID, DrugAuthoritativeSourceID, IsActive, DrugConceptID) VALUES(%s,%s,%s,%s,%s,%s,%s)"
  args = (DrugTermID, DrugTermName, DrugTTYID, DrugExternalID, DrugAuthoritativeSourceID, IsActive, DrugConceptID)
  try:
    conx = mariadb.connect(user='root', password='************', database='opioid')
    crsor = conx.cursor()
    print("Executing term insert {}".format(DrugTermName))
 #   print(query)
 #   print(args)
    crsor.execute(query, args)
    conx.commit()
  except Error as error:
    print(error)

  finally:
    crsor.close()
    conx.close()
	
"""
 Field                     Type             Null Key Default Extra
 ------------------------- ---------------- ---- --- ------- -----
 DrugConceptID             int(10) unsigned NO   PRI NULL
 PreferredTermID           bigint(20)       NO       NULL
 DrugAuthoritativeSourceID smallint(6)      YES      NULL
 DrugConceptTypeID         bigint(20)       YES      NULL
 DrugSourceConceptID       varchar(32)      YES      NULL
 CreationDate              datetime         YES      NULL
 CreationUserID            char(4)          YES      NULL
 UpdatedDate               datetime         YES      NULL
 UpdateUserID              char(4)          YES      NULL
 IsActive                  tinyint(1)       YES      NULL
"""

def insert_concept(DrugConceptID, PreferredTermID, DrugAuthoritativeSourceID, DrugConceptTypeID, DrugSourceConceptID, IsActive):
  query = "INSERT INTO NLMDrugConcept(DrugConceptID, PreferredTermID, DrugAuthoritativeSourceID, DrugConceptTypeID, DrugSourceConceptID, IsActive) VALUES(%s,%s,%s,%s,%s,%s)"
  args = (DrugConceptID, PreferredTermID, DrugAuthoritativeSourceID, DrugConceptTypeID, DrugSourceConceptID, IsActive)
  try:
    conx = mariadb.connect(user='root', password='************', database='opioid')
    crsor = conx.cursor()
    print("Executing concept insert")
 #   print(query)
 #   print(args)
    crsor.execute(query, args)
    conx.commit()
  except Error as error:
    print(error)

  finally:
    crsor.close()
    conx.close()

"""
describe NLMDrugTermTerm;
 Field            Type             Null Key Default Extra
 ---------------- ---------------- ---- --- ------- -----
 DrugTermTermID   int(10) unsigned NO   PRI NULL    
 DrugTermID1      bigint(20)       YES      NULL    
 Relation         char(50)         YES      NULL    
 DrugTermID2      bigint(20)       YES      NULL    
 CreationUserID   char(4)          YES      NULL    
 CreationDateTime datetime         YES      NULL    
 UpdatedUserID    char(4)          YES      NULL    
 UpdatedDateTime  datetime         YES      NULL    
 IsActive         tinyint(1)       YES      NULL 
"""
def insert_term_term_rel(relId, termId, relName, pvId):
  query = "INSERT INTO NLMDrugTermTerm(DrugTermTermID, DrugTermID1, Relation, DrugTermID2) VALUES(%s,%s,%s,%s)"
  args = (relId, termId, relName, pvId)
  try:
    conx = mariadb.connect(user='root', password='************', database='opioid')
    crsor = conx.cursor()
 #   print("Executing term rel insert")
 #   print(query)
 #   print(args)
    crsor.execute(query, args)
    conx.commit()
  except Error as error:
    print(error)

  finally:
    crsor.close()
    conx.close()
  
def set_max_index():
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
#  print(records)
    for r in records:
      tmpMax = r[0]
      if tmpMax > maxIndex:
        maxIndex = tmpMax
  return maxIndex

# process command line arguments
# if specified, argument is the term 
arglen = len(sys.argv)
if arglen == 2:
    addfile = sys.argv[1]
else:
    print("FORMAT:   python {} [file]".format(sys.argv[0]))
    print("File is of the format:")
    print("  Term|TTY|Principal Variant|Source Code|Source|Class Type")
    sys.exit()

f = open(addfile, "r")

"""
coffeecleaner|SY|sodiumcarbonate|007|DEA|Substance
mryuck|SY|Marijuana Extract|7350|DEA|Substance
mextract|SY|Marijuana Extract|7350|DEA|Substance
deanotinconfig|PV|deanotinconfig|00001|DEA|Substance
mezzzcaline|SY|mescaline|7381|DEA|Substance
"""
for x in f:
  maxIndex = set_max_index()
  
  x = x.strip()
  values = x.split('|')
  termName = values[0]
  termType = values[1]
  pvName = values[2]
  pvExternalId = values[3]
  sourceName = values[4]
  conceptType = values[5]
  
  cnx = mariadb.connect(user='root', password='************', database='opioid')
  cursor = cnx.cursor()  
  
  #find the concept id by termname
  termQuery1 = "select distinct C.DrugConceptID, C.DrugSourceConceptID, T1.DrugTermID, T1.DrugTermName, T2.DrugTermID, T2.DrugTermName, TT.Abbreviation, TS.Name, C.PreferredTermID, T2.DrugExternalID"
  termQuery2 = " from NLMDrugConcept C, NLMDrugTerm T1, NLMDrugTerm T2, NLMDrugTermType TT, NLMDrugAuthoritativeSource TS"
  termQuery3 = " where T2.DrugTermName='" + pvName + "' and C.PreferredTermID = T1.DrugTermID and C.DrugConceptID = T2.DrugConceptID and T2.IsActive = 1"
  termQuery4 = " and T2.DrugTTYID = TT.DrugTTYID and T2.DrugAuthoritativeSourceID = TS.DrugAuthoritativeSourceID"
  termQuery = termQuery1 + termQuery2 + termQuery3 + termQuery4
  
  #find the source
  sourceQuery = "SELECT DrugAuthoritativeSourceID from NLMDrugAuthoritativeSource where Name='" + sourceName + "'"
  
  #find the tty
  ttyQuery = "SELECT DrugTTYID from NLMDrugTermType where Abbreviation='" + termType + "'"
  
  #find the concept type
  conceptTypeQuery = "SELECT DrugConceptTypeID from NLMDrugConceptType where Description='" + conceptType + "'"
  
#  print(termQuery)
  cursor.execute(termQuery)
  termRecords = cursor.fetchall()

#  print(sourceQuery)
  cursor.execute(sourceQuery)
  sourceRecords = cursor.fetchall()
  sourceId = ""
  for s in sourceRecords:
    sourceId = s[0]

#  print(ttyQuery)
  cursor.execute(ttyQuery)
  ttyRecords = cursor.fetchall()
  ttyId = ""
  for t in ttyRecords:
    ttyId = t[0]

# print(conceptTypeQuery)
  cursor.execute(conceptTypeQuery)
  conceptTypeRecords = cursor.fetchall()
  conceptTypeId = ""
  for t in conceptTypeRecords:
    conceptTypeId = t[0]
  
  makeTerm = "true"
  makeConcept = "true"
    
  if len(termRecords) > 0:
    makeConcept = "false"
    for r in termRecords:
      recordConceptId = r[0]
      recordConceptPvId = r[2]
      recordTermName = r[5]
      recordTermType = r[6]
      recordTermSource = r[7]
      print("**** CONCEPT found for {}".format(recordTermName))	  
      if( (termName.lower() == recordTermName.lower()) and (recordTermSource.lower() == sourceName.lower()) and (recordTermType.lower() == termType.lower()) ):
        print("**** TERM found for {}".format(r[5]))
        makeTerm = "false"
  if( makeConcept == "true" and makeTerm == "true" ):
    # def insert_term(DrugTermID, DrugTermName, DrugTTYID, DrugExternalID, DrugAuthoritativeSourceID, IsActive, DrugConceptID):
    insert_term(maxIndex + 1, termName, ttyId, pvExternalId, sourceId, 1, maxIndex + 2)
	#def insert_concept(DrugConceptID, PreferredTermID, DrugAuthoritativeSourceID, DrugConceptTypeID, DrugSourceConceptID, IsActive):	
    insert_concept(maxIndex + 2, maxIndex + 1, sourceId, ttyId, pvExternalId, 1)
    insert_term_term_rel(maxIndex + 3, maxIndex + 1, termType, recordConceptPvId)
    print("Creating term and concept for {}, source {}".format(termName, sourceName))
  elif makeTerm == "true":
    insert_term(maxIndex + 1, termName, ttyId, pvExternalId, sourceId, 1, recordConceptId)
    insert_term_term_rel(maxIndex + 3, maxIndex + 1, termType, recordConceptPvId)
    print("Creating a term for {}, source {}".format(termName, sourceName))
  else:
    print("Term already exists for {}".format(termName))
