#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 11-Jan-20111-Jan-20111-Jan-20111-Jan-20111-Jan-20111-Jan-20111-Jan-20111-Jan-20111-Jan-20111-Jan-20111-Jan-2019
@author: lee peters 
"""

import numpy as np
import pandas as pd
import json
import sys
sys.path.append('/nfsvol/mor-nchs/lee/python-code')

import ESutil


### indexing death certificate ICD-10 codes and literal text
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])

termFile = 'test-terms'
debug = 0
# process command line arguments
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
mltcse = ['mltcse'+str(x) for x in range(1,21)]
# list of drug overdose codes
dodicd10 = ['X40','X41','X42','X43','X44','X60','X61','X62','X63','X64','X85','Y10','Y11','Y12','Y13','Y14']
# list of partial drug ICD10 codes - need a substring search for these
drugicd10 = ['T36', 'T37', 'T38', 'T39', 'T4', 'T5']

#################### ElasticSearch retrieval ##################################
filtered_rxnorm = pd.read_csv(termFile, sep='\t', dtype=str)    


filtered_rxnorm['frequency'] = np.nan

## then elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_rxnorm.iterrows():
##    if rid % 1000 == 0:   print('Retrieving illicit drugs: rows', rid, '...')
    if debug == 1: print('Retrieving name: ', eachrow['Search_Term'])
##    rxcui = eachrow['rxcui']
    query = ESutil.initialize_mmquery()
    
    #generic name
    query = ESutil.append_mmquery_term(eachrow['Search_Term'], query)
                    
    res = ESutil.es_query_search(query)

#   keep track of death certificates that have no drug codes
    noDrugCert = []
#   keep track of # of death certificates that have drug codes
    certCount = 0
    for doc in res['hits']['hits']:
        icd10 = []
        src = doc['_source']
#       see if any of the ICD10 codes indicate drug overdose
        for x in mltcse:
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
        else:
            noDrugCert.append(doc['_id'])
    print("%s|%d|%d" % (eachrow['Search_Term'], res['hits']['total'], certCount))
    if debug == 1: print('No drug cert #s:', noDrugCert)
##        print("Cert: %s, ICD10 %s" % (doc['_id'], doc['_source']['mltcse1']))     
#    filtered_rxnorm.at[rid, 'frequency'] = res['hits']['total']


#filtered_rxnorm.to_csv('Data/filtered_MCL.tsv', sep='\t', index=False)
