CREATE TABLE ACL_SID(
	ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 100) NOT NULL PRIMARY KEY,
	PRINCIPAL BOOLEAN NOT NULL,
	SID VARCHAR_IGNORECASE(100) NOT NULL,
	CONSTRAINT UNIQUE_UK_1 UNIQUE(SID,PRINCIPAL));

CREATE TABLE ACL_CLASS(
	ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 100) NOT NULL PRIMARY KEY,
	CLASS VARCHAR_IGNORECASE(100) NOT NULL,
	CONSTRAINT UNIQUE_UK_2 UNIQUE(CLASS));

CREATE TABLE ACL_OBJECT_IDENTITY(
	ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 100) NOT NULL PRIMARY KEY,
	OBJECT_ID_CLASS BIGINT NOT NULL,
	OBJECT_ID_IDENTITY BIGINT NOT NULL,
	PARENT_OBJECT BIGINT,
	OWNER_SID BIGINT,
	ENTRIES_INHERITING BOOLEAN NOT NULL,
	CONSTRAINT UNIQUE_UK_3 UNIQUE(OBJECT_ID_CLASS,OBJECT_ID_IDENTITY),
	CONSTRAINT FOREIGN_FK_1 FOREIGN KEY(PARENT_OBJECT)REFERENCES ACL_OBJECT_IDENTITY(ID),
	CONSTRAINT FOREIGN_FK_2 FOREIGN KEY(OBJECT_ID_CLASS)REFERENCES ACL_CLASS(ID),
	CONSTRAINT FOREIGN_FK_3 FOREIGN KEY(OWNER_SID)REFERENCES ACL_SID(ID));

CREATE TABLE ACL_ENTRY(
	ID BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 100) NOT NULL PRIMARY KEY,
	ACL_OBJECT_IDENTITY BIGINT NOT NULL,
	ACE_ORDER INT NOT NULL,
	SID BIGINT NOT NULL,
	MASK INTEGER NOT NULL,
	GRANTING BOOLEAN NOT NULL,
	AUDIT_SUCCESS BOOLEAN NOT NULL,
	AUDIT_FAILURE BOOLEAN NOT NULL,
	CONSTRAINT UNIQUE_UK_4 UNIQUE(ACL_OBJECT_IDENTITY,ACE_ORDER),
	CONSTRAINT FOREIGN_FK_4 FOREIGN KEY(ACL_OBJECT_IDENTITY) REFERENCES ACL_OBJECT_IDENTITY(ID),
	CONSTRAINT FOREIGN_FK_5 FOREIGN KEY(SID) REFERENCES ACL_SID(ID));


CREATE TABLE users(
	USERNAME VARCHAR_IGNORECASE(50) NOT NULL PRIMARY KEY,
    PASSWORD VARCHAR_IGNORECASE(50) NOT NULL,ENABLED BOOLEAN NOT NULL);

CREATE TABLE authorities(
	USERNAME VARCHAR_IGNORECASE(50) NOT NULL,
	AUTHORITY VARCHAR_IGNORECASE(50) NOT NULL,
	CONSTRAINT FK_AUTHORITIES_USERS FOREIGN KEY(USERNAME) REFERENCES users(USERNAME));
	
CREATE UNIQUE INDEX IX_AUTH_USERNAME ON authorities(USERNAME,AUTHORITY);

CREATE TABLE contacts(
	ID BIGINT NOT NULL PRIMARY KEY, 
	CONTACT_NAME VARCHAR_IGNORECASE(50) NOT NULL, 
	EMAIL VARCHAR_IGNORECASE(50) NOT NULL)