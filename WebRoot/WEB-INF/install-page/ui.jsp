<%@ page contentType="text/html;charset=UTF8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8" />
    <meta content="text/html; charset=utf-8" http-equiv="content-type" />
    <title>xBoson Install</title>
    <style type="text/css">
      <%@ include file="install.css" %>
      .td1 {
        text-align : right;
      }
    </style>
    <script><%@ include file="jquery-2.1.4.min.js" %></script>
    <script><%@ include file="install.js" %></script>
    <%! com.xboson.been.Config c; %>
    <% c =  (com.xboson.been.Config) request.getAttribute("config"); %>
  </head>

  <body>
    <div id='contentdiv'>
      <div id='left'>
        <h1>UI 服务</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          <p>当配置一个集群时, 必须有且只有一个节点配置为 <b>"本地静态文件"</b>, 其他节点
            配置为 <b>'远程虚拟文件'</b>, 如果配置错误, 对文件的写入会产生混乱. </p>

          <p>单机模式的服务器, 只要配置为 <b>"本地静态文件"</b> 即可;
            静态文件根目录必须是绝对路径, 应该有如下几个子目录: t, ui, web, lib</p>

          <p>如果选了 <b>"输出目录列表"</b> 当用户打开的路径是目录时, 会返回一个 html
            页面, 并输出这个目录中的所有文件/目录,
            该特性仅在单机时有效, 在集群中设置容易出现, 有时不好用的情况.
            <u>(该功能用于调试, 暴露目录结构会产生安全问题)</u> </p>
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>UI Service</h6>

          <table>
          <tr>
            <td class='td1'>服务类型: </td><td>
              <select name="uiProviderClass" value="${ param.uiProviderClass }">
                <option value="local">本地静态文件</option>
                <option value="online">远程虚拟文件</option>
              </select>
            </td>
          </tr>

          <tr>
            <td class='td1'>静态文件根目录:</td><td>
              <input name="uiUrl" value="${ param.uiUrl }" />
              <a href='#' val='~/web4xboson/public'
                 setto='[name=uiUrl]'/>(Linux)</a>
              <a href='#' val='C:\web4xboson\public'
                 setto='[name=uiUrl]'/>(Windows)</a>
            </td>
          </tr>

          <tr>
            <td class='td1'>根路径跳转:</td><td>
              <input name="uiWelcome" value="${ param.uiWelcome }" />
              <a href='#' val='/face/ui/paas/login.html'
                 setto='[name=uiWelcome]'/>(默认登录页)</a>
            </td>
          </tr>

          <tr>
            <td class='td1'>输出目录列表:</td>
            <td>
              <select name="uiListDir" value="${ param.uiListDir }" >
                <option value='false'>否</option>
                <option value='true'>是</option>
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