<%@ page contentType="text/html;charset=UTF8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8" />
    <meta content="text/html; charset=utf-8" http-equiv="content-type" />
    <title>xBoson Install</title>
    <style type="text/css">
      <%@ include file="install.css" %>
    </style>
    <script><%@ include file="jquery-2.1.4.min.js" %></script>
    <script><%@ include file="install.js" %></script>
    <%! com.xboson.been.Config c; %>
    <% c =  (com.xboson.been.Config) request.getAttribute("config"); %>
  </head>

  <body>
    <div id='contentdiv'>
      <div id='left'>
        <h1>系统重启中</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          平台即将上线, 请稍等片刻,
          稍后请用超级管理员登录.<br/>
          <a href="<%=request.getServletContext().getContextPath() %>">
            点击这里进入首页</a>
        </section>

        <section class='red'>
          <%=request.getAttribute("msg") %>
        </section>
      </div>

    </div>
  </body>
</html>