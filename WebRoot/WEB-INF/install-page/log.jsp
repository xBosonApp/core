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
        <h1>系统日志</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          全局日志配置<br/>
          当需要单独修改日志级别时,  在配置文件
          "<%=c.configPath %>/log.level.properties" 寻找对应的配置项.
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>LOG</h6>

          <table>
          <tr>
            <td>输出目录:</td><td><input name="log_path" value="${ param.log_path }" />
              <a href='#' val='/logs' setto='[name=log_path]'>(/logs)</a>
            </td>
          </tr>

          <tr>
            <td>全局输出级别: </td>
            <td>
              <select name="log_level" value="${ param.log_level }">
                <option value="ALL">ALL</optione>
                <option value="DEBUG">DEBUG</optione>
                <option value="INFO">INFO</optione>
                <option value="WARN">WARN</optione>
                <option value="ERR">ERR</optione>
                <option value="FATAL">FATAL</optione>
                <option value="OFF">OFF</optione>
              </select>
            </td>
          </tr>

          <tr>
            <td>日志输出到: </td><td>
              <select name="log_type" value="${ param.log_type }">
                <option value="ConsoleOut">控制台</optione>
                <option value="FileOut">日志文件</optione>
                <option value="FileAndConsoleOut">日志文件 & 控制台</optione>
                <option value="DbOut">数据库</optione>
                <option value="DbAndConsoleOut">数据库 & 控制台</optione>
              </select>
            </td>
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