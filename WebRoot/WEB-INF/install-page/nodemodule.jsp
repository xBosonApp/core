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
        <h1>Node Module</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          <p>脚本环境可以加载 nodejs 模块, 这些模块通过 npm 安装在本地目录上.</p>

          <p>在集群模式中, 配置一个 "本地静态文件" 模式的节点, 该模式需要设置 "静态文件根目录";
            <br/>其他节点为 "远程虚拟文件" 模式, 该模式无需设置 "静态文件根目录".
          </p>
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>Node Module Setting</h6>

          <table>
          <tr>
            <td>模式: </td><td>
            <select name="nodeProviderClass" value="${ param.nodeProviderClass }">
              <option value="local">本地静态文件</option>
              <option value="online">远程虚拟文件</option>
            </select></td>
          </tr>

          <tr>
            <td class='td1'>静态文件根目录:</td><td>
              <input name="nodeUrl" value="${ param.nodeUrl }" />
              <a href='#' val='~/web4xboson/xboson-node-modules'
                 setto='[name=nodeUrl]'/>(Linux)</a>
              <a href='#' val='C:\web4xboson\xboson-node-modules'
                 setto='[name=nodeUrl]'/>(Windows)</a>
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