#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 15-Feb-2019
@author: lee peters 
"""
######
##### Test to see what drug certificates contain "ambiguous" T-codes
#####    ambiguous T-code means more than one drug from that T-code
#####    is named in the drug certificate.
#####
#####    Data is non-Washington state death certificates
#####
#####    Input is a T-code
#####
#####    The program also reads in the ICD10 drug classes file
#####    (InputLists/icd10-TCode-drugTable.tsv) which contains drug names and T-codes
#####
#####    Program will search for all drugs in the T-code class and find 
#####    matching drug certificates between the drugs
######
import numpy as np
import pandas as pd
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')

import ESutil2

### indexing death certificate ICD-10 codes and literal text
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])

termFile = 'test-Tcode'
debug = 0
# process command line arguments - either "debug" or "termfile filename"
arglen = len(sys.argv)
if arglen > 1:
    param1 = sys.argv[1]
    if param1=='debug':
        debug = 1
    elif param1=='termfile':
        if arglen > 2:
            termFile = sys.argv[2]
        else:
            print('ERROR: No file specified on command line - using test-terms');

# list the keys which contain the ICD10 codes
recax = ['recax'+str(x) for x in range(1,20)]
# list of drug overdose codes
dodicd10 = ['X40','X41','X42','X43','X44','X60','X61','X62','X63','X64','X85','Y10','Y11','Y12','Y13','Y14']
# list of partial drug ICD10 codes - need a substring search for these
drugicd10 = ['T36', 'T37', 'T38', 'T39', 'T4', 'T5']

########## read in ICD10 drugs associated with T codes #############
icd10dict = dict()
tcodedict = dict()
icd10drugs = pd.read_csv('InputLists/icd10-TCode-drugTable.tsv', sep='\t', dtype=str)
for cnt, row in icd10drugs.iterrows():
    tCode = row['T-code']
    drugName = row['Drug'].lower()
    icd10dict[drugName] = tCode
#   create a dictionary of T-codes, value is list of drugs
    if tCode not in tcodedict:
        drugNameList = []
    else:
        drugNameList = tcodedict[tCode] 
    drugNameList.append(drugName)
    tcodedict[tCode] = drugNameList
    
#################### ElasticSearch retrieval ##################################
filtered_rxnorm = pd.read_csv(termFile, sep='\t', dtype=str)    

filtered_rxnorm['frequency'] = np.nan

# keep track of all the drug pairs of same code and the certificates found
# key is drug1:drug2, value is list of certificates
drugPairCerts = dict()

# Read the T-Codes
for rid, eachrow in filtered_rxnorm.iterrows():
    if debug == 1: print('Retrieving name: ', eachrow['Search_Term'])
#   dictionary with drugname as key, document list as value
    drugDocDict = dict()

    tCode = eachrow['Search_Term']
#   skip if < 2 drugs in this T-Code class
    if tCode not in tcodedict:
        print ('T-code not found: ', tCode)
    else:
        drugList = tcodedict[tCode]
        if len(drugList) == 1:
            if debug == 1: print('T-code only contains one drug: ', tCode)
        else:
#           list of death certificates that have more than one drug found with same T-code
            ambigDC = []
#           get all drugs in the tCode
            if debug == 1: print('Checking drugs for T-code: ', tCode)
            for name in drugList:
                if debug == 1: print('  searching for ', name)
                query = ESutil2.initialize_mmquery()
                query = ESutil2.append_mmquery_term(name, query)
                res = ESutil2.es_query_search(query)
                docList = []
#               keep track of # of death certificates that have drug codes
                certCount = 0
#               keep track of # of death certificates that match the drug's T-code
                drugCount = 0
                for doc in res['hits']['hits']:
                    icd10 = []
                    src = doc['_source']
#                   see if any of the ICD10 codes indicate drug overdose
                    for x in recax:
                        if (x in src):
                            if (src[x] in dodicd10):
                                icd10.append(src[x])
                            else:
                                for s in drugicd10:
                                    if (s in src[x]):
                                        icd10.append(src[x])
#                   if any drug codes found, increment certificate count
                    if (len(icd10) > 0):
                        if debug == 1:
                            print("Cert: %s, ICD10 codes: %s" % (doc['_id'], icd10))
                        certCount += 1
#                       check to see if T-code is in list
                        if (tCode in icd10):
                            drugCount += 1  
                            docList.append(doc['_id'])
                            drugDocDict[name] = docList
#           see if drug pairs in T-Code class share common death certificates
            if (len(drugDocDict) < 2):
                if debug == 1: print('<2 drugs found with Death Certificates for T-code: ', tCode)
            else:
                if debug == 1: print('T-code: %s, drugs to compare: %s' % (tCode,drugDocDict.keys()))
                for dkey in drugDocDict.keys():
                    dclist = drugDocDict[dkey]
                    for key2 in drugDocDict.keys():
                        if dkey != key2:
                            dclist2 = drugDocDict[key2]
                            for dcnum in dclist:
                                if dcnum in dclist2:
                                    dcstr = dcnum + key2 + dkey
                                    if dcstr not in ambigDC:
                                        print('%s: both %s and %s are in certificate %s' % (tCode,dkey,key2,dcnum))
                                        ambigDC.append(dcnum+dkey+key2)
                                        drugpair = tCode + ":" + dkey + ":" + key2
                                        certList = []
                                        if drugpair in drugPairCerts:
                                            certList = drugPairCerts[drugpair]
                                        certList.append(dcnum)
                                        drugPairCerts[drugpair] = certList 
print('drug-pair|#ofcertificates')
for dgpr in drugPairCerts.keys():
    certList = drugPairCerts[dgpr]
    print ('%s|%d' % (dgpr,len(certList)))
