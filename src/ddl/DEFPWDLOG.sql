--SET SCHEMA CAP;

-- ====================================
-- 密碼歷史資訊
-- ====================================
--DROP TABLE DEF_PWDLOG;
create table DEF_PWDLOG (
   OID                  CHAR(32)               not null,
   USERCODE                 CHAR(4)                not null,
   PASSWORD                 VARCHAR(100),
   UPDATETIME           TIMESTAMP              default CURRENT_TIMESTAMP,
   DISCRIMINATOR		VARCHAR(31)            not null,
   constraint P_DEF_PWDLOG primary key (OID)
);

CREATE UNIQUE INDEX XDEFPWLG01
    ON DEF_PWDLOG (OID ASC);

COMMENT ON TABLE DEF_PWDLOG IS '密碼歷史資訊';

COMMENT ON COLUMN DEF_PWDLOG.OID IS 'oid';
COMMENT ON COLUMN DEF_PWDLOG.USERCODE IS '使用者代號';
COMMENT ON COLUMN DEF_PWDLOG.PASSWORD IS '密碼';
COMMENT ON COLUMN DEF_PWDLOG.UPDATETIME IS '修改日期';

