/**<#-- This javascript source is the Freemarker template and supposed to be included. -->*/
(function() {
    var signinKey = 'signIn.rememberMe';
    $('#signInForm').submit(function() {
        var value = $('#emailOrUsername').val();
        if ($('#rememberMe').is(':checked') && value) {
            value = btoa(encodeURIComponent('sde' + value));
            localStorage.setItem(signinKey, value);
        } else {
            localStorage.removeItem(signinKey);
        }
        $('#signInSubmit').attr('disabled', 'disabled');
        $('#signInSubmit').html('${i18n("signInButtonTitleLoading")}');
    });
    var savedValue = localStorage.getItem(signinKey);
    if (savedValue) {
        var value = decodeURIComponent(atob(savedValue)).substring(3);
        if (!$('#emailOrUsername').val()) {
            $('#emailOrUsername').val(value);
        }
        $('#rememberMe').prop('checked', true);
    }
})();