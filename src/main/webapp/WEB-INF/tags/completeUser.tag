<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@attribute name="user" type="org.mamute.model.User" required="true" %>
<%@attribute name="microdata" required="false" %>
<%@attribute name="edited" required="false" %>
<div class="complete-user">
	<jsp:doBody/>
	<a href="${linkTo[UserProfileController].showProfile(user,user.sluggedName)}"><img border="0" class="user-image" src="${userMediumPhoto ? user.getMediumPhoto(env.get('gravatar.avatar.url')) : user.getSmallPhoto(env.get('gravatar.avatar.url'))}"/></a>
	<div class="user-info" 
		<c:if test="${microdata}">
			itemscope itemtype="http://schema.org/Person" itemprop="${edited ? 'editor' : 'author'}"
		</c:if>
	>
		<tags:userProfileLink user="${user}" htmlClass="user-name ellipsis" microdata="${microdata}"/>
            <c:if test="${user.badges.size() > 0}">
                <span class="badge-bar">
                    <c:if test="${user.goldBadges.size() > 0}">
                        <span class="badge-ball badge-gold"></span>
                        ${user.goldBadges.size()}
                    </c:if>
                    <c:if test="${user.silverBadges.size() > 0}">
                        <span class="badge-ball badge-silver"></span>
                        ${user.silverBadges.size()}
                    </c:if>
                    <c:if test="${user.bronzeBadges.size() > 0}">
                        <span class="badge-ball badge-bronze"></span>
                        ${user.bronzeBadges.size()}
                    </c:if>
                </span>
            </c:if>
		<div title="${t['touch.karma.title']}" class="user-karma ellipsis">${user.karma}<tags:pluralize key="touch.karma" count="${user.karma}" /></div>
	</div>
</div>