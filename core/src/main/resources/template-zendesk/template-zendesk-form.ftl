<html>
   <head></head>
   <body>
<form id="jwtForm" method="POST" action="${action}">
         <input id="jwtString" type="hidden" name="jwt" value="${jwt}"/>
         <input id="returnTo" type="hidden" name="return_to" value="${returnTo}"/>
         </form>
         <script>window.onload = () => { document.forms["jwtForm"].submit(); };</script>
    </body>
</html>