<c:set var="title" value="${t['metas.profile.title']}"/>
<c:set var="siteName" value="${t['site.name']}"/>

<c:set var="genericTitle" value="${t['metas.generic.title'].args(siteName)}"/>

<tags:header title="${genericTitle} - ${title}"/>

<ul class="badge-descriptions">
    <c:forEach items="${badges}" var="badge">
        <li class="badge-item"><tags:badgeDescription badgeName="${badge.name}" badgeDescription="${badge.description}" badgeClass="${badge.badgeClass}"/></li>
    </c:forEach>
</ul>
