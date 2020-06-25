DROP TABLE IF EXISTS NLMDrugAuthoritativeSource;
CREATE TABLE NLMDrugAuthoritativeSource (
	DrugAuthoritativeSourceID INT unsigned not null primary key,
	Name varchar(50) not null,
	Description varchar(100),
	CreationUserID char(4),
	CreationDate datetime,
	UpdatedUserID char(5),
	UpdatedDate datetime,
	IsActive tinyint(1)
) ENGINE=MyISAM CHARACTER SET utf8;

CREATE INDEX S_NAME ON NLMDrugAuthoritativeSource(Name);
CREATE INDEX S_DESCRIPTION ON NLMDrugAuthoritativeSource(Description);
load data local infile 'authoritative-source.txt' into table NLMDrugAuthoritativeSource fields terminated by '|' lines terminated by '\n';

DROP TABLE IF EXISTS NLMDrugConceptType;
CREATE TABLE NLMDrugConceptType (
	DrugConceptTypeID INT unsigned not null primary key,
	Description varchar(50),
	CreationDateTime datetime,
	CreationUserID char(4),
        UpdatedDateTime datetime,
	UpdatedUserID char(4),
	IsActive tinyint(1)
) ENGINE=MyISAM CHARACTER SET utf8;

CREATE INDEX D_DESCRIPTION ON NLMDrugConceptType(Description);
load data local infile 'concept-type.txt' into table NLMDrugConceptType fields terminated by '|' lines terminated by '\n';

DROP TABLE IF EXISTS NLMDrugTermType;
CREATE TABLE NLMDrugTermType (
	DrugTTYID INT unsigned not null primary key,
	Abbreviation char(4),
	Description char(50),
	CreationUserID char(4),
	CreationDate datetime,
	UpdatedUserID char(5),
	UpdatedDate datetime,
	IsActive tinyint(1)
) ENGINE=MyISAM CHARACTER SET utf8;

CREATE INDEX TT_ABBREVIATION ON NLMDrugTermType(Abbreviation);
CREATE INDEX TT_DESCRIPTION ON NLMDrugTermType(Description);
load data local infile 'term-type.txt' into table NLMDrugTermType fields terminated by '|' lines terminated by '\n';

DROP TABLE IF EXISTS NLMDrugTerm;
CREATE TABLE NLMDrugTerm (
	DrugTermID INT unsigned not null primary key,
	DrugTermName varchar(500) NOT NULL,
	DrugTTYID smallint,
	DrugExternalID varchar(32),
	DrugAuthoritativeSourceID smallint,
	CreationUserID char(4),
	CreationDate datetime,
	UpdatedUserID char(5),
	UpdatedDate datetime,
	IsActive tinyint(1),
	DrugConceptID bigint
) ENGINE=MyISAM CHARACTER SET utf8;

CREATE INDEX DT_DRUGTERMNAME ON NLMDrugTerm(DrugTermName);
load data local infile 'term.txt' into table NLMDrugTerm fields terminated by '|' lines terminated by '\n';

DROP TABLE IF EXISTS NLMDrugConcept;
CREATE TABLE NLMDrugConcept (
	DrugConceptID INT unsigned not null primary key,
	PreferredTermID bigint NOT NULL,
	DrugAuthoritativeSourceID smallint,
	DrugConceptTypeID bigint,
	DrugSourceConceptID varchar(32),
	CreationDate datetime,
	CreationUserID char(4),
	UpdatedDate datetime,
	UpdateUserID char(4),
	IsActive tinyint(1)
) ENGINE=MyISAM CHARACTER SET utf8;

load data local infile 'concept.txt' into table NLMDrugConcept fields terminated by '|' lines terminated by '\n';

DROP TABLE IF EXISTS NLMDrugTermTerm;
CREATE TABLE NLMDrugTermTerm (
        DrugTermTermID INT unsigned not null primary key,
	DrugTermID1 bigint,
	Relation char(50),
	DrugTermID2 bigint,
	CreationUserID char(4),
	CreationDateTime datetime,
	UpdatedUserID char(4),
	UpdatedDateTime datetime,
	IsActive tinyint(1)
) ENGINE=MyISAM CHARACTER SET utf8;

load data local infile 'term-term.txt' into table NLMDrugTermTerm fields terminated by '|' lines terminated by '\n';

DROP TABLE IF EXISTS NLMDrugConcepttoConcept;
CREATE TABLE NLMDrugConcepttoConcept (
	DrugConceptConceptID INT unsigned not null primary key,
	DrugConceptID1 bigint not null,
	Relation char(50),
	DrugConceptID2 bigint not null,
	CreationUserID char(4),
	CreationDateTime datetime,
	UpdatedUserID char(4),
	UpdatedDateTime datetime,
	IsActive tinyint(1)
) ENGINE=MyISAM CHARACTER SET utf8;

load data local infile 'concept-concept.txt' into table NLMDrugConcepttoConcept fields terminated by '|' lines terminated by '\n';
