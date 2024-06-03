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
        <h1>Redis 缓存</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          平台需要一个 Redis 服务器来保存临时数据, Redis 版本 > 3.0.0.
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>Core Redis Connection Setting</h6>

          <table>
          <tr>
            <td>地址:</td>
            <td><input name="rhost" value="${ param.rhost }" />
              <a href='#' val='localhost' setto='[name=rhost]'>(localhost)</a></td>
            </td>
          </tr><tr>
            <td>端口: </td>
            <td><input name="rport" value="${ param.rport }" />
              <a href='#' val='6379' setto='[name=rport]'>(6379)</a>
            </td>
          </tr><tr>
            <td>密码: </td>
            <td><input name="rpassword" 
              value="${ param.rpassword }" type="password" /></td>
          </tr>
          <tr><td></td>
            <td> <input type='submit' value='确定' />
              <span class="red"><%=request.getAttribute("msg") %></span>
            </td>
          </tr>
          </table>
        </form>
      </div>

    </div>
  </body>
</html>