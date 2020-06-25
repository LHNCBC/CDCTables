#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Nov 28 15:09:58 2018
@author: lee peters 
Read in the illicit drug list and find the hits in the Washington Death Certificates
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

## Read in illicit drug list
filtered_rxnorm = pd.read_csv('InputLists/illicitDrugs.tsv', sep='\t', dtype=str)    

## add new column for frequency counts
filtered_rxnorm['frequency'] = np.nan

## elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_rxnorm.iterrows():
##    if rid % 1000 == 0:   print('Retrieving illicit drugs: rows', rid, '...')
    print('Retrieving illicit drugs: rows', rid, '...')
    query = ESutil.initialize_mmquery()
    
    #generic name
    query = ESutil.append_mmquery_term(eachrow['Base Description'], query)
                    
    # do the search for the Washington Death Certificates
    res = ESutil.es_query_search(query)
    filtered_rxnorm.at[rid, 'frequency'] = res['hits']['total']

## write results
filtered_rxnorm.to_csv('Results/filtered_illicitDrugs.tsv', sep='\t', index=False)
