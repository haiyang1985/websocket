<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
    <meta charset="UTF-8">
</head>
<body>
<span>Netty WebSocket 时间服务器</span>
<form onsubmit="return false;">
    <input type="text" name="message" value="Netty最佳实践"/>
    <br><br>
    <input type="button" value="发送WebSocket请求消息" onclick="send(this.form.message.value)"/>
    <hr color="blue"/>
    <h3>服务端返回的应答消息</h3>
    <textarea id="responseText" style="width:500px;height:300px;"></textarea>
</form>
<script type="text/javascript">
    var socket;
    if (!window.WebSocket) {
        window.WebSocket = window.MozWebSocket;
    }
    if (window.WebSocket) {
        socket = new WebSocket("ws://localhost:8090");
        socket.onmessage = function (event) {
            var ta = document.getElementById('responseText');
//            ta.value = "";
//            ta.value = event.data
            ta.value = ta.value + "\n" + event.data;
        };
        socket.onopen = function (event) {
            var ta = document.getElementById('responseText');
            ta.value = "打开WebSocket服务正常，浏览器支持WebSocket!";
        };
        socket.onclose = function (event) {
            var ta = document.getElementById('responseText');
            ta.value = "";
            ta.value = "WebSocket 关闭!";
        };
    }
    else {
        alert("抱歉，您的浏览器不支持WebSocket协议!");
    }

    function send(message) {
        if (!window.WebSocket) {
            return;
        }
        if (socket.readyState == WebSocket.OPEN) {
            socket.send(message);
        }
        else {
            alert("WebSocket连接没有建立成功!");
        }
    }
</script>
</body>
</html>