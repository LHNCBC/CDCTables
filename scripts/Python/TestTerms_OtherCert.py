#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 11-Jan-2019
@author: lee peters 
"""
# Test the terms on the Other Death Certificates (non-Washington state)
#

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
# process command line arguments
# if specified, argument is the term file
# the terms file MUST have "Search_Term" in the first column of the first line
arglen = len(sys.argv)
if arglen > 1:
    termFile = sys.argv[1]

#################### ElasticSearch retrieval ##################################
filtered_rxnorm = pd.read_csv(termFile, sep='\t', dtype=str)    


filtered_rxnorm['frequency'] = np.nan

## then elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_rxnorm.iterrows():
##    if rid % 1000 == 0:   print('Retrieving illicit drugs: rows', rid, '...')
    query = ESutil2.initialize_mmquery()
    if debug == 1: print('Retrieving name: ', eachrow['Search_Term'])
    
    #generic name
    query = ESutil2.append_mmquery_term(eachrow['Search_Term'], query)
                    
    res = ESutil2.es_query_search(query)

    print("%s|%d" % (eachrow['Search_Term'], res['hits']['total']))


#filtered_rxnorm.to_csv('Data/filtered_MCL.tsv', sep='\t', index=False)
