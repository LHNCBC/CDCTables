#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 15-Feb-2019
@author: lee peters 
"""
# generate statistics on one or more lists of terms
# this program uses the following files:
# 1) ICD10 T-codes associated with drugs/drug classes in InputLists/icd10-TCode-drugTable.tsv
# 2) synonym file in InputLists/synonyms.tsv
# 3) misspelling file in InputLists/misspell.tsv
# 4) 
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
fileList = []
spell = 0
debug = 0
printcerts = 0
printterms = 0
# process command line arguments - list of files or debug indicator 
arglen = len(sys.argv)
argpos = 1
while (arglen > argpos):
    argval = sys.argv[argpos]
    if argval == '-debug':
        debug = 1
    elif argval == '-debug2':
        debug = 2
    elif argval == '-spell':
        spell = 1
    elif argval == '-certs':
        printcerts = 1
    elif argval == '-terms':
        printterms = 1
    else:
        fileList.append(argval)
    argpos = argpos + 1

if len(fileList) < 1:
    print('ERROR: No term file specified on command line');
    print('FORMAT: python {} termfile1 [termfile2 ... termfileN] [-debug|-debug2][-spell][-certs][-terms]'.format(sys.argv[0]))
    sys.exit()

def getFreqTcode( tcodeDict ):
    if len(tcodeDict) < 1:
        return ''
    return sorted(tcodeDict.items() , reverse=True, key=lambda x: x[1])
    
# list the keys which contain the ICD10 codes
recax = ['recax'+str(x) for x in range(1,20)]
# NCHS list of drug overdose codes
dodicd10 = ['X40','X41','X42','X43','X44','X60','X61','X62','X63','X64','X85','Y10','Y11','Y12','Y13','Y14']
# NCHS list of (partial) drug ICD10 codes - need a substring search for these
drugicd10 = ['T36', 'T37', 'T38', 'T39', 'T4', 'T5']
# NCHS drug induced death codes
druginducedicd10 = ['D521','D590','D592','D611','D642','E064','E160','E231','E242','E273','E661','F111','F112','F113','F114','F115','F116','F117','F118','F119', 'F121','F122','F123','F124','F125','F127','F128','F129','F131','F132','F133','F134','F135','F137','F138','F139', 'F141','F142','F143','F144','F145','F147','F148','F149','F151','F152','F153','F154','F155','F157','F158','F159', 'F161','F162','F163','F164','F165','F167','F168','F169','F171','F172','F173','F174','F175','F177','F178','F179', 'F181','F182','F183','F184','F185','F187','F188','F189','F191','F192','F193','F194','F195','F197','F198','F199','G211','G240','G251','G254','G256','G444','G620','G720','I952','J702','J703','J704','K85.3','L105','L270','L271','M102','M320','M804','M814','M835','R502','R781','R782','R783','R784','R785''X40','X41','X42','X43','X44','X60','X61','X62','X63','X64','X85','Y10','Y11','Y12','Y13','Y14']
# NCHS alcohol induced death codes
alcoholicd10 = ['E244','F10','G312','G621','G721','I426','K292','K70','K852','K860','R780','X45','X65','Y15']
# possible codes involving drugs (if not included in drug induced death codes)
othericd10 = ['A800','D683','E032','F55','H263','H406','I427','L233','L244','L251','L278','L279','L432','L560','L561','L640','M342','N140','N141','N142','O355','P040','P041','P044','P046','P047','P048','P049','P584','P93','P961','P962','Q861','Q862','R786','R788','R789','R825','R832','R833','R842','R843','R852','R853','R862','R863','R872','R873','R892','R893','T655','T658','T659','T880','T881','T886','T887','T96','T97','X49','X69','X89','X90','Y19','Y40','Y41','Y42','Y43','Y44','Y45','Y46','Y47','Y49','Y50','Y51','Y52','Y53','Y54','Y55','Y56','Y57','Y58','Y59','Y880','Z036','Z272','Z910','Z921','Z922']

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
        
######### read in misspelling file ###################
misspelldict = dict()
if spell == 1:
    misspellrecs = pd.read_csv('InputLists/misspell.tsv', sep='\t', dtype=str)
    for cnt, row in misspellrecs.iterrows():
        misspell = row['misspelled'].lower()
        misspelldict[misspell] = row['word'].lower()

# print output header (did = drug induced deaths, dod = drug overdose deaths, 
#                      T-hit = drug TCode mentions, alcd - alcohol induced deaths)
##print("Name|TCode|hits|did|dod|oth|T-hit|alcd")
print("Name|TCode|hits|uniq|did|dod|T-hit|alcd|maxT")

filedict = dict()
######### read in term file ##########################
for termFile in fileList:
    filtered_terms = pd.read_csv(termFile, sep='\t', dtype=str)

    ## Structures for preferred Terms
    prefertermdict = dict()

    for rid, eachrow in filtered_terms.iterrows():

        term = eachrow['Search_Term']
        #################### ElasticSearch retrieval ##################################
        ## then elasticsearch will retrieve deathcert based on 'query'
        ## death cert that has the mention of any drug term, will be retrieved
        # use the Other death certificates
        query = ESutil2.initialize_mmquery()
    
        query = ESutil2.append_mmquery_term(term, query)
        res = ESutil2.es_query_search(query)

        ########################################################################
        # look up term to see if it is misspelled
        #
        realWord = term.lower()
        if realWord in misspelldict:
            realWord = misspelldict[realWord]
            if debug > 0: print("%s is a misspelling of %s" % (term, realWord))
        ########################################################################            
        # look up term in synonyms database to find if its a synonym or brand name
        #       to get the proper principle name
        # NOTE: if a multi-ingredient brand name (e.g. Caduet), can produce more
        #        than one preferred Term
        principalTcode=[]
        preferredTermList = []
        if realWord in synonymdict:
            preferredTermList = synonymdict[realWord]
            if debug > 0: print("%s is a synonym of %s" % (realWord, preferredTermList))
        else:
            preferredTermList.append(realWord)
        for x in preferredTermList:
            if x in icd10dict:
                principalTcode.append(icd10dict[x])
                if debug > 0: print("T-code %s found for %s" % (icd10dict[x], term))

        # list of death certificates with term hits
        certList = [] 
        # death certificates count containing drug overdose codes
        overdoseCount = 0
        # death certificates count containing the drug's T-code
        drugCount = 0
        # drug induced death counter
        inducedCount = 0
        # possible drug codes counter
        otherCount = 0
        # alcohol induced death counter
        alcoholCount = 0
        # T-code counter - key is T-Code, value is # of references
        tCodeCount = dict()
        ################################# term processing #############################
        processed_hits = 0
        total_hits = res['hits']['total']
        returned_hits = len(res['hits']['hits'])
        # note: returned hits could be less that total_hits (if total_hits > 10000)
        while processed_hits < total_hits:
          #  For each hit, calculate the statistics
          for doc in res['hits']['hits']:
            processed_hits += 1
            alcohol = []
            overdose = []
            drugInduced = []
            other = []
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
                                tcnt = 0
                                if src[x] in tCodeCount:
                                    tcnt = tCodeCount[src[x]]
                                tcnt += 1
                                tCodeCount[src[x]] = tcnt
        #
        #           check if it is a drug induced code
                    if (src[x] in druginducedicd10):
                        drugInduced.append(src[x])
                        if debug > 1:
                            print("Drug induced death code found: %s" % (src[x]))
        #           check if it is code that potentially involves drugs
                    if (src[x] in othericd10):
                        other.append(src[x])
                        if debug > 1:
                            print("Possible drug induced death code found: %s" % (src[x]))
                         
        #           check if death is alcohol induced
                    if (src[x] in alcoholicd10):
                        alcohol.append(src[x])
                        if debug > 1:
                            print("Alcohol induced death code found: %s" % (src[x]))
        #   if drug induced death code(s) found, increment counter
            if (len(drugInduced) > 0):
                inducedCount += 1
            elif (len(other) > 0):
                inducedCount += 1
        #    if (len(other) > 0):
                otherCount += 1
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
            certList.append(doc['_id'])
          ## end for ##
          # check if more data to retrieve via scrolling
          if processed_hits < total_hits:
              scrollID = res['_scroll_id']
              res = ESutil2.es_query_scroll(scrollID) 
              if (len(res['hits']['hits']) == 0):
                  print("ABORTING scroll for %s -returned 0 results" % (term))
                  processed_hits = total_hits

        ## display the highest T-code count (must have at least 2 dod occurrences)
        maxTcode = ''
        if (overdoseCount > 1):
            listofTuples = getFreqTcode(tCodeCount)
            if len(listofTuples) > 0:
                elem=listofTuples[0] 
                maxTcode = elem[0]
        ## format the T-code(s) for display
        tCodeStr = ';'.join(principalTcode)
        ########################### end of term processing #############################
        
        #########  calculate principal variant statistics ##################################
        if debug > 0:
            print("%s|%s|%d|%d|%d|%d|%d|%s" % (term, tCodeStr, res['hits']['total'], inducedCount, overdoseCount, drugCount, alcoholCount,maxTcode))
        if printcerts == 1:
            print("certs:%s" % (certList))
        termhits = res['hits']['total']
        for t in preferredTermList:
            hitdict = dict()
            if t in prefertermdict:
                hitdict = prefertermdict[t] 
                #  add to existing totals
                if termhits > 0:
                    hitdict["HITS"] += termhits
                    hitdict["DID"] += inducedCount
                    hitdict["DOD"] += overdoseCount
                    hitdict["T-HIT"] += drugCount
                    hitdict["ALCH"] += alcoholCount
                    final_list = list(set(hitdict["CERTS"]) | set(certList))
                    hitdict["CERTS"] = final_list
                    hitdict["TERMS"].append(term)
                    if len(hitdict["TMAX"]) == 0:
                        hitdict["TMAX"] = maxTcode
            else:
                hitdict["HITS"] = termhits
                hitdict["DID"] = inducedCount
                hitdict["DOD"] = overdoseCount
                hitdict["T-HIT"] = drugCount
                hitdict["TMAX"] = maxTcode
                hitdict["ALCH"] = alcoholCount
                hitdict["TCODE"] = ''
                if t in icd10dict:
                    hitdict["TCODE"] = icd10dict[t]
                hitdict["CERTS"] = certList
                if termhits > 0:
                    hitdict["TERMS"] = [ term ]
                else:
                    hitdict["TERMS"] = []
            # put hit dictionary in preferred term dictionary
            prefertermdict[t] = hitdict
    ################### print out file statistics #########################
    for pterm in prefertermdict:
        hitdict = prefertermdict[pterm]
        certs = hitdict["CERTS"]
        if printterms == 1:
            termStr=';'.join(hitdict["TERMS"])
            print("%s|%d|%s" % (pterm,hitdict["HITS"],termStr))
        else:
            print("%s|%s|%d|%d|%d|%d|%d|%d|%s" % (pterm, hitdict["TCODE"], hitdict["HITS"], len(certs), hitdict["DID"], hitdict["DOD"], hitdict["T-HIT"], hitdict["ALCH"], hitdict["TMAX"]))

    filedict[termFile] = prefertermdict
