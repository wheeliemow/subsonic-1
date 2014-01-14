<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<%@ include file="header.jsp" %>

<body>

<a name="top"/>

<div id="container">
    <jsp:include page="menu.jsp">
        <jsp:param name="current" value="screenshots"/>
    </jsp:include>

    <div id="content">
        <div id="main-col">
            <a href="inc/img/screenshots/screen02.png"><img src="inc/img/screenshots/thumb02.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen01.png"><img src="inc/img/screenshots/thumb01.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen05.png"><img src="inc/img/screenshots/thumb05.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen06.png"><img src="inc/img/screenshots/thumb06.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen03.png"><img src="inc/img/screenshots/thumb03.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen07.png"><img src="inc/img/screenshots/thumb07.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen08.png"><img src="inc/img/screenshots/thumb08.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen09.png"><img src="inc/img/screenshots/thumb09.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen10.png"><img src="inc/img/screenshots/thumb10.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen11.png"><img src="inc/img/screenshots/thumb11.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/subair/subair-1.png" title="SubAir app"><img src="inc/img/screenshots/subair/subair-thumb-1.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/screen17.png" title="Z-Subsonic app for iPhone"><img src="inc/img/screenshots/thumb17.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/isub/isub-1.png" title="iSub app for iPhone"><img src="inc/img/screenshots/isub/isub-thumb-1.png" alt="" style="padding:3px"/></a>
            <a href="inc/img/screenshots/android/android-1.png" title="Subsonic app for Android"><img src="inc/img/screenshots/android/android-thumb-1.png" alt="" style="padding:3px;padding-right:30px"/></a>
            <a href="inc/img/screenshots/android/android-2.png" title="Subsonic app for Android"><img src="inc/img/screenshots/android/android-thumb-2.png" alt="" style="padding:3px;padding-right:30px"/></a>
            <a href="inc/img/screenshots/android/android-3.png" title="Subsonic app for Android"><img src="inc/img/screenshots/android/android-thumb-3.png" alt="" style="padding:3px;"/></a>
            <a href="inc/img/screenshots/screen23.png" title="Subsonic app for Windows Phone"><img src="inc/img/screenshots/thumb23.png" alt="" style="padding:3px;padding-right:10px"/></a>
            <a href="inc/img/screenshots/screen24.png" title="Subsonic app for Windows Phone"><img src="inc/img/screenshots/thumb24.png" alt="" style="padding:3px;padding-right:10px"/></a>
            <a href="inc/img/screenshots/screen25.png" title="Subsonic app for Windows Phone"><img src="inc/img/screenshots/thumb25.png" alt="" style="padding:3px"/></a>
        </div>

        <div id="side-col">
            <%@ include file="google-translate.jsp" %>
            <%@ include file="download-subsonic.jsp" %>
            <%@ include file="premium-column.jsp" %>
        </div>

        <div class="clear">
        </div>
    </div>
    <hr/>
    <%@ include file="footer.jsp" %>
</div>


</body>
</html>
