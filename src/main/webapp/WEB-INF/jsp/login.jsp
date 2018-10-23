<!DOCTYPE html>

<head>
    <%@ include file='header.jspf' %>
</head>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html lang="en">

<body>
    <%@ include file='topbar.jspf' %>
    <div class="content">
        <c:if test="${error}" >
        <div class="error">
            ${error_description}
        </div>
        </c:if>
        <div class="login text-center">
            <div class="grid-x">
                <div class="small-12 large-offset-3 large-6 cell">
                    <p>You can initiate a SAML login with Azure by clicking the button below.</p>
                </div>
            </div>
            <div class="grid-x">
                <div class="small-12 large-offset-3 large-6 cell">
                    <p>If you want to trace the SAML login, use developer tools, preserve the network log, and you will be able to see the exchange between Cognito and Azure</p>
                </div>
            </div>
            <div class="grid-x space-above">
                <div class="small-12 large-offset-3 large-6 cell">
                    <a class="button" href="${loginurl}">Login with Azure</a>
                </div>
            </div>
        </div>
    </div>

    <script type="text/javascript">
        if ( window.location.hash ) {
        	var raw = window.location.hash.substr(1);
        	var parts = raw.split("&");
        	var access_token;
        	for (i=0;i<parts.length;i++) {
        	    var split = parts[i].split("=");
        		console.log(split[0] + "=" + split[1]);
        		if (split[0] == "access_token") {
        		    access_token = split[1];
        		}
        	}
        	if ( access_token ) {
        	    window.location.href='/s3?' + raw;
            } else {
                console.log("No access token found");
            }
        }
    </script>
</body>

</html>