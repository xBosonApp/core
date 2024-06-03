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
        <h1>超级用户</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          指定一个用户为超级用户, 该用户不受任何权限约束;<br/>
          该用户应该已经存在于平台上, 指定不存在的用户可能引起安全风险.
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>超级用户权限</h6>

          <table>
            <tr>
            <td>用户:</td><td>
              <input name="rootUserName"
                     value="${ param.rootUserName }"/>
              <a href='#' val='unknowadmin'
                 setto='[name=rootUserName]'>(default)</a>
              (最短4个字符)
              </td>
            </tr>

            <tr>
            <td>密码:</td><td>
              <input name="rootPassword"
                     value="${ param.rootPassword }"/>
              <a href='#' val='unknowpassword'
                 setto='[name=rootPassword]'>(default)</a>
              </td>
            </td>
            </tr>

            <tr><td></td><td>
              <input type='submit' value='确定' />
              <span class="red"><%=request.getAttribute("msg") %></span>
            </td>
            </tr>
          </table>
        </form>
      </div>

    </div>
  </body>
</html>