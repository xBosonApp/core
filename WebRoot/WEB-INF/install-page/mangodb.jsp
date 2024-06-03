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
        <h1>Mongo DB</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          MongoDB 为可选组件, 被网盘和脚本模块依赖, 如果未正确配置, 这些模块将不可用.
          <br/>
          MongoDB 版本 >= 3.6
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>MongoDB Connection Setting</h6>

          <table>
          <tr>
            <td>地址:</td>
            <td><input name="host" value="${ param.host }" />
              <a href='#' val='localhost' setto='[name=host]'>(localhost)</a></td>
            </td>
          </tr>

          <tr>
            <td>端口: </td>
            <td><input name="port" value="${ param.port }" />
              <a href='#' val='27017' setto='[name=port]'>(27017)</a>
            </td>
          </tr>

          <tr>
            <td>用户名: </td>
            <td><input name="username" value="${ param.username }" /></td>
          </tr>

          <tr>
            <td>密码: </td>
            <td><input name="password"
              value="${ param.password }" type="password" /></td>
          </tr>

          <tr>
            <td>默认数据库: </td>
            <td><input name="database"value="${ param.database }" />
                <a href='#' val='xboson' setto='[name=database]'>(default)</a></td>
          </tr>

          <tr>
            <td></td>
            <td><input name="skip" value="1" type="checkbox" />跳过配置</td>
          </tr>

          <tr><td></td>
            <td> <input type='submit' value='确定' />
            </td>
          </tr>
          </table>
          <span class="red">
            <%=request.getAttribute("msg") %>
          </span>
        </form>
      </div>

    </div>
  </body>
</html>