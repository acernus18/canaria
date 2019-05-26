<html lang="en">
<head>
    <title>Login</title>

    <script src="/webjars/jquery/3.0.0/jquery.min.js"></script>
    <script>
        function getFormData(pair) {
            let result = {};

            $.map(pair, function (item) {
                result[item['name']] = item['value'];
            });

            return result;
        }

        // getFormData($('#login-form').serializeArray());
    </script>
</head>
<body>
<form action="/passport/login" method="POST" id="login-form">
    <h1>登录管理系统</h1>
    <div>
        <label>
            <input type="text" placeholder="请输入用户名" name="username" required=""/>
        </label>
    </div>
    <div>
        <label>
            <input type="password" placeholder="请输入密码" name="password" required=""/>
        </label>
    </div>
    <div>
        <label>
            <input type="checkbox" id="rememberMe" name="rememberMe">记住我
        </label>
    </div>
    <div>
        <button type="submit">登录</button>
    </div>
</form>
</body>
</html>
