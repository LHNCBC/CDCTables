#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 15-Feb-2019
@author: lee peters 
"""
######
#####    Program to read in the ICD10 T-code data and find the synonyms in each class
#####    Use the Manually Curated List to determine the synonyms
#####
#####    Input is a T-code
#####
#####    The program also reads in the ICD10 drug classes file
#####    (InputLists/icd10-TCode-drugTable.tsv) which contains drug names and T-codes
#####
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
    
########## read in Manually curated list ########################
# dictionary contain variant as key, principal variants as value
mcldict = dict()
filtered_mcl = pd.read_csv('InputLists/MCL-terms', sep='\t', dtype=str)
for idx, mclrow in filtered_mcl.iterrows():
    variant = mclrow['Search_Term'].lower()
    principal = mclrow['principal_variant'].lower()
    if variant in mcldict:
        princ = mcldict[variant]
        print('variant %s has multiple principals: %s %s (ignored)' % (variant,princ,principal))
    else:
        mcldict[variant]=principal

filtered_rxnorm = pd.read_csv(termFile, sep='\t', dtype=str)    

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
#           get all drugs in the tCode
            if debug == 1: print('Checking drugs for T-code: ', tCode)
            principaldict = dict()
            for name in drugList:
#               get the principal for the name
                if name in mcldict:
                    prin = mcldict[name]
                    if prin in principaldict:
                        syn = principaldict[prin]
                        print('%s: synonyms: %s, %s, principal: %s' % (tCode, syn, name, prin))
                    else:
                        principaldict[prin] = name
                else:
                    if debug == 1: print('%s: Name not found in MCL - %s' % (tCode,name))
