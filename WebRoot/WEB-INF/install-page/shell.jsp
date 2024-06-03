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
        <h1>SHELL 脚本</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          <p>平台脚本环境中可以执行一个操作系统脚本,
              这些脚本必须保存在指定的目录中, 'shell' 模块依赖这个配置.</p>
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>Shell Script Setting</h6>

          <table>
          <tr>
            <td>脚本目录</td><td>
            <input name="shellUrl" value="${ param.shellUrl }" />
              <a href='#' val='~/web4xboson/shell-script'
                 setto='[name=shellUrl]'/>(Linux)</a>
              <a href='#' val='c:\web4xboson\shell-script'
                 setto='[name=shellUrl]'/>(Windows)</a>
            </td>
          </tr>

          <tr><td></td>
            <td> <input type='submit' value='确定' />
            <span class="red">
              <%=request.getAttribute("msg") %>
            </span>
            </td>
          </tr>
          </table>
        </form>
      </div>

    </div>
  </body>
</html>