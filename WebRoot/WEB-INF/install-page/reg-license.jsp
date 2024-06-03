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
        <h1>软件授权许可</h1>
      </div>

      <div id='right'>
        <h2>xBoson 大数据平台</h2>

        <section>
          填写完成申请表, 下载后将授权申请与本公司负责人连接,
          申请通过后, 您将接收到程序的授权许可;<br/>

          可以跳过该步骤, 但务必保留授权申请表以备后用.
        </section>

        <form method="POST">
          <input type="hidden" name="next" value="1"/>
          <input type="hidden" name="op" value="1"/>
          <h6>授权申请表</h6>

          <table><tbody>
          <tr>
            <td>公司名称:</td>
            <td><input name="company" value="${ param.company }" /></td>
            </td>
          </tr>

          <tr>
            <td>域名地址: </td>
            <td><input name="dns" value="${ param.dns }" /></a>
            </td>
          </tr>

          <tr>
            <td>邮箱地址: </td>
            <td><input name="email" value="${ param.email }"/></td>
          </tr>

          <tr>
            <td>使用时间: </td>
            <td>
              <select name="useTime" value="${ param.useTime }">
                <option value='1'>1年</option>
                <option value='2'>2年</option>
                <option value='3'>3年</option>
                <option value='5'>5年</option>
                <option value='10'>10年</option>
              </select>
            </td>
          </tr>

          <tr>
            <td></td>
            <td>
              <input type='submit' value='下载授权申请' id='download'/>
              <span class="red" id='message'></span>
            </td>
          </tr>

          <tr>
            <td>授权许可文件:</td>
            <td>
              <input type='file' id='filepath'/>
              <span class="red" id='message'></span>
            </td>
          </tr>

          <tr>
            <td></td>
            <td>
              <input type='button' value='上传授权许可' id='upload'/>
              <input value='跳过授权' name='skip' type='submit'/>
              <span class="red" id='message2'></span>
            </td>
          </tr>

          </tbody></table>
        </form>
      </div>

    </div>
<script>
jQuery(function($) {
  var messageOut = $("#message");
  var messageOut2 = $("#message2");
  var msg = [<%=request.getAttribute("msg") %>][0];

  if (typeof msg == 'string') {
    messageOut.text(msg);
  } else if (msg) {
    messageOut.text(msg.msg);
    openDownloadDialog(msg.code, "license.req");
  }

  $("#upload").click(function() {
    var data = { op:2, yaml: "", next: 1 };
    var reader = new FileReader();

    reader.readAsText( $("#filepath")[0].files[0] );
    reader.onload = function() {
      data.yaml = reader.result;

      $.post("api", data, function(ret) {
        if (ret == 'next' || ret.length > 200) {
          location.reload();
        }
        messageOut.text("");
        messageOut2.text(ret);
      }, 'text');
    };
  });

  function openDownloadDialog(str, filename) {
    var data = new Blob([ str ], {type: "application/octet-binary"});
    var url  = URL.createObjectURL(data, {oneTimeOnly: true});
    var a    = $('<a class="mhide"></a>');
    a.attr({ href: url, download: filename });
    a[0].click();
  }
});
</script>
  </body>
</html>