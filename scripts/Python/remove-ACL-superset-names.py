#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on 05-Feb-2020
@author: lee peters 
"""
# Remove all the "superset" names to create list for searching
#
# Example:  'morphine sulfate' is a superset name of morphine 
#
# Input - a file containing on each line:
#   variant|principal

import sys

# import mysql.connector as mariadb

# process command line arguments
# if specified, argument is the file name
arglen = len(sys.argv)
fname = 'file_variants'
if arglen < 2:
    print("FORMAT: python {} filename".format(sys.argv[0]))
    sys.exit()
fname = sys.argv[1]

d = {}
vlist = []
# read in file
f = open(fname, 'r')
lines = f.readlines()
for line in lines:
    line2 = line.rstrip()
    (val, key) = line2.split("|")
    if key in d:
        vlist = d[key]
    else:
        vlist = []
    vlist.append(val.lower())
    d[key] = vlist
f.close()
# for each principal variant, sort list of variants by length
for prin in d:
    outList = []
    vlist = d[prin]
    vlistSorted = sorted(vlist,key=len) 
#    print("{}: {}".format(prin,vlistSorted))
    # for each term see if it is a superset term
    for term in vlistSorted:
        # split the term into words 
        wordList = term.split()
        if len(wordList) == 1:
            print("{}|{}".format(term,prin))
            outList.append(term)
        else:
            # check if word is already there
            strbuf = ""
            eliminate = 0
            for word in wordList:
                if len(strbuf) > 0:
                    tmpstr = strbuf
                    strbuf = tmpstr + " " + word
                else:
                    strbuf = word
                # check if this word is being searched for - for cases where word is embedded in a phrase
                if word in outList:
                    eliminate=1
                if strbuf in outList:
                    eliminate=1
#                    print("{} should be eliminated from {}".format(term,prin))
            if eliminate == 0:
                outList.append(term)
                print("{}|{}".format(term,prin))
