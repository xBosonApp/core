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
        <h1>欢迎使用</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          拥有业界最先进的开放式数据架构, 一站式部署与开发, 
          彻底解决高昂的企业软件开发与维护成本.
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>

          <h6>即将开始安装平台</h6>

          <section>
            <label>首次启动平台软件, 即可看到该配置画面, 
              一旦配置完成, 该画面将永久关闭</label>  
            <br/><br/>
            <input type='submit' name="begin_config" value='开始配置'/>
            <br/><br/>

            <div><%=  System.getProperty("os.name") + ", "
              + System.getProperty("os.arch") %></div>

            <div>
              <%= System.getProperty("java.vm.name") %>,
              <%= System.getProperty("java.version") %>,
              <span class='info'>[<%= System.getProperty("java.home") %>]</span>
            </div>

            <div><%= request.getAttribute("j2ee_info") %></div>

            <div>Config: version: <%=c.configVersion %>,
              directory: <span class='info'>[<%=c.configPath %>]</span></div>
          </section>
        </form>
      </div>

    </div>
  </body>
</html>