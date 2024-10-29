//******************************************************
//* DEFINE a new logstream model for DFHSECR journal
//* Note: Log data will be autodeleted after 1 day
//******************************************************
//DEFMODEL EXEC PGM=IXCMIAPU
//SYSPRINT DD SYSOUT=A,DCB=RECFM=FBA
//SYSIN    DD *
   DATA TYPE(LOGR) REPORT(NO)
   DEFINE LOGSTREAM NAME(++OWNER++.++APPLID++.MODEL)
          STRUCTNAME(++CFSTRUCT++)  MODEL(YES)
          AUTODELETE(YES) RETPD(0001)
/*