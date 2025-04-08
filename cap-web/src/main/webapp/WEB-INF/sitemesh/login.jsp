<%@ taglib uri="http://www.opensymphony.com/sitemesh/decorator" prefix="decorator"%>
<!DOCTYPE html>
<html>
<head>
<link rel="stylesheet" href="../static/css/main.css" />
<link rel="stylesheet" href="../static/css/form.css" />
<link rel="stylesheet" href="../static/css/login.css" />
<style>
/*login hide common.message(iLog)*/
#newSysMsgArea {
  display: none;
}
</style>
<script type="text/javascript">
  var baseUrl = "../static";
</script>
<title><decorator:title default="CapWebMVC" /></title>
<script src="../static/requirejs/2.3.2/require.min.js"></script>
<script src="../static/main.js"></script>
<decorator:getProperty property="reqJSON" default="" />
<decorator:getProperty property="i18n" default="" />
<decorator:head />
<meta name="_csrf" content="${_csrf.token}" />
<meta name="_csrf_header" content="${_csrf.headerName}" />
</head>
<body>
    <decorator:body />
</body>
</html>
