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
        <h1>集群设置</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          <p>同一个集群中的运算节点 ID 不能重复, 有效值为 0~1023, 默认为 0.</p>
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>Core DBMS</h6>

          <table>
          <tr>
            <td>运算节点ID:</td><td><input name="clusterNodeID" value="${ param.clusterNodeID }" />
              <a href='#' val='0' setto='[name=clusterNodeID]'>(单机)</a></td>
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