<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=social.displayInfo; section>
    <#if section = "title">
        ${msg("loginTitle",(realm.displayName!''))}
    <#elseif section = "header">
        <link href="https://fonts.googleapis.com/css?family=Muli" rel="stylesheet"/>
        <link href="${url.resourcesPath}/img/favicon.png" rel="icon"/>
        <script>
            function togglePassword(inputPasswordId, eyeImageId) {
                var x = document.getElementById(inputPasswordId);
                var v = document.getElementById(eyeImageId);
                if (x.type === "password") {
                    x.type = "text";
                    v.src = "${url.resourcesPath}/img/eye.png";
                } else {
                    x.type = "password";
                    v.src = "${url.resourcesPath}/img/eye-off.png";
                }
            }
        </script>
    <#elseif section = "header">
        ${msg("updatePasswordTitle")}
    <#elseif section = "form">
        <div>
            <img class="logo" src="${url.resourcesPath}/img/fraudbusters-logo.svg" alt="Fraudbusters">
        </div>
        <div class="box-container">
            <form id="kc-passwd-update-form" class="form update-password" action="${url.loginAction}" method="post">
                <div>
                    <label class="visibility" id="v" onclick="togglePassword('password-new', 'vi')">
                        <img id="vi" src="${url.resourcesPath}/img/eye-off.png">
                    </label>
                </div>
                <input type="password" class="login-field" id="password-new" name="password-new"
                       placeholder="${msg("password")}"
                       autofocus autocomplete="new-password"/>
                <div>
                    <label class="visibility" id="v-confirm" onclick="togglePassword('password-confirm', 'vi-confirm')">
                        <img id="vi-confirm" src="${url.resourcesPath}/img/eye-off.png">
                    </label>
                </div>
                <input type="password" id="password-confirm" name="password-confirm"
                       placeholder="${msg("Password confirm")}"
                       class="login-field"
                       autocomplete="confirm-password"/>
                <input class="submit" type="submit" value="${msg("doSubmit")}" tabindex="3">
            </form>
        </div>
    </#if>
</@layout.registrationLayout>