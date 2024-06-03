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
        <h1>数据库</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          <p>平台需要一个 Mysql 数据库作为内核存储库, 请设置一个长期稳定的 Mysql 服务器连接.
          Mysql 版本在 5.0 以上.</p>

          <p>database 字段在 mysql 中叫 schema, 在 oralce 中叫 tablespace</p>
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>Core DBMS</h6>

          <table>
          <tr>
            <td>DB地址:</td><td><input name="host" value="${ param.host }" />
              <a href='#' val='localhost' setto='[name=host]'>(localhost)</a></td>
          </tr>

          <tr>
            <td> DB端口: </td>
            <td><input name="port" value="${ param.port }"/>
              <a href='#' val='3306' setto='[name=port]'>(3306)</a></td>
          </tr>

          <tr>
            <td> DB类型: </td><td>
              <select name='dbname'>
                <option>mysql</option>  
              </select>
            </td>
          </tr>

          <tr>
            <td> DB用户: </td><td><input name="username" value="${ param.username }"/>
              <a href='#' val='root' setto='[name=username]'>(root)</a>
            </td>
          </tr>

          <tr>
            <td> DB密码: </td><td><input name="password" 
              value="${ param.password }" type="password" /></td>
          </tr>
          <tr>
            <td> database: </td><td>
              <input name="database" value="${ param.database }"/>
              <a href='#' val='a297dfacd7a84eab9656675f61750078'
                 setto='[name=database]'>(sys-org-id)</a>
            </td>
          </tr>

          <tr><td></td>
            <td> <input type='checkbox' value='1' name='createdb' />
                自动创建不存在的 database.
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