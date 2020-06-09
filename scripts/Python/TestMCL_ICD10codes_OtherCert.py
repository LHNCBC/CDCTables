#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 15-Feb-2019
@author: lee peters 
"""

import numpy as np
import pandas as pd
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')

import ESutil2


### indexing death certificate ICD-10 codes and literal text
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])

termFile = 'test-terms'
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
icd10drugs = pd.read_csv('InputLists/icd10-TCode-drugTable.tsv', sep='\t', dtype=str)
for cnt, row in icd10drugs.iterrows():
    drugName = row['Drug'].lower()
    icd10dict[drugName] = row['T-code']
    
#################### ElasticSearch retrieval ##################################
filtered_rxnorm = pd.read_csv(termFile, sep='\t', dtype=str)    


filtered_rxnorm['frequency'] = np.nan

## then elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_rxnorm.iterrows():
##    if rid % 1000 == 0:   print('Retrieving illicit drugs: rows', rid, '...')
    if debug == 1: print('Retrieving name: ', eachrow['Search_Term'])
    # use the Other death certificates
    query = ESutil2.initialize_mmquery()
    
    #generic name
    query = ESutil2.append_mmquery_term(eachrow['Search_Term'], query)
                    
    res = ESutil2.es_query_search(query)

#   keep track of # of death certificates that have drug codes
    certCount = 0
#   keep track of # of death certificates that match the drug's T-code
    drugCount = 0
    for doc in res['hits']['hits']:
        icd10 = []
        src = doc['_source']
#       see if any of the ICD10 codes indicate drug overdose
        for x in recax:
            if (x in src):
                if (src[x] in dodicd10):
                    icd10.append(src[x])
                else:
                    for s in drugicd10:
                        if (s in src[x]):
                            icd10.append(src[x])
#       if any drug codes found, increment certificate count
        if (len(icd10) > 0):
            if debug == 1:
                print("Cert: %s, ICD10 codes: %s" % (doc['_id'], icd10))
            certCount += 1
#           check to see if the drug has a T-code ICD10 value
            principal = eachrow['principal_variant'].lower()
            if (principal in icd10dict):
                 principalTcode = icd10dict[principal]
                 if (principalTcode in icd10):
                     drugCount += 1  
                 else:
                     if debug == 1: print("T-code %s not found" % (principalTcode))
            else:
                if debug == 1: print("%s not an ICD10 drug" % (principal))
    print("%s|%s|%d|%d|%d" % (eachrow['Search_Term'], eachrow['principal_variant'], res['hits']['total'], certCount, drugCount))
