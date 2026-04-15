--SET SCHEMA CAP;

-- ====================================
-- 單位檔
-- ====================================
--DROP TABLE DEF_DEP;
create table DEF_DEP (
   OID                  CHAR(32)               not null,
   CODE                 CHAR(4)                not null,
   NAME                 VARCHAR(30),
   LEVEL                CHAR(1),
   TEL                  VARCHAR(20),
   ZIP                  CHAR(5),
   ADDR                 VARCHAR(90),
   DIVISION             CHAR(4),
   MANAGER              VARCHAR(30),
   PROXY                VARCHAR(30),
   BIZGROUP             CHAR(4),
   COUNTRY              CHAR(2),
   TIMEZONE             CHAR(9),
   STATUS               CHAR(1),
   UPDATER              VARCHAR(10),
   UPDATETIME           TIMESTAMP              default CURRENT_TIMESTAMP,
   DISCRIMINATOR		VARCHAR(31)            not null,
   constraint P_DEF_DEP primary key (OID)
);

CREATE UNIQUE INDEX XDEFBRN01
    ON DEF_DEP (CODE ASC);

COMMENT ON TABLE DEF_DEP IS '單位檔';

COMMENT ON COLUMN DEF_DEP.OID IS 'oid';
COMMENT ON COLUMN DEF_DEP.CODE IS '分行代碼';
COMMENT ON COLUMN DEF_DEP.NAME IS '分行名稱';
COMMENT ON COLUMN DEF_DEP.LEVEL IS '分行層級';
COMMENT ON COLUMN DEF_DEP.TEL IS '分行電話';
COMMENT ON COLUMN DEF_DEP.ZIP IS '郵遞區號';
COMMENT ON COLUMN DEF_DEP.ADDR IS '分行地址';
COMMENT ON COLUMN DEF_DEP.DIVISION IS '單位預設分類';
COMMENT ON COLUMN DEF_DEP.MANAGER IS '分行經理';
COMMENT ON COLUMN DEF_DEP.PROXY IS '法務代理人';
COMMENT ON COLUMN DEF_DEP.BIZGROUP IS '所屬區域中心';
COMMENT ON COLUMN DEF_DEP.COUNTRY IS '國別';
COMMENT ON COLUMN DEF_DEP.TIMEZONE IS '格式：GMT Sign Hours : Minutes 例：GMT-08:00';
COMMENT ON COLUMN DEF_DEP.STATUS IS '停業註記';
COMMENT ON COLUMN DEF_DEP.UPDATER IS '最後異動人員';
COMMENT ON COLUMN DEF_DEP.UPDATETIME IS '修改日期';

