--SET SCHEMA CAP;

-- ====================================
-- 使用者基本資料檔
-- ====================================
--DROP TABLE DEF_USER;
create table DEF_USER (
   OID                  VARCHAR(32)               not null,
   CODE                 VARCHAR(10)               not null,
   NAME                 VARCHAR(12),
   DEPCODE              VARCHAR(4),
   DEPNAME              VARCHAR(50),
   STATUSDESC           VARCHAR(30),
   UPDATER              VARCHAR(10),
   UPDATETIME           TIMESTAMP,
   PASSWORD				VARCHAR(100),
   EMAIL				VARCHAR(128),
   STATUS				VARCHAR(1),
   PRESTATUS			VARCHAR(1),
   CREATOR              VARCHAR(10),
   CREATETIME           TIMESTAMP,
   PWDEXPIREDTIME       TIMESTAMP,
   LASTLOGINTIME        TIMESTAMP,
   DISCRIMINATOR		VARCHAR(31)            not null,
   constraint P_DEF_USER primary key (OID)
);

CREATE UNIQUE INDEX XDEFUSER01
    ON DEF_USER (CODE ASC, DEPCODE ASC);

COMMENT ON TABLE DEF_USER IS '使用者基本資料檔';

COMMENT ON COLUMN DEF_USER.OID IS 'oid';
COMMENT ON COLUMN DEF_USER.CODE IS '行員編號';
COMMENT ON COLUMN DEF_USER.NAME IS '行員姓名';
COMMENT ON COLUMN DEF_USER.DEPCODE IS '分行代碼';
COMMENT ON COLUMN DEF_USER.STATUSDESC IS '停用原因';
COMMENT ON COLUMN DEF_USER.UPDATER IS '最後異動人員';
COMMENT ON COLUMN DEF_USER.UPDATETIME IS '修改日期';

