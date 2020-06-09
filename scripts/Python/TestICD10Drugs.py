#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 11-Jan-2019
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

## read in ICD10 drug list
filtered_rxnorm = pd.read_csv('Data/icd10cm-drugs', sep='\t', dtype=str)    

# create new field to hold hits from Death Certificates
filtered_rxnorm['frequency'] = np.nan

## elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_rxnorm.iterrows():
##    if rid % 1000 == 0:   print('Retrieving illicit drugs: rows', rid, '...')
    print('Retrieving ICD10CM drug names: rows', rid, '...')
    query = ESutil.initialize_mmquery()
    
    #generic name
    query = ESutil.append_mmquery_term(eachrow['Search_Term'], query)
                    
    ## do the search
    res = ESutil.es_query_search(query)
    filtered_rxnorm.at[rid, 'frequency'] = res['hits']['total']

## write new file
filtered_rxnorm.to_csv('Results/filtered_ICD10CM_drugs.tsv', sep='\t', index=False)
