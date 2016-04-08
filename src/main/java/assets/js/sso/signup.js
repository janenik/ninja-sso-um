(function() {
    var captchaImage = document.getElementById('captchaImage');
    $(captchaImage).on('click touchend', function(e) {
        e.preventDefault();
        e.stopPropagation();
        captchaImage.src = captchaImage.src + '&_=' + Math.random();
    });
    $('#countriesSelector a').on('click touchend', function(e) {
        e.preventDefault();
        e.stopPropagation();
    });
 })();