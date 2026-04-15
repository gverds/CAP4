--SET SCHEMA CAP;

-- ====================================
-- 代理人檔
-- ====================================
--DROP TABLE DEF_PROXY;
create table DEF_PROXY (
   oid                  CHAR(32)               not null,
   STAFFPID             CHAR(10)               not null,
   PROXYPID             CHAR(10)               not null,
   PROXYBEG             DATE,
   PROXYEND             DATE,
   PROXYFLG             CHAR(1)                default '1',
   UPDATER              CHAR(6),
   UPDATETIME           TIMESTAMP,
   constraint P_DEF_PROXY primary key (oid)
);

COMMENT ON TABLE DEF_PROXY IS '代理人檔';

COMMENT ON COLUMN DEF_PROXY.oid IS 'oid';
COMMENT ON COLUMN DEF_PROXY.STAFFPID IS '行員編號';
COMMENT ON COLUMN DEF_PROXY.PROXYPID IS '職務代理';
COMMENT ON COLUMN DEF_PROXY.PROXYBEG IS '代理起日';
COMMENT ON COLUMN DEF_PROXY.PROXYEND IS '代理迄日';
COMMENT ON COLUMN DEF_PROXY.PROXYFLG IS '0.停用 1.啟用';
COMMENT ON COLUMN DEF_PROXY.UPDATER IS '最後異動人員';
COMMENT ON COLUMN DEF_PROXY.UPDATETIME IS '修改日期';

