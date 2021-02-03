<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%-- /WebContent/layout/boardlayout.jsp --%> 
<%@ taglib prefix="decorator" uri="http://www.opensymphony.com/sitemesh/decorator" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="path" value="${pageContext.request.contextPath}" />          
    
<!DOCTYPE html>
<html lang="en">
<title><decorator:title /></title>
<link rel="stylesheet" href="${path}/css/main.css">
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
<link rel="stylesheet" href="https://www.w3schools.com/lib/w3-theme-black.css">
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<style>
html,body,h1,h2,h3,h4,h5,h6 {font-family: "Roboto", sans-serif;}
.w3-sidebar {
  z-index: 3;
  width: 250px;
  top: 40px;
  bottom: 0;
  height: inherit;
}
.w3-top {
   line-height: 2.3;
}
</style>
<script type="text/javascript"
	src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
<script type="text/javascript" 
   src="http://cdn.ckeditor.com/4.5.7/standard/ckeditor.js"></script>

<decorator:head />
<body>

<div class="w3-top">
  <div class="w3-bar w3-theme w3-top w3-right-align w3-large">
    <a class="w3-bar-item w3-button w3-right w3-hide-large w3-hover-white w3-large w3-theme-l1" href="javascript:void(0)" onclick="w3_open()"><i class="fa fa-bars"></i></a>
    <c:if test="${empty sessionScope.loginUser }">
       <a href="${path}/user/login.shop">로그인</a>
       <a href="${path}/user/userEntry.shop">회원가입</a>
    </c:if>
    <c:if test="${!empty sessionScope.loginUser }">
      ${sessionScope.loginUser.username}님이 로그인 하셨습니다.&nbsp;&nbsp;
       <a href="${path}/user/logout.shop">로그아웃</a>
    </c:if>
  </div>
</div>

<!-- Sidebar -->
<nav class="w3-sidebar w3-bar-block w3-collapse w3-large w3-theme-l5 w3-animate-left" id="mySidebar">
  <a href="javascript:void(0)" onclick="w3_close()" class="w3-right w3-xlarge w3-padding-large w3-hover-black w3-hide-large" title="Close Menu">
    <i class="fa fa-remove"></i>
  </a>
  <h4 class="w3-bar-item"><b>Menu</b></h4>
  <a class="w3-bar-item w3-button w3-hover-black" 
      href="${path}/user/main.shop">회원관리</a>
  <a class="w3-bar-item w3-button w3-hover-black" 
      href="${path}/item/list.shop">상품관리</a>
  <a class="w3-bar-item w3-button w3-hover-black" 
      href="${path}/board/list.shop">게시판</a>
  <a class="w3-bar-item w3-button w3-hover-black"
      href="${path}/chat/chat.shop">채팅</a><br>
</nav>

<!-- Overlay effect when opening sidebar on small screens -->
<div class="w3-overlay w3-hide-large" onclick="w3_close()" style="cursor:pointer" title="close side menu" id="myOverlay"></div>

<!-- Main content: shift it to the right by 250 pixels when the sidebar is visible -->
<div class="w3-main" style="margin-left:250px">
  <div class="w3-row w3-padding-64">
    <div class="w3-col w3-container" style="width:95%; height:700px; overflow: auto;">
       <decorator:body />
    </div>
    <div class="w3-col w3-container"style="width:5%">
      <p class="w3-padding-large w3-padding-32 w3-center">&nbsp</p>
      <p class="w3-padding-large w3-padding-64 w3-center">&nbsp</p>
    </div>
  </div>

  <footer class="w3-container w3-padding-16 w3-light-grey">
    <h4>구디 아카데미 Since 2016</h4>
    <p>Powered by <a href="http://www.goodee.co.kr" target="_blank">구디아카데미</a></p>
  </footer>
</div>

<script>
// Get the Sidebar
var mySidebar = document.getElementById("mySidebar");

// Get the DIV with overlay effect
var overlayBg = document.getElementById("myOverlay");

// Toggle between showing and hiding the sidebar, and add overlay effect
function w3_open() {
  if (mySidebar.style.display === 'block') {
    mySidebar.style.display = 'none';
    overlayBg.style.display = "none";
  } else {
    mySidebar.style.display = 'block';
    overlayBg.style.display = "block";
  }
}
// Close the sidebar with the close button
function w3_close() {
  mySidebar.style.display = "none";
  overlayBg.style.display = "none";
}
</script>
</body>
</html>
