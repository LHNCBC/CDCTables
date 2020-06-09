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
# process command line arguments - searchterm 
arglen = len(sys.argv)
if arglen > 1:
    term = sys.argv[1]
    if arglen > 2:
        val = sys.argv[2]
        if val.lower() == 'debug':
            debug = 1
        if val.lower() == 'debug2':
            debug = 2
else:
    print('ERROR: No term specified on command line - must specify search term');
    sys.exit()

# list the keys which contain the ICD10 codes
recax = ['recax'+str(x) for x in range(1,20)]
# list of drug overdose codes
dodicd10 = ['X40','X41','X42','X43','X44','X60','X61','X62','X63','X64','X85','Y10','Y11','Y12','Y13','Y14']
# list of partial drug ICD10 codes - need a substring search for these
drugicd10 = ['T36', 'T37', 'T38', 'T39', 'T4', 'T5']
# drug induced deaths
druginducedicd10 = ['D521','D590','D592','D611','D642','E064','E160','E231','E242','E273','E661','F111','F112','F113','F114','F115','F116','F117','F118','F119', 'F121','F122','F123','F124','F125','F127','F128','F129','F131','F132','F133','F134','F135','F137','F138','F139', 'F141','F142','F143','F144','F145','F147','F148','F149','F151','F152','F153','F154','F155','F157','F158','F159', 'F161','F162','F163','F164','F165','F167','F168','F169','F171','F172','F173','F174','F175','F177','F178','F179', 'F181','F182','F183','F184','F185','F187','F188','F189','F191','F192','F193','F194','F195','F197','F198','F199','G211','G240','G251','G254','G256','G444','G620','G720','I952','J702','J703','J704','K85.3','L105','L270','L271','M102','M320','M804','M814','M835','R502','R781','R782','R783','R784','R785''X40','X41','X42','X43','X44','X60','X61','X62','X63','X64','X85','Y10','Y11','Y12','Y13','Y14']
# alcohol induced deaths
alcoholicd10 = ['E244','F10','G312','G621','G721','I426','K292','K70','K852','K860','R780','X45','X65','Y15']

########## read in ICD10 drugs associated with T codes #############
icd10dict = dict()
icd10drugs = pd.read_csv('InputLists/icd10-TCode-drugTable.tsv', sep='\t', dtype=str)
for cnt, row in icd10drugs.iterrows():
    drugName = row['Drug'].lower()
    icd10dict[drugName] = row['T-code']

########## read in synonyms file (contains also brand names) ###########
synonymdict = dict()
synonymrecs = pd.read_csv('InputLists/synonyms.tsv', sep='\t', dtype=str)
for cnt, row in synonymrecs.iterrows():
    synonym = row['synonym'].lower()
    wordlist = []
    if synonym in synonymdict:
        wordlist = synonymdict[synonym]
    wordlist.append(row['word'].lower())
    synonymdict[synonym] = wordlist;
        
#    synonymdict[synonym] = row['word'].lower()

########################################################################            
# TBD - need to look up term in database to find if its a synonym or brand name
#       to get the proper principle name
principal = term.lower()
principalTcode=[]
preferredTermList = []
if term.lower() in synonymdict:
    preferredTermList = synonymdict[term.lower()]
    if debug > 0: print("%s is a synonym of %s" % (term, preferredTermList))
else:
    preferredTermList.append(term.lower())
for x in preferredTermList:
    if x in icd10dict:
        principalTcode.append(icd10dict[x])
        if debug > 0: print("T-code %s found for %s" % (icd10dict[x], term))

#################### ElasticSearch retrieval ##################################
## then elasticsearch will retrieve deathcert based on 'query'
## death cert that has the mention of any drug term, will be retrieved
# use the Other death certificates
query = ESutil2.initialize_mmquery()
    
query = ESutil2.append_mmquery_term(term, query)
res = ESutil2.es_query_search(query)

# death certificates count containing drug overdose codes
overdoseCount = 0
# death certificates count containing the drug's T-code
drugCount = 0
# drug induced death counter
inducedCount = 0
# alcohol induced death counter
alcoholCount = 0
for doc in res['hits']['hits']:
    alcohol = []
    overdose = []
    drugInduced = []
    src = doc['_source']
#   see if any of the ICD10 codes indicate drug overdose
    for x in recax:
        if (x in src):
            if debug > 1:
                print("Checking %s " % (src[x]))
#           check if it is an overdose code
            if (src[x] in dodicd10):
                overdose.append(src[x])
                if debug > 1:
                    print("Drug overdose %s found in %s" % (src[x],x))
#           check if it is a drug T-code
            else:
                for s in drugicd10:
                    if (s in src[x]):
                        overdose.append(src[x])
#                        if debug == 1:
#                            print("Drug %s found in %s" % (src[x],x))
#
#           check if it is a drug induced code
            if (src[x] in druginducedicd10):
                drugInduced.append(src[x])
                if debug > 1:
                    print("Drug induced death code found: %s" % (src[x]))
#           check if death is alcohol induced
            if (src[x] in alcoholicd10):
                alcohol.append(src[x])
                if debug > 1:
                    print("Alcohol induced death code found: %s" % (src[x]))
#   if drug induced death code(s) found, increment counter
    if (len(drugInduced) > 0):
        inducedCount += 1
    if (len(alcohol) > 0):
        alcoholCount += 1
#   if any overdose codes found increment count, and check if drug was found
    if (len(overdose) > 0):
        overdoseCount += 1
#       check to see if the drug has a T-code ICD10 value
#        if len(principalTcode) > 0:
        for x in principalTcode:
            if x in overdose:
                drugCount += 1  
                if debug > 1: print("T-code %s found for %s" % (x, term))
    if debug > 0:
        print("Cert: %s, overdose codes:%s, drug-induced codes:%s, alcohol codes:%s" % (doc['_id'], overdose, drugInduced, alcohol))
print("%s|%s|%d|%d|%d|%d|%d" % (term, principalTcode, res['hits']['total'], inducedCount, overdoseCount, drugCount, alcoholCount))
