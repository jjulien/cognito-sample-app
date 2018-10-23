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
    <div class="grid-x">
        <div class="small-12 cell text-center">
            <h3>S3 Cognito Demo</h3>
        </div>
    </div>

    <div class="grid-x">
        <div class="small-12 cell">
            <h4>List of All Buckets</h4>
        </div>
        <c:forEach items="${all_buckets}" var="bucket">
            <div class="small-12 cell">
                <c:out value="${bucket.getName()}" />
            </div>
        </c:forEach>
    </div>
    <div class="grid-x space-above">
        <div class="small-12 cell">
            <h4>Access Token</h4>
        </div>

        <div class="small-2 cell">
            <b>Header</b>
        </div>
        <div class="small-10 cell">
            ${access_token_header}
        </div>

        <div class="small-2 cell">
            <b>Payload</b>
        </div>
        <div class="small-10 cell">
            ${access_token_payload}
        </div>

        <div class="small-2 cell">
            <b>Signature</b>
        </div>
        <div class="small-10 cell">
            ${access_token_signature}
        </div>
    </div>

    <div class="grid-x space-above">
        <div class="small-12 cell">
            <h4>ID Token</h4>
        </div>

        <div class="small-2 cell">
            <b>Header</b>
        </div>
        <div class="small-10 cell">
            ${id_token_header}
        </div>

        <div class="small-2 cell">
            <b>Payload</b>
        </div>
        <div class="small-10 cell">
            ${id_token_payload}
        </div>

        <div class="small-2 cell">
            <b>Signature</b>
        </div>
        <div class="small-10 cell">
            ${id_token_signature}
        </div>
     </div>
</div>
</body>

</html>