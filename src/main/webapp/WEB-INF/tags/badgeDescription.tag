<%@ tag language="java" pageEncoding="UTF-8"%>
<%@attribute name="badgeName" type="java.lang.String" required="true" %>
<%@attribute name="badgeDescription" type="java.lang.String" required="true" %>
<%@attribute name="badgeClass" type="java.lang.String" required="true" %>

<a href="" class="badge"><span class="badge-ball badge-tag badge-${badgeClass}">&nbsp;</span>${t[badgeName]}</a> ${t[badgeDescription]}
