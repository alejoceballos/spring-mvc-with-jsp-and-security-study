<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<%@include file="/WEB-INF/view/header.jspf" %>
<body>
<h1>Admin Page</h1>
<h3>Go to <a href="/admin/other">another admin page</a></h3>
<%@include file="/WEB-INF/view/links.jspf"%>
<table>
    <thead>
    <tr>
        <th>USER</th>
        <th>AUTHORIZATIONS</th>
    </tr>
    </thead>
    <tbody id="tbl">
    </tbody>
</table>
</body>
<script>
    displayUsers();
</script>
</html>
