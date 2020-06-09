#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 14-Mar-2019 
@author: lee peters 
Read in DEA unique drug list and find the hits in the Washington Death Certificates
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

## read in the DEA drug list
filtered_dea = pd.read_csv('InputLists/2018_DEA_drugs_of_abuse_drugs_unique_drugs.tsv', sep='\t', dtype=str)    

# create a new column containing the hits
filtered_dea['frequency'] = np.nan

## elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_dea.iterrows():
##    if rid % 1000 == 0:   print('Retrieving illicit drugs: rows', rid, '...')
    query = ESutil.initialize_mmquery()
    
    query = ESutil.append_mmquery_term(eachrow['name'], query)
                    
    res = ESutil.es_query_search(query)
    filtered_dea.at[rid, 'frequency'] = res['hits']['total']

filtered_dea.to_csv('Results/filtered_DEA_unique.tsv', sep='\t', index=False)
print('Created Results/filtered_DEA_unique.tsv')
