/**<#-- This javascript source is a Freemarker template and is supposed to be included. -->*/
(function() {
    var captchaImage = document.getElementById('captchaImage');
    $(captchaImage).on('click touchend', function(e) {
        e.preventDefault();
        e.stopPropagation();
        captchaImage.src = captchaImage.src + '&_=' + Math.random();
    });
 })();