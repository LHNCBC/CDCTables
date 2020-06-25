#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Nov 28 15:09:58 2018

@author: soonjye
"""

import numpy as np
import pandas as pd
import json
import sys
###sys.path.append('/home/soonjye/Documents/Death/MedInfo/Code')
sys.path.append('/nfsvol/mor-nchs/lee/python-code')

import ESutil



### indexing death certificate ICD-10 codes and literal text
from elasticsearch import Elasticsearch
es = Elasticsearch([{'host': 'localhost', 'port': 9200}])

years = ['2003', '2004', '2005', '2006','2007','2008','2009','2010','2011','2012','2013','2014','2015']
mltcse = ['mltcse'+str(x) for x in range(1,21)]
for year in years:
    deathlit = pd.read_csv('DeathCert/DeathLit/DeathLitF'+year+'.csv', delimiter=',')
    deathstat = pd.read_csv('DeathCert/DeathStat/DeathStatF'+year+'.csv', delimiter=',')
    print('Year:', year, 'row(lit)=', deathlit.shape[0], 'row(stat)=', deathstat.shape[0], 'matched=', sum(deathstat['certno'] == deathstat.certno))

    for rowid, litrow in deathlit.iterrows():
        if rowid % 1000 == 0:   print('Year', year, '- no', rowid)
        
        parsed = dict()
        for x in list(deathlit)[1:]:
            if not( pd.isnull(litrow[x]) ):
                parsed[x] = litrow[x]
        
        if year != '2013' or litrow['CERT-NUM'] in list(deathstat['certno']):
            stat = deathstat[ deathstat['certno'] == litrow['CERT-NUM']]
            stat = stat.iloc[0,:]
            for x in mltcse:
                if not pd.isnull(stat[x]):
                    parsed[x] = stat[x]
                    
        es.index(index = 'death', doc_type='cert', id=litrow['CERT-NUM'], body=parsed)
        
## year 2016 death cert has different metadata
deathlit = pd.read_csv('DeathCert/DeathLit/DeathLitF2016.csv', delimiter=',')
deathstat = pd.read_csv('DeathCert/DeathStat/DeathStatF2016.csv', delimiter=',')
acme = ['ACME Cause Category ' + str(x) for x in range(1,21)]
print('Year:', year, 'row(lit)=', deathlit.shape[0], 'row(stat)=', deathstat.shape[0], 'matched=', sum(deathstat['State File Number'] == deathlit['CERT-NUM']))

for rowid, litrow in deathlit.iterrows():
    if rowid % 1000 == 0:   print('Year', year, '- no', rowid)
    
    parsed = dict()
    for x in list(deathlit)[1:]:
        if not( pd.isnull(litrow[x]) ):
            parsed[x] = litrow[x]
    
    stat = deathstat[ deathstat['State File Number'] == litrow['CERT-NUM']]
    stat = stat.iloc[0,:]
    for x in zip(acme, mltcse):
        if not pd.isnull(stat[x[0]]):
            parsed[x[1]] = stat[x[0]]
                
    es.index(index = 'death', doc_type='cert', id=litrow['CERT-NUM'], body=parsed)










#################### ElasticSearch retrieval ##################################
filtered_rxnorm = pd.read_csv('Data/filtered_RxNorm3.tsv', sep='\t', dtype=str)    
with open('Data/inpinbn.json') as fi:   inpinbn = json.load(fi)


filtered_rxnorm['frequency'] = np.nan

## for every drug entity, 'query' will combine every drug terms (includes PIN, BN and their misspelings, synonyms)
## then elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
for rid, eachrow in filtered_rxnorm.iterrows():
    if rid % 1000 == 0:   print('Retrieving deathcert: rows', rid, '...')

    rxcui = eachrow['rxcui']
    query = ESutil.initialize_mmquery()
    
    #generic name
    query = ESutil.append_mmquery_term(eachrow['name'], query)
    #generic name misspelling
    if  rxcui in inpinbn.keys()  and  'MSP' in inpinbn[rxcui].keys():
        for eachmsp in inpinbn[rxcui]['MSP']:
            query = ESutil.append_mmquery_term(eachmsp, query)
    #synonym
    if rxcui in inpinbn.keys()  and  'SYN' in inpinbn[rxcui].keys():
        for eachsyn in inpinbn[rxcui]['SYN']:
            query = ESutil.append_mmquery_term(eachsyn, query)
   
    
    #####include PIN if the rxcui is IN
    if  eachrow['tty'] == 'IN'  and  rxcui in inpinbn.keys() and 'PIN' in inpinbn[rxcui].keys():
        pins = inpinbn[rxcui]['PIN']
        for eachpin in pins:
            if eachpin not in list(filtered_rxnorm.rxcui):  continue
            
        # PIN_name
            query = ESutil.append_mmquery_term(inpinbn[eachpin]['name'], query)
        
            # PIN_misspelling
            if 'MSP' in inpinbn[eachpin].keys():
                for eachpin_msp in inpinbn[eachpin]['MSP']:
                    query = ESutil.append_mmquery_term(eachpin_msp, query)
                
            # PIN_synonym
            if 'SYN' in inpinbn[eachpin].keys():
                for eachpin_syn in inpinbn[eachpin]['SYN']:
                    query = ESutil.append_mmquery_term(eachpin_syn, query)
    
    
    #####include BN if the rxcui is IN/PIN and has BN
    if  eachrow['tty'] != 'BN'  and  rxcui in inpinbn.keys()  and  'BN' in inpinbn[rxcui].keys():
        bns = inpinbn[rxcui]['BN']
        for eachbn in bns:
            if eachbn not in list(filtered_rxnorm.rxcui):  continue
            # BN_name
            query = ESutil.append_mmquery_term(inpinbn[eachbn]['name'], query)
            
            # BN_misspelling
            if 'MSP' in inpinbn[eachpin].keys():
                for eachbn_msp in inpinbn[eachbn]['BN']:
                    query = ESutil.append_mmquery_term(eachbn_msp, query)
                    
    res = ESutil.es_query_search(query)
    filtered_rxnorm.at[rid, 'frequency'] = res['hits']['total']


filtered_rxnorm.to_csv('Data/filtered_RxNorm4.tsv', sep='\t', index=False)
