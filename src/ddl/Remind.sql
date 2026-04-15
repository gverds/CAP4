--==============================================================
-- Table: REMIND 提醒通知
--==============================================================
--drop table CFG_REMIND;
create table CFG_REMIND (
   OID                  CHAR(32)               not null,
   CONTENT              VARCHAR(2000),
   SCOPETYP             CHAR(1),
   SCOPEPID             VARCHAR(10),
   STARTDATE            TIMESTAMP,
   ENDDATE              TIMESTAMP,
   CRTIME               TIMESTAMP              default CURRENT_TIMESTAMP,
   LOCALE               CHAR(5),
   UPDATER              VARCHAR(10),
   UPDTIME              TIMESTAMP              default CURRENT_TIMESTAMP,
   constraint P_REMIND primary key (OID)
);

comment on table CFG_REMIND is '提醒通知';
COMMENT ON COLUMN CFG_REMIND.OID IS 'OID';
COMMENT ON COLUMN CFG_REMIND.CONTENT IS '內文';
COMMENT ON COLUMN CFG_REMIND.SCOPETYP IS '0.個人 1.組別(業務) 2.群組 3.部門 9.全體';
COMMENT ON COLUMN CFG_REMIND.SCOPEPID IS '對象號碼';
COMMENT ON COLUMN CFG_REMIND.STARTDATE IS '資料有效期間';
COMMENT ON COLUMN CFG_REMIND.ENDDATE IS '資料有效期間';
COMMENT ON COLUMN CFG_REMIND.CRTIME IS '建立時間';
COMMENT ON COLUMN CFG_REMIND.LOCALE IS '語系';
COMMENT ON COLUMN CFG_REMIND.UPDATER IS '最後異動人員';
COMMENT ON COLUMN CFG_REMIND.UPDTIME IS '修改日期';

--==============================================================
-- Table: REMINDS 通知方式
--==============================================================
--drop table CFG_REMINDS;
create table CFG_REMINDS (
   OID                  CHAR(32)               not null,
   PID                  CHAR(32)               not null,
   SCOPEPID             VARCHAR(10),
   STYLETYP             CHAR(1),
   STYLECLR             CHAR(1),
   STYLE                DECIMAL(5,0),
   UNIT                 INTEGER,
   YNFLAG               CHAR(1)                default '0',
   constraint P_REMINDS primary key (OID)
);

comment on table CFG_REMINDS is '通知方式';
COMMENT ON COLUMN CFG_REMINDS.OID IS 'OID';
COMMENT ON COLUMN CFG_REMINDS.PID IS 'PID';
COMMENT ON COLUMN CFG_REMINDS.SCOPEPID IS '對象號碼';
COMMENT ON COLUMN CFG_REMINDS.STYLETYP IS '提醒方式';
COMMENT ON COLUMN CFG_REMINDS.STYLECLR IS '顏色';
COMMENT ON COLUMN CFG_REMINDS.STYLE IS '數値';
COMMENT ON COLUMN CFG_REMINDS.UNIT IS '單位';
COMMENT ON COLUMN CFG_REMINDS.YNFLAG IS '是否完成';
