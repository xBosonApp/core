<%@ page contentType="text/html;charset=UTF8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="utf-8" />
    <meta content="text/html; charset=utf-8" http-equiv="content-type" />
    <title>xBoson Install</title>
    <style type="text/css">
      <%@ include file="install.css" %>

      .configlist * {
        border-bottom: 1px solid #aaa;
      }
      .configlist a {
        color: #ccc;
        display: inline-block;
        width: 100px;
        background-color: #555;
        padding: 3px 10px;
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
        <h1>配置完成</h1>
      </div>

      <div id='right'>
        <h2>配置文件</h2>

        <section class='configlist'>
          <div><a>文件路径</a>
                <%=c.configFile %></div>
          <div><a>ROOT 帐号</a>
                <%=c.rootUserName %></div>
          <div><a>DB</a>
                <%=c.db.getDbname() %> /
                <%=c.db.getHost() %> / <%=c.db.getDatabase() %></div>
          <div><a>Redis</a>
                <%=c.redis.getHost() %> : <%=c.redis.getPort() %></div>
          <div><a>Log Level</a>
                <%=c.logLevel %> </div>
          <div><a>UI 目录</a>
                <%=c.uiUrl %> </div>
          <div><a>UI 目录列表</a>
                <%=c.uiListDir %> </div>
          <div><a>根目录跳转</a>
                <%=c.uiWelcome %> </div>
          <div><a>JS 模块目录</a>
                <%=c.nodeUrl %> </div>
          <div><a>运算节点 ID</a>
                <%=c.clusterNodeID %> </div>
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <h6>配置已经就绪, 准备重置服务器</h6>

          <section>
            <p>
              <input type="checkbox" name="act" value="restart"/>应用配置并重启服务器
              <br/>  
              <input type="checkbox" name="act" value="reconfig"/>重新配置
            </p>
            <input type='submit' value='确定' />
                <span class="red"><%=request.getAttribute("msg") %></span>
          </section>
        </form>
      </div>

    </div>
  </body>
</html>