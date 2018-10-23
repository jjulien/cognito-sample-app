<!DOCTYPE html>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<head>
    <%@ include file='header.jspf' %>
</head>
<html lang="en">

<body>

    <%@ include file='topbar.jspf' %>

    <div class="content">
        <div class="grid-x">
            <div class="small-12 cell">
                <h1>Cognito Sample App</h1>
            </div>
            <div class="small-12 cell">
                <p>This app demonstrates the ability to</p>
            </div>
        </div>
        <div>
           ${message}
        </div>
        <div>
            <a class="button" href="/login">Login</a>
        </div>
</body>

</html>